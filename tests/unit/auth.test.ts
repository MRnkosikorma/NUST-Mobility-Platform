import { test } from "node:test";
import assert from "node:assert/strict";
import { authenticateUser } from "../../apps/api/src/modules/identity/auth.js";

test("authenticateUser succeeds with correct seed credentials", () => {
  const result = authenticateUser({
    email: "student@nust.ac.zw",
    passwordHash: "StudentPass123!",
  });

  assert.notEqual(result, null);
  assert.equal(result?.email, "student@nust.ac.zw");
  assert.equal(result?.role, "STUDENT");
  assert.ok(result?.token.startsWith("bearer_"));
});

test("authenticateUser fails with invalid password", () => {
  const result = authenticateUser({
    email: "student@nust.ac.zw",
    passwordHash: "WrongPass",
  });

  assert.equal(result, null);
});
