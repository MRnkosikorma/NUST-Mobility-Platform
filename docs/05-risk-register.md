# Pilot risk register

| Risk | Rating | Mitigation | Accountable decision owner |
|---|---|---|---|
| Regulatory/custody status unclear | Critical | Mock/non-cash credit only; segregate provider/custody interfaces | NUST + licensed partner + legal counsel |
| Offline double-spend | High | Provisional offline status, caps, device controls, reconciliation/dispute process | NUST pilot owner |
| University identity/data-sharing dependency | High | Adapter/mocked verification; approved data process before integration | NUST |
| Incorrect fare/operator payable | High | Effective-dated fares, approval/audit, double-entry ledger, reconciliation | NUST finance/transport owners |
| Payment-event spoofing/duplicates | High | Verified provider webhook, durable uniqueness, idempotency, reconciliation | Platform + future provider |
| Device theft/compromise | High | Enrollment, device binding, short TTL, MDM/attestation, revocation | NUST transport operations |
| Operator adoption | High | Discovery interviews, transparent payables, operator UAT and training | NUST pilot team |
| Poor connectivity | Medium/High | Offline caps, queue/sync, small payload PWA, field tests | NUST pilot team + platform |
| Privacy misuse | High | Minimize data, RLS/RBAC, audit, retention and DPA | NUST |
| Scope expansion | Medium | Phase gates and explicit MVP exclusions | NUST product owner |
