# Architecture and MVP plan

**Status:** Draft for review  
**Pilot context:** NUST, Bulawayo, Zimbabwe  
**Scope:** Architecture only. This document does not claim NUST integration, regulatory approval, or production payment connectivity.

## 1. Product boundary and operating model

This is university mobility-payment infrastructure, not a generic stored-value wallet. A student obtains transport credit through a regulated payment partner, uses an approved transport service, and receives an auditable journey record. The platform records service entitlements and ledger events; it must not represent itself as the legal custodian of customer funds.

| Concern | MVP owner / responsibility | Boundary |
|---|---|---|
| Identity and access | Platform, with university verification adapter | University identity integration is **REQUIRES VERIFICATION**; use a mock verifier in development. |
| Payment initiation | Platform UI/API | Creates an idempotent request to a provider adapter. |
| Payment processing and fund custody | Licensed PSP, bank, or mobile-money partner | Not implemented in MVP; mock provider only. Partner and legal basis are **REQUIRES VERIFICATION**. |
| Internal accounting | Platform | Immutable double-entry operational ledger reflecting confirmed provider events and ride debits. |
| Ride validation | Platform + approved conductor device | Creates a signed, auditable transport transaction. |
| Operator entitlement and settlement instruction | Platform | Calculates amounts due; actual payout occurs through an authorised partner. |
| University administration | University-appointed administrators | Manages eligible students, operators, routes, fares, subsidies, and audit access. |

## 2. Recommended MVP architecture

Build a TypeScript modular monolith with a relational database and one deployable API. It keeps financial operations atomic and explainable while leaving clean seams for regulated providers and future service extraction.

```text
Student PWA ───────┐                         ┌─ University Admin PWA
Conductor PWA ─────┼─ HTTPS / API Gateway ──┤
Operator PWA ──────┘                         └─ Platform Admin PWA
                              |
                   TypeScript modular-monolith API
    ┌─────────┬──────────┬─────────┬───────────┬────────────┐
    │ Identity│ Mobility │ Ledger  │ Payments  │ Admin/Audit│
    │ & RBAC  │ & fares  │ & wallet│ adapter  │ & reporting│
    └─────────┴──────────┴─────────┴───────────┴────────────┘
                |             |          |           |
          PostgreSQL       Object store  │        Observability
          (source of truth) (receipts)   │
                                        Mock PSP
                              (later: licensed provider)
```

### Core modules

1. **Identity and tenancy:** users, institutions, roles, sessions, device enrollment, and university identity-verification adapter.
2. **Mobility catalogue:** operators, vehicles, staff, routes, fare rules, journey records, and configurable passes/subsidies.
3. **Credential and validation:** QR credential issuance, scan verification, replay protection, online ride authorization, and offline queue/sync.
4. **Financial core:** accounts, journal entries, transport credit balance projection, top-up intents, provider-event processing, operator payables, refunds, and reconciliation exceptions.
5. **Administration and audit:** scoped dashboards, audit log, complaints, fraud signals, reports, and configuration.
6. **Integration boundary:** provider, SMS/email, university identity, maps, and analytics adapters. Every adapter has `mock`, `sandbox`, or `production` status.

## 3. Trust boundaries and security posture

```text
[Student device]       [Conductor device]         [Admin browser]
       |                      |                         |
       +------ untrusted clients over TLS -------------+
                               |
                    [Authenticated API boundary]
                      token + role + tenant checks
                               |
      ----------------------------------------------------------
      | business services + transactional DB + audit boundary  |
      ----------------------------------------------------------
                 |                                  |
      [licensed-payment-provider boundary]     [university boundary]
         signed webhooks / idempotency       verified adapter only
```

- Enforce authorization in each server-side use case, including institution scoping; never trust a UI role or client-supplied institution ID.
- Store monetary values as integer minor units plus ISO currency code. Do not use floating-point arithmetic.
- Use append-only ledger entries; corrections are compensating entries, never silent edits.
- Encrypt data in transit and at rest. Keep secrets in a managed secret store, rotate keys, and redact secrets and personal data from logs.
- QR credentials are short-lived, signed, opaque references—not a student number or mutable balance. A scanned credential is bound to an active account and validation context.
- Admin, operator, and conductor actions require durable audit events with actor, tenant, event type, target, request ID, timestamp, and before/after values where appropriate.

## 4. Primary online journey

