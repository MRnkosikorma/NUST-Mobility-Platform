# Phase 1 runtime contract

## `GET /health`

Returns `200` and `{ "status": "ok" }` when the API process is running.

## `GET /ready`

Returns `200` and the configured dependency state. It deliberately reports the database as `not-configured` until the PostgreSQL migration runner is added.

All responses include `X-Request-Id`. Unknown routes use the approved user-safe error envelope. This foundation intentionally exposes no student, payment, validation, or admin action.
