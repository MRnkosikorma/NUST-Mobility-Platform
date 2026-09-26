import type {
  NfcCardRegisterRequest,
  NfcCardRegisterResponse,
  NfcCardVerifyRequest,
  NfcCardVerifyResponse,
} from "../../../../../packages/contracts/src/index.js";
import {
  registerNfcCard,
  verifyNfcCard,
  getCardsByUserId,
} from "../../../../../packages/domain/src/index.js";
import { recordJourneyDebit } from "../ledger/accounting.js";
import { getRouteById } from "../mobility/catalogue.js";

export function handleCardRegistration(
  req: NfcCardRegisterRequest,
): NfcCardRegisterResponse {
  const card = registerNfcCard(req.cardUid, req.userId, req.cardType);
  return { card };
}

export function handleCardVerification(
  req: NfcCardVerifyRequest,
): NfcCardVerifyResponse {
  const verification = verifyNfcCard(req.cardUid);

  if (!verification.valid || !verification.card) {
    return {
      validationId: `val_nfc_fail_${Date.now()}`,
      outcome: "DECLINED",
      reason: verification.reason ?? "INVALID_NFC_CARD",
      cardUid: req.cardUid,
      timestamp: new Date().toISOString(),
      fareMinor: req.fareMinor,
      currency: "USD",
    };
  }

  const { userId } = verification.card;
  const route = getRouteById(req.routeId);
  const routeName = route ? route.name : "NUST Campus Route";

  // Post ride debit to ledger
  const debitResult = recordJourneyDebit(
    userId,
    req.fareMinor,
    routeName,
    req.vehicleRegistration,
    req.isOfflineMode ?? false,
  );

  if (!debitResult.success) {
    return {
      validationId: `val_nfc_fail_${Date.now()}`,
      outcome: "DECLINED",
      reason: debitResult.errorReason ?? "INSUFFICIENT_FUNDS",
      cardUid: req.cardUid,
      userId,
      timestamp: new Date().toISOString(),
      fareMinor: req.fareMinor,
      currency: "USD",
    };
  }

  return {
    validationId: `val_nfc_${Date.now()}`,
    outcome: req.isOfflineMode ? "PROVISIONAL" : "CONFIRMED",
    cardUid: req.cardUid,
    userId,
    timestamp: new Date().toISOString(),
    fareMinor: req.fareMinor,
    currency: "USD",
  };
}

export function getUserNfcCards(userId: string) {
  return { cards: getCardsByUserId(userId) };
}
