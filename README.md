# NUST Mobility Platform

Foundation work for a university-focused mobility-payment platform, initially scoped to a pilot at NUST in Bulawayo, Zimbabwe.

## Current status

**Phase 1 foundation in progress.** NUST is an approved pilot partner and owns pilot-phase discovery and rights. The repository currently contains a development-only API foundation; no production code or real-money integration has been started.

## Local development

Copy `.env.example` to `.env`, install dependencies with `pnpm install`, then run `pnpm dev:api`. The service exposes `GET /health` and `GET /ready` on port 3000 by default. Use `pnpm check` before reviewing a change.

The PWA shell is intentionally static at this stage. Do not connect it to real NUST, identity, payment, or transport data.

## Android conductor app

The Android-first conductor client is in [`apps/conductor`](apps/conductor/README.md). It provides a development-only vehicle-and-scan workflow and visibly distinguishes an online confirmation from an offline provisional validation. Open that folder in Android Studio; it requires JDK 17 and an Android API 35 SDK.

## Android student app

The student app is in [`apps/student`](apps/student/README.md). It demonstrates a fictional credit balance, short-lived opaque credential flow, and journey history without storing funds or connecting to NUST systems. Open it in Android Studio with JDK 17 and Android API 35 SDK.

## First review

Read [the architecture and MVP plan](docs/01-architecture-and-mvp-plan.md), then the supporting design set in `docs/`. Regulatory and regulated-provider dependencies remain **REQUIRES VERIFICATION**; approval of the pilot does not constitute financial-services approval.

## Guiding boundaries

- The platform does not custody regulated customer funds.
- Payment initiation, processing, application ledgering, operator entitlement, and settlement are separately modeled.
- Initial payment behavior uses an explicitly labelled mock provider only.
- MVP is a modular monolith with a PWA for students and conductors.
- The initial QR flow is designed for intermittent connectivity; offline use has bounded risk and later reconciliation.
