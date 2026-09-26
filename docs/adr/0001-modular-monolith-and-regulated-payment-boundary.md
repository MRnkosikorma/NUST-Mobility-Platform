# ADR 0001: Modular monolith and regulated payment boundary

**Status:** Accepted for MVP  
**Date:** 10 September 2026

## Context

The NUST mobility pilot needs financial correctness, traceability, low operational cost, and a credible path to offline operation. No verified legal or regulated partner arrangement currently authorizes this product to custody funds or process payments.

## Decision

Build a TypeScript modular monolith backed by PostgreSQL. Separate application logic, payment initiation, provider processing/custody, internal ledgering, operator entitlement, and settlement into explicit modules and adapter boundaries. Use a development-only mock payment provider until a licensed provider is contracted and verified.

## Consequences

- Financial workflows can use one atomic database transaction and a single audit model.
- Adapter interfaces let a real provider replace the mock without rewriting student credit or ride validation logic.
- The MVP has lower deployment complexity than microservices.
- The platform must not claim to be a wallet custodian, payment processor, or regulator-approved service.
- A later scale-out may extract modules only after measured operational need.
