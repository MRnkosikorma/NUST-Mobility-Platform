import { test } from "node:test";
import assert from "node:assert/strict";
import {
  generateTransportCredential,
  verifyTransportCredential,
} from "../../packages/domain/src/credentials.js";

test("generate and verify valid transport credential token", () => {
  const generated = generateTransportCredential(
    "usr_student_001",
    "inst_001",
    "dev_secret",
    60,
  );
  assert.ok(generated.opaqueToken.includes("."));

  const verification = verifyTransportCredential(
    generated.opaqueToken,
    "dev_secret",
  );
  assert.equal(verification.valid, true);
  assert.equal(verification.payload?.userId, "usr_student_001");
  assert.equal(verification.payload?.institutionId, "inst_001");
});

test("verify rejects token with incorrect HMAC secret", () => {
  const generated = generateTransportCredential(
    "usr_student_001",
    "inst_001",
    "secret_A",
    60,
  );
  const verification = verifyTransportCredential(
    generated.opaqueToken,
    "secret_B",
  );

  assert.equal(verification.valid, false);
  assert.equal(verification.reason, "SIGNATURE_MISMATCH");
});

test("verify rejects expired token", () => {
  const generated = generateTransportCredential(
    "usr_student_001",
    "inst_001",
    "dev_secret",
    -10,
  );
  const verification = verifyTransportCredential(
    generated.opaqueToken,
    "dev_secret",
  );

  assert.equal(verification.valid, false);
  assert.equal(verification.reason, "CREDENTIAL_EXPIRED");
});
