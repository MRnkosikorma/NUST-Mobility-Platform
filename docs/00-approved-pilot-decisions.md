# Approved pilot decisions

**Recorded:** 10 September 2026  
**Authority:** Project sponsor / NUST-approved pilot direction

| Decision | Agreed position | Consequence |
|---|---|---|
| NUST relationship | NUST is an approved partner. | The pilot may be designed around NUST workflows; an actual integration still needs its named interface owner and technical authorization. |
| Pilot connectivity | Include online and bounded offline validation. | Offline validation must be provisional, tightly exposure-capped, device-bound, queued, and reconciled. |
| Discovery and rights | NUST owns pilot-phase discovery and all rights. | Project governance, research artefacts, data rights, and approvals must follow a NUST-approved agreement before field collection or release. |
| MVP roles | Proposed role scope accepted. | Build server-enforced RBAC for Student, Conductor, Driver, Operator, university administrative/finance/super-admin, platform support/finance/super-admin, and Auditor. |

## Still not implied by this approval

- NUST system/API access, data-sharing approval, or production identity integration.
- A licensed payment provider, bank, or mobile-money arrangement.
- Any authority to hold customer money, settle funds, or represent the service as regulator-approved.
- Production route, vehicle, fare, or student data. Development seeds stay fictional and clearly labelled.

## Immediate product consequence

The first pilot must support the full online ride flow and a constrained offline experiment with a monitored fallback. It must not label an offline provisional acceptance as irrevocably settled until server synchronization and reconciliation have completed.
