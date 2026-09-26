# NUST Mobility Conductor

Native Android-first client for the development-only NUST Mobility pilot. It is deliberately a **conductor validation app**, not a student wallet or payment app.

## Current capability

- Displays a fictional assigned vehicle and fare context.
- Clearly differentiates online confirmation from offline **Pending verification**.
- Adds offline demonstration validations to an in-memory queue; it never labels them settled.
- Uses no camera, network, payment, student data, or device credentials yet.

## Run it

Open `apps/conductor` in Android Studio. The project targets Android API 35 and requires JDK 17. Select an emulator or development device and run `app`.

## Next implementation gates

1. NUST-approved conductor sign-in and server-side role/vehicle assignment.
2. Device enrollment and hardware-backed key strategy.
3. Camera QR scanner with opaque signed credentials only.
4. API contract for `GET /conductor/context` and `POST /conductor/validations`.
5. Encrypted durable offline queue, signed policy bundles, and ordered synchronization.

No real payment, real NUST account, or real travel authorisation belongs in this build.
