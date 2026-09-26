import type {
  CredentialResponse,
  ValidationRequest,
  ValidationResponse,
} from "../../../../../packages/contracts/src/index.js";
import {
  generateTransportCredential,
  verifyTransportCredential,
} from "../../../../../packages/domain/src/index.js";
import { recordJourneyDebit } from "../ledger/accounting.js";
import { getRouteById } from "../mobility/catalogue.js";

// Anti-replay cache for development
const usedCredentials = new Set<string>();

export function issueCredential(
  userId: string,
  institutionId: string,
): CredentialResponse {
  const generated = generateTransportCredential(userId, institutionId);
  return {
    credentialId: generated.credentialId,
    opaqueToken: generated.opaqueToken,
    expiresAt: generated.expiresAt,
    ttlSeconds: 60,
  };
}

export function validateConductorScan(
  req: ValidationRequest,
): ValidationResponse {
  const verification = verifyTransportCredential(req.opaqueToken);

  if (!verification.valid || !verification.payload) {
    return {
      validationId: `val_fail_${Date.now()}`,
      outcome: "DECLINED",
      reason: verification.reason ?? "INVALID_TOKEN",
      timestamp: new Date().toISOString(),
      fareMinor: req.fareMinor,
      currency: "USD",
    };
  }

  const { credentialId, userId } = verification.payload;

  // Check anti-replay
  if (usedCredentials.has(credentialId)) {
    return {
      validationId: `val_fail_${Date.now()}`,
      outcome: "DECLINED",
      reason: "REPLAY_ATTEMPT_DETECTED",
      timestamp: new Date().toISOString(),
      fareMinor: req.fareMinor,
      currency: "USD",
    };
  }

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
      validationId: `val_fail_${Date.now()}`,
      outcome: "DECLINED",
      reason: debitResult.errorReason ?? "DEBIT_FAILED",
      timestamp: new Date().toISOString(),
      fareMinor: req.fareMinor,
      currency: "USD",
    };
  }

  // Mark credential as redeemed
  usedCredentials.add(credentialId);

  return {
    validationId: `val_${Date.now()}`,
    outcome: req.isOfflineMode ? "PROVISIONAL" : "CONFIRMED",
    timestamp: new Date().toISOString(),
    fareMinor: req.fareMinor,
    currency: "USD",
  };
}
