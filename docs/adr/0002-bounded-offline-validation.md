# ADR 0002: Bounded offline validation for pilot

**Status:** Accepted for pilot, subject to field-test gate  
**Date:** 10 September 2026

## Context

The approved pilot requires functionality during unreliable connectivity, but an offline device cannot provide the same real-time balance or replay guarantees as the server.

## Decision

Support a limited offline-validation mode. Enrolled conductor devices receive expiring, signed, device-bound entitlements while online. Offline scans create an ordered, tamper-evident local queue and a **provisional** acceptance. On synchronization, the server enforces idempotency and business rules, posts accepted rides atomically, and raises conflicts for review.

## Guardrails

- Small configurable offline exposure and short expiry, per device and student credential.
- Mandatory device enrollment, revocation, clock-skew limits, and secure local storage.
- No final settlement decision on a device.
- Clear UI states for accepted online, queued provisional, synchronized, and exception.
- Pilot field testing and fraud review before expanding offline limits.

## Consequences

The product accepts bounded operational and financial exposure for availability. Offline usage requires reconciliation and exception workflows, not silent balance adjustment.
