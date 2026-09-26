# API design and build plan

**Status:** Draft for Phase 1 implementation  
**Style:** REST/JSON at `/v1`, defined in OpenAPI 3.1 and runtime-validated.

## API rules

- Protected requests use bearer access tokens. Device requests additionally prove possession of the enrolled device key.
- Every mutation includes `X-Request-Id` and `Idempotency-Key`. Financial, validation, and synchronization actions reject a reused key with a different body.
- Responses include `X-Request-Id`; timestamps are UTC ISO-8601; money is `{ "amountMinor": 100, "currency": "USD" }`.
- Tenant identity is derived server-side from the authenticated principal, never from an untrusted request field.

| Group | Initial endpoints | Access |
|---|---|---|
| Auth | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/verify-phone` | Public/authenticated as appropriate |
| Student | `GET /me`, `/me/balance`, `/me/journeys`, `/me/receipts/{id}`, `POST /me/credentials` | Student, own data only |
| Catalogue | `GET /routes`, `/fares`, `/operators`, `/vehicles` | Tenant-scoped authenticated reader |
| Provider boundary | `POST /payment-intents`, `GET /payment-intents/{id}`, `POST /webhooks/payments/{provider}` | Student / verified provider only |
| Conductor | `GET /conductor/context`, `POST /conductor/validations`, `POST /conductor/offline-batches` | Enrolled, assigned conductor/device |
| Operator | `GET/POST /operator/vehicles`, `GET /operator/journeys`, `/operator/payables` | Operator administrator, own operator |
| University | CRUD `/university/operators`, `/vehicles`, `/routes`, `/fares`, `/subsidies`; reports and complaints | NUST scoped administrators |
| Platform | configuration, adapter status, fraud/reconciliation exceptions, audit search | Narrowly scoped platform administrator |

### Error envelope

```json
{
  "error": {
    "code": "CREDENTIAL_EXPIRED",
    "message": "This transport credential has expired. Ask the student to refresh it.",
    "requestId": "01J..."
  }
}
```

Use `401` for unauthenticated, `403` for forbidden, tenant-scoped `404` for missing resource, `409` for idempotency/duplicate/state conflict, `422` for business-rule rejection, `429` for rate limiting, and `503` for unavailable verified dependencies.

## Implementation shape

```text
nust-mobility-platform/
  apps/api/src/modules/{identity,institutions,mobility,credentials,ledger,payments,operations,audit}/
  apps/web/                         # responsive student/admin PWA
  apps/conductor/                   # Android-first conductor delivery track
  packages/{contracts,domain,config}/
  infrastructure/{docker,migrations,deploy}/
  docs/{adr,api,security,operations}/
  tests/{unit,integration,contract,e2e,performance}/
```

Use strict TypeScript, PostgreSQL, typed validation/contracts, an explicit database migration tool, and containerized local dependencies. Redis or object storage are introduced only when a defined capability needs them. The payment adapter is mock-only.

## Phase 1 file/component plan

1. Repository workspaces, strict TypeScript, lint/format/test configuration, and secret-safe environment validation.
2. API platform layer: request IDs, error envelope, structured redacted logging, health/readiness endpoints, rate-limit policy, and telemetry hooks.
3. Database migration baseline for institutions, users, roles/permissions, memberships, sessions, enrolled devices, audit events, idempotency keys, and outbox events.
4. Identity, tenant-scope, and permission guards with no client-authorized shortcuts.
5. Development-only seed process for fictional NUST pilot fixtures.
6. Minimal authenticated PWA shell with role routing; unsupported features remain absent rather than mocked as complete.
7. CI checks: format, lint, strict types, build, schema/OpenAPI validation, unit/integration/contract tests, migration test, dependency/secret/security scans.

## Release gates

- Phase 1: code/security review confirms tenant isolation, audit immutability, migration rollback behavior, and no leaked secrets.
- Phase 2: NUST approves student-verification workflow, device-enrollment workflow, pilot privacy notice, and support/escalation contacts.
- Before any real value: NUST and an appropriately licensed partner verify legal, custody, payment, reconciliation, settlement, privacy, and incident responsibilities.