1. Student completes verified onboarding and signs in.
2. Student requests a top-up. The API creates an idempotent payment intent.
3. The mock provider confirms the payment; later, a licensed provider webhook will do so.
4. Within one database transaction, the platform records the provider event, posts balanced ledger entries, and updates the balance projection.
5. Student presents a short-lived QR credential.
6. Approved, authenticated conductor scans it, selects the configured vehicle and fare context, and requests validation.
7. The API verifies credential, role, vehicle status, fare, available credit/pass, duplicate constraints, and fraud rules.
8. In one transaction it posts ride debit and operator-payable entries, writes the journey and audit event, then returns a receipt result.
9. University and operator dashboards read derived records; reconciliation compares provider, ledger, wallet projection, transport transaction, and settlement data.

## 5. Offline path: bounded, explicit risk

Offline validation is not equivalent to an online debit. The MVP should use the following controlled model:

1. While online, conductor devices enroll and obtain an expiring, signed entitlement cache constrained by institution, vehicle, device, credential, amount/fare range, count, and expiry.
2. The device validates a QR credential locally, writes a tamper-evident queued transaction with a unique device sequence, and gives a clearly marked provisional result.
3. The device synchronizes in order when connectivity returns. The server replays idempotently, rejects conflicts, posts accepted records atomically, and flags exceptions for review.
4. A device never decides final settlement. Offline limits, device revocation, clock-skew limits, suspicious-activity throttles, and rapid credential expiry constrain exposure.

**Design decision for review:** for the first pilot, begin with online-only validation or a very small, exposure-capped offline allowance. Full offline debit must be validated in field testing; it cannot guarantee prevention of every double-spend without a trusted secure element or live network.

## 6. Initial data model

All tenant-owned tables include `institution_id`; immutable finance and audit tables retain their own identifiers and timestamps.

| Domain | Main entities | Key constraints / indexes |
|---|---|---|
| Tenancy and identity | institutions, users, memberships, roles, permissions, sessions, devices | unique institution code; unique `(institution_id, student_number)` when verified; indexed tenant and status fields |
| Mobility | operators, operator_staff, vehicles, routes, stops, fare_rules, journeys | unique vehicle registration per institution; active vehicle/staff checks; route and effective-date indexes |
| Financial | accounts, ledger_transactions, ledger_entries, balance_projections, payment_intents, provider_events, operator_payables, settlements, refunds | balanced journal per transaction; unique provider event ID; unique idempotency key by actor/action; amount ≥ 0; money currency immutable per entry |
| Credentials | credentials, credential_keys, validation_attempts, offline_batches | credential opaque ID; expiry index; unique `(device_id, sequence_no)`; validation idempotency index |
| Governance | audit_events, complaints, fraud_alerts, reconciliation_runs, reconciliation_exceptions | append-only audit record; event time and tenant indexes; explicit status transitions |

### Financial posting example: $1.00 ride

The exact account names require accounting review, but the flow must remain balanced and traceable:

```text
confirmed student transport-credit liability  ─ debit 100 cents
operator payable                              ─ credit  (fare less configured platform fee)
platform fee payable/revenue clearing          ─ credit  (configured fee)
```

Production chart of accounts, tax treatment, settlement timing, and whether the student balance is a legal stored-value instrument are **REQUIRES VERIFICATION** with the payment partner and counsel.

## 7. API shape

Use versioned JSON over HTTPS; OpenAPI is the source of endpoint contracts. Every mutation requires an idempotency key and returns a request/transaction identifier.

| Area | Initial endpoints |
|---|---|
| Auth | `POST /v1/auth/register`, `POST /v1/auth/login`, `POST /v1/auth/verify-phone`, `POST /v1/auth/refresh`, `POST /v1/devices/enroll` |
| Student | `GET /v1/me`, `GET /v1/me/balance`, `POST /v1/me/credentials`, `GET /v1/me/journeys`, `GET /v1/me/receipts/{id}` |
| Top-up | `POST /v1/payment-intents`, `GET /v1/payment-intents/{id}`, `POST /v1/webhooks/payments/{provider}` |
| Validation | `POST /v1/conductor/validations`, `POST /v1/conductor/offline-batches`, `GET /v1/conductor/vehicle-context` |
| Operator | `GET /v1/operator/vehicles`, `GET /v1/operator/transactions`, `GET /v1/operator/payables` |
| University | CRUD for approved operators, vehicles, routes, fares, subsidies; reports and dispute review |
| Platform | institutions, adapter configuration, reconciliation exceptions, audit search, fraud alerts |

Standard errors: `401` unauthenticated, `403` forbidden, `404` tenant-scoped not found, `409` duplicate or stale state, `422` business-rule validation, `429` rate limited, `503` dependent service unavailable. Error bodies are stable, user-safe, and include a correlation ID.

## 8. RBAC baseline

