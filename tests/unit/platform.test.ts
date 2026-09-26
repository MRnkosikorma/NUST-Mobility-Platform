import assert from "node:assert/strict";
import test from "node:test";
import {
  errorBody,
  redactForLog,
  requestIdFor,
} from "../../apps/api/src/platform.js";
import { money } from "../../packages/domain/src/money.js";

test("platform primitives", () => {
  assert.match(requestIdFor("bad id"), /^[a-f0-9-]{36}$/);
  assert.equal(requestIdFor("request_123"), "request_123");
  assert.deepEqual(errorBody("NOT_FOUND", "Missing", "request_123"), {
    error: { code: "NOT_FOUND", message: "Missing", requestId: "request_123" },
  });
  assert.deepEqual(
    redactForLog({ token: "abc", nested: { password: "x", value: 1 } }),
    { token: "[REDACTED]", nested: { password: "[REDACTED]", value: 1 } },
  );
  assert.deepEqual(money(100, "USD"), { amountMinor: 100, currency: "USD" });
});
