import { test } from "node:test";
import assert from "node:assert/strict";
import { generateTransportCredential } from "../../packages/domain/src/credentials.js";
import { convertCardUidToCredentialToken } from "../../packages/domain/src/nfc-card.js";
import { validateConductorScan } from "../../apps/api/src/modules/credentials/transport-credentials.js";
import { handleCardVerification } from "../../apps/api/src/modules/nfc/nfc-cards.js";
import {
  getStudentBalance,
  getStudentJourneys,
  topUpStudentBalance,
} from "../../apps/api/src/modules/ledger/accounting.js";

// ══════════════════════════════════════════════════════════════════════════════
//  APDU PROTOCOL CONSTANTS (Must match Android HostApduService & ReaderCallback)
// ══════════════════════════════════════════════════════════════════════════════
const SELECT_AID_APDU = Buffer.from([
  0x00, 0xa4, 0x04, 0x00, 0x07, 0xf0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
]);

const SW_SUCCESS = Buffer.from([0x90, 0x00]);
const SW_FAILURE = Buffer.from([0x6a, 0x82]);

/**
 * Simulates Student Android HostApduService (MobilityCardService.kt)
 */
function simulateStudentApduService(
  commandApdu: Buffer,
  activeToken: string,
): Buffer {
  if (commandApdu.equals(SELECT_AID_APDU)) {
    const payloadBytes = Buffer.from(activeToken, "utf-8");
    return Buffer.concat([payloadBytes, SW_SUCCESS]);
  }
  return SW_FAILURE;
}

/**
 * Simulates Conductor Android IsoDep Reader (MainActivity.kt onTagDiscovered)
 */
function simulateConductorIsoDepReader(apduResponse: Buffer): {
  success: boolean;
  payload: string | null;
} {
  if (apduResponse.length >= 2) {
    const sw1 = apduResponse[apduResponse.length - 2];
    const sw2 = apduResponse[apduResponse.length - 1];
    if (sw1 === 0x90 && sw2 === 0x00) {
      const payloadBytes = apduResponse.subarray(0, apduResponse.length - 2);
      return {
        success: true,
        payload: payloadBytes.toString("utf-8"),
      };
    }
  }
  return { success: false, payload: null };
}

// ══════════════════════════════════════════════════════════════════════════════
//  END-TO-END NFC VALIDATION TESTS
// ══════════════════════════════════════════════════════════════════════════════

test("E2E NFC HCE Tap: Student active token -> APDU exchange -> Conductor validation -> Ledger debit", () => {
  const userId = "usr_student_001";
  const institutionId = "00000000-0000-0000-0000-000000000001";

  // Step 1: Top up initial student wallet balance
  topUpStudentBalance(userId, 500); // Add $5.00
  const initialBalance = getStudentBalance(userId).balanceMinor;

  // Step 2: Student app generates a short-lived transport credential (uses default secret)
  const credential = generateTransportCredential(
    userId,
    institutionId,
    undefined,
    300,
  );
  assert.ok(credential.opaqueToken.length > 0);

  // Step 3: Student HCE Service receives SELECT AID APDU command over ISO-DEP
  const apduResponse = simulateStudentApduService(
    SELECT_AID_APDU,
    credential.opaqueToken,
  );

  // Step 4: Conductor Reader parses APDU response byte stream
  const readResult = simulateConductorIsoDepReader(apduResponse);
  assert.equal(readResult.success, true);
  assert.equal(readResult.payload, credential.opaqueToken);

  // Step 5: Conductor submits captured credential token to API backend for ride validation
  const validationResult = validateConductorScan({
    opaqueToken: readResult.payload!,
    vehicleRegistration: "NUST-BUS-01",
    routeId: "route_nust_cbd",
    fareMinor: 100, // $1.00 fare
  });

  // Step 6: Verify ticket validation status & ledger update
  assert.equal(validationResult.outcome, "CONFIRMED");

  const updatedBalance = getStudentBalance(userId).balanceMinor;
  assert.equal(updatedBalance, initialBalance - 100);

  const journeys = getStudentJourneys(userId);
  assert.ok(journeys.some((j) => j.vehicleReg === "NUST-BUS-01"));
});

test("E2E Physical NFC Tap Card: Hardware Card UID -> Conductor tap -> API verify -> Ledger debit", () => {
  const cardUid = "04:A2:8B:3C:90:12:80"; // Seed card assigned to usr_student_001
  const rawToken = convertCardUidToCredentialToken(cardUid);
  assert.equal(rawToken, "card_uid:04A28B3C901280");

  const initialBalance = getStudentBalance("usr_student_001").balanceMinor;

  // Conductor receives physical card UID tap and verifies card
  const result = handleCardVerification({
    cardUid,
    vehicleRegistration: "NUST-BUS-02",
    routeId: "route_cbd_nust",
    fareMinor: 150,
  });

  assert.equal(result.outcome, "CONFIRMED");
  assert.equal(result.cardUid, cardUid);
  assert.equal(result.userId, "usr_student_001");

  const newBalance = getStudentBalance("usr_student_001").balanceMinor;
  assert.equal(newBalance, initialBalance - 150);
});

test("E2E NFC Error Path: Blocked physical NFC card UID is rejected", () => {
  const blockedCardUid = "11:22:33:44:55:66:77";

  const result = handleCardVerification({
    cardUid: blockedCardUid,
    vehicleRegistration: "NUST-BUS-01",
    routeId: "route_nust_cbd",
    fareMinor: 100,
  });

  assert.equal(result.outcome, "DECLINED");
  assert.equal(result.reason, "CARD_BLOCKED");
});

test("E2E NFC Error Path: Expired student APDU credential is rejected", () => {
  // Generate expired token (-10 seconds TTL using default secret)
  const expiredCred = generateTransportCredential(
    "usr_student_001",
    "00000000-0000-0000-0000-000000000001",
    undefined,
    -10,
  );

  const apduResponse = simulateStudentApduService(
    SELECT_AID_APDU,
    expiredCred.opaqueToken,
  );
  const readResult = simulateConductorIsoDepReader(apduResponse);
  assert.equal(readResult.success, true);

  const validationResult = validateConductorScan({
    opaqueToken: readResult.payload!,
    vehicleRegistration: "NUST-BUS-01",
    routeId: "route_nust_cbd",
    fareMinor: 100,
  });

  assert.equal(validationResult.outcome, "DECLINED");
  assert.equal(validationResult.reason, "CREDENTIAL_EXPIRED");
});