- **Student:** own profile, credentials, balance, journeys, receipts, support requests.
- **Conductor/Driver:** assigned vehicle context and validation actions only; no balance editing or settlement access.
- **Operator:** its staff, vehicles, transactions, payables, and reports.
- **University transport admin:** institution operators, vehicles, routes, fares, complaints, reports.
- **University finance admin:** institution finance reports, subsidies, reconciliation visibility; no unrestricted identity administration.
- **University super admin:** scoped university configuration and administrator management.
- **Platform support:** limited support tools with audited, just-in-time access; no ledger mutation.
- **Platform finance admin:** reconciliation, settlement workflow, financial reports; no unlogged journal editing.
- **Platform super admin:** global configuration, tenant lifecycle, controlled admin assignment.
- **Auditor:** read-only, scoped audit and report access.

## 9. MVP delivery plan

| Phase | Outcome | Review gate |
|---|---|---|
| 0. Architecture | This approved design, regulatory assumptions, ADRs, and repository plan | Product/legal/technical approval |
| 1. Foundation | Repository, strict TypeScript, CI, tenant model, configuration, audit primitives, development seed data | Security and code-quality review |
| 2. Identity | Student onboarding, verification mock, RBAC, device enrollment | Auth and authorization tests pass |
| 3. Mobility catalogue | Operators, vehicles, staff, routes, configurable fares | University workflow review |
| 4. Financial mock | Mock provider, payment intents, append-only ledger, balance projection, receipts | Reconciliation and atomicity tests pass |
| 5. QR rides | Credential issuance, online conductor scan, journey debit, operator payable | End-to-end online journey passes |
| 6. Offline pilot | Queue, sync, bounded entitlements, conflict review | Field-test risk sign-off |
| 7. Dashboards | Operator/university dashboards, reports, complaints, subsidies | Role/UAT review |
| 8. Hardening | Threat-model controls, observability, load tests, backups, pilot runbook | Pilot readiness sign-off |

## 10. Proposed repository plan for Phase 1

```text
nust-mobility-platform/
  apps/
    web/                    # responsive PWA, role-specific experiences
    api/                    # TypeScript modular-monolith API
  packages/
    domain/                 # money, ledger, identity, mobility business rules
    contracts/              # API schemas and generated client types
    config/                 # shared linting, TypeScript, formatting config
  infrastructure/
    docker/                 # local development services only
    migrations/             # versioned PostgreSQL migrations
  docs/
    adr/                    # architecture decisions
    api/                    # OpenAPI and examples
    security/               # threat model and controls
    operations/             # runbooks, backups, incident response
  tests/
    integration/
    e2e/
```

Phase 1 creates only the foundation, tenancy, configuration, audit primitives, development-only seed process, basic health checks, and test pipeline. It does **not** create a real PSP, bank, NUST, mobile-money, or student-identity integration.

## 11. Risk register

| Risk | Impact | Early mitigation | Decision / verification needed |
|---|---|---|---|
| Unclear regulatory status for credit/wallet/custody | Critical | Keep custody and processing with licensed partners; use mock only | Zimbabwean legal/payment-partner review |
| University identity and approval process unavailable | High | Build adapter and controlled student import/mocks | NUST authorization and interface confirmation |
| Offline double spending / device compromise | High | Constrain offline scope, device enrollment, signed expiring cache, server reconciliation | Pilot policy for offline exposure |
| Operator adoption and incentive misalignment | High | Conduct discovery interviews, transparent earnings/payables, operator UAT | Pilot partner agreements and fare process |
| Payment webhook fraud or duplicate credit | High | Verify provider signatures, idempotency, replay window, reconciliation | Provider-specific contract |
| Incorrect fare or operator settlement | High | Effective-dated fares, double-entry ledger, approval workflow, exception alerts | Finance/accounting rules |
| Connectivity and low-end devices | Medium/High | PWA, small payloads, queue/sync, accessible low-bandwidth UX | Field-device and network testing |
| Personal-data misuse | High | Minimize data, scoped access, audit, retention policy | Privacy/legal review |
| Scope growth before validation | Medium | Phase gates and MVP exclusions | Sponsor alignment |

## 12. Approved pilot decisions

1. NUST is an approved partner, not merely a target pilot.
2. The pilot includes online validation and a strictly bounded offline-validation experiment.
3. NUST owns pilot-phase discovery and all pilot rights. Legal and licensed-provider work remains **REQUIRES VERIFICATION** before any real-money flow.
4. The proposed role scope is approved. Finance and auditor permissions exist from the start with restricted server-side access.
5. The detailed supporting documents now define the database, API, security/offline posture, and risk register.

## 13. Definition of ready to begin Phase 1

- This architecture is approved or revised.
- MVP scope and offline posture are agreed.
- Regulatory and partner work is explicitly owned and tracked as a dependency.
- A named product owner can approve fare, operator, and university workflows.
- Development repository location and access approach are confirmed.
