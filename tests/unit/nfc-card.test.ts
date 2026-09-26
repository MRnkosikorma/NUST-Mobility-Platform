import { test } from "node:test";
import assert from "node:assert/strict";
import {
  normalizeCardUid,
  registerNfcCard,
  verifyNfcCard,
  convertCardUidToCredentialToken,
} from "../../packages/domain/src/nfc-card.js";
import {
  handleCardRegistration,
  handleCardVerification,
} from "../../apps/api/src/modules/nfc/nfc-cards.js";

test("normalizeCardUid formats raw UIDs into uppercase hex without separators", () => {
  assert.equal(normalizeCardUid("04:a2:8b:3c:90:12:80"), "04A28B3C901280");
  assert.equal(normalizeCardUid("card_uid:04-A2-8B"), "04A28B");
});

test("verifyNfcCard validates seed active cards and rejects blocked cards", () => {
  const activeVerification = verifyNfcCard("04:A2:8B:3C:90:12:80");
  assert.equal(activeVerification.valid, true);
  assert.equal(activeVerification.card?.userId, "usr_student_001");

  const blockedVerification = verifyNfcCard("11:22:33:44:55:66:77");
  assert.equal(blockedVerification.valid, false);
  assert.equal(blockedVerification.reason, "CARD_BLOCKED");
});

test("registerNfcCard registers a new physical tap card to a student", () => {
  const newCard = registerNfcCard(
    "AA:BB:CC:DD:EE",
    "usr_student_999",
    "MIFARE_DESFIRE",
  );
  assert.equal(newCard.cardUid, "AABBCCDDEE");
  assert.equal(newCard.userId, "usr_student_999");
  assert.equal(newCard.status, "ACTIVE");

  const verified = verifyNfcCard("AABBCCDDEE");
  assert.equal(verified.valid, true);
});

test("registerNfcCard rejects duplicate registration to a different user", () => {
  assert.throws(
    () => registerNfcCard("04A28B3C901280", "usr_another_user"),
    /CARD_ALREADY_REGISTERED_TO_ANOTHER_USER/,
  );
});

test("handleCardVerification debits student ledger on valid tap card tap", () => {
  const response = handleCardVerification({
    cardUid: "04A28B3C901280",
    vehicleRegistration: "NUST-BUS-01",
    routeId: "route_cbd_nust",
    fareMinor: 100,
  });

  assert.equal(response.outcome, "CONFIRMED");
  assert.equal(response.cardUid, "04A28B3C901280");
  assert.equal(response.userId, "usr_student_001");
});

test("handleCardRegistration registers physical NFC tap card via API handler", () => {
  const response = handleCardRegistration({
    cardUid: "99:88:77:66:55",
    userId: "usr_student_003",
    cardType: "GENERIC_NFC",
  });

  assert.equal(response.card.cardUid, "9988776655");
  assert.equal(response.card.userId, "usr_student_003");
});

test("convertCardUidToCredentialToken produces standard card_uid token string", () => {
  const token = convertCardUidToCredentialToken("04:A2:8B");
  assert.equal(token, "card_uid:04A28B");
});
