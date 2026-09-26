# NUST Mobility Student

Native Android student application for the development-only NUST Mobility pilot.

## Current capability

- Shows a fictional transport-credit balance and journey history.
- Displays and refreshes an opaque, short-lived development credential reference.
- Includes a visibly labelled mock top-up interaction for product-flow testing.
- Does not use a real QR code, real student identity, real payment, or a live NUST service.

## Run it

Open `apps/student` in Android Studio. The project targets Android API 35 and requires JDK 17. Run the `app` configuration on an emulator or development device.

## Before pilot use

The student app needs verified sign-in, server-issued signed QR credentials, a real credential renderer, accessible error states, provider-adapter top-up initiation, receipt retrieval, privacy notice, and approved support/revocation workflows. None may be substituted with client-side balance or payment logic.
