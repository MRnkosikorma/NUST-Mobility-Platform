# Security and offline model

**Status:** Draft for pilot design  
**Core rule:** An offline scan is a provisional boarding authorization, not an irreversible debit or settlement decision.

## Threat model and mitigations

| Threat | Primary controls | Residual risk |
|---|---|---|
| QR replay / screenshot | 30–60 second one-time online QR nonce; opaque signed payload; offline entitlements are serialised and single-use at reconciliation | A disconnected scanner cannot see other scanners' spent sets. |
| Offline double-spend | Short-expiry, signed offline entitlements; per-device/student/value/count caps; reconciliation and dispute queue | Bounded exposure remains until sync. |
| Fake/compromised conductor device | Individual login, device/vehicle/conductor assignment, hardware-backed keys where supported, MDM/attestation, short device certificate and revocation bundle TTL | Compromised hardware needs monitoring and rapid revocation. |
| Stolen student phone | Short QR lifetime, credential epoch rotation, self-service revoke, optional device-bound proof | Valid credential may be usable for the short remaining window. |
| Webhook spoofing / duplicate top-up | Provider signature verification, replay window, durable event uniqueness, idempotent posting, reconciliation | Real provider specifics await verified contract. |
| Insider or admin abuse | Server RBAC, separation of duties, dual approval for high-impact configuration, append-only audit | Requires active NUST audit oversight. |
| Data leakage | Data minimization, TLS/at-rest encryption, redacted logs, masked support views, retention policy | Privacy/data-sharing policy still needs NUST approval. |

## QR credentials

Online QR payloads contain only version, institution ID, opaque credential ID, credential epoch, issued/expiry time, nonce, key ID, and signature. They never contain a student number, name, balance, PIN, password, or payment data.

The server resolves all eligibility, balance, fare, vehicle, and risk decisions. Online nonce reuse is rejected atomically.

## Bounded offline protocol

1. While connected, an approved conductor device downloads a signed, versioned, expiry-bound device/status, fare, route, key, revocation, and policy bundle.
2. Eligible students receive a very small set of signed one-time offline boarding entitlements. Each is scoped to the credential epoch, permitted fare/value class, device-binding proof where supported, and expiry.
3. Offline, the scanner validates signatures, scope, expiry, cached revocation status, local spent set, device integrity, and configured caps.
4. It creates an encrypted, append-only, device-signed transaction with monotonically increasing sequence number and previous-record hash; the UI says **Pending verification**.
5. When connected, it uploads ordered batches. The server validates device status, signatures, bundle hash, sequence continuity, vehicle assignment, fare, entitlement serial, and fraud rules. It independently returns `CONFIRMED`, `REJECTED`, `DUPLICATE`, `DISPUTED`, or `ESCALATED`.

Exact exposure limits are NUST operating-policy settings, not code constants. Defaults must be conservative and automatically fall back to online-only when device integrity, caps, expiry, or synchronization deadlines fail.

## Key and audit requirements

- Separate asymmetric signing keys for online credentials, offline entitlements, device certificates, signed policy/revocation bundles, and audit integrity.
- Store root and issuer keys in managed KMS/HSM; applications receive narrow signing authority rather than raw root keys.
- Generate conductor device private keys locally and non-exportably where hardware supports it.
- Rotate keys with overlap; compromise revokes affected key IDs/devices, rotates credential epochs, and records the full response.
- Audit every auth event, privileged data access, device lifecycle event, policy/fare/route change, scan, sync result, financial event, exception, and export.

## Operational monitoring

Alert on replay attempts, repeated offline entitlement serials, sequence gaps/hash-chain breaks, stale or integrity-failed devices, invalid signatures, unsynced queue growth, abnormal offline concentration, admin privilege changes, mass exports, and any provider-to-ledger reconciliation break.

Before live monetary value, NUST and the licensed partner must accept the offline loss/dispute policy and complete legal, privacy, security, and payment-provider review.
