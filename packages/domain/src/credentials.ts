import { createHmac, randomBytes } from "node:crypto";

export interface TransportCredentialPayload {
  credentialId: string;
  userId: string;
  institutionId: string;
  issuedAt: number;
  expiresAt: number;
}

const DEFAULT_SECRET = "nust_pilot_dev_hmac_secret_key_2026";
const DEFAULT_TTL_SECONDS = 60;

/**
 * Generates an opaque, HMAC-signed transport credential string for student QR codes.
 * Format: `opaqueToken = payloadBase64Url.signatureHex`
 */
export function generateTransportCredential(
  userId: string,
  institutionId: string,
  secret: string = DEFAULT_SECRET,
  ttlSeconds: number = DEFAULT_TTL_SECONDS,
): { credentialId: string; opaqueToken: string; expiresAt: string } {
  const credentialId = `cred_${randomBytes(8).toString("hex")}`;
  const now = Math.floor(Date.now() / 1000);
  const expiresAtNum = now + ttlSeconds;

  const payload: TransportCredentialPayload = {
    credentialId,
    userId,
    institutionId,
    issuedAt: now,
    expiresAt: expiresAtNum,
  };

  const payloadJson = JSON.stringify(payload);
  const payloadB64 = Buffer.from(payloadJson).toString("base64url");
  const signature = createHmac("sha256", secret)
    .update(payloadB64)
    .digest("hex");

  const opaqueToken = `${payloadB64}.${signature}`;
  const expiresAtIso = new Date(expiresAtNum * 1000).toISOString();

  return {
    credentialId,
    opaqueToken,
    expiresAt: expiresAtIso,
  };
}

/**
 * Verifies an opaque QR credential token.
 * Checks signature authenticity, structure, and expiration.
 */
export function verifyTransportCredential(
  opaqueToken: string,
  secret: string = DEFAULT_SECRET,
): { valid: boolean; payload?: TransportCredentialPayload; reason?: string } {
  if (!opaqueToken || !opaqueToken.includes(".")) {
    return { valid: false, reason: "INVALID_FORMAT" };
  }

  const [payloadB64, signature] = opaqueToken.split(".");
  if (!payloadB64 || !signature) {
    return { valid: false, reason: "MALFORMED_TOKEN" };
  }

  const expectedSig = createHmac("sha256", secret)
    .update(payloadB64)
    .digest("hex");
  if (expectedSig !== signature) {
    return { valid: false, reason: "SIGNATURE_MISMATCH" };
  }

  try {
    const payloadJson = Buffer.from(payloadB64, "base64url").toString("utf-8");
    const payload = JSON.parse(payloadJson) as TransportCredentialPayload;
    const now = Math.floor(Date.now() / 1000);

    if (now > payload.expiresAt) {
      return { valid: false, payload, reason: "CREDENTIAL_EXPIRED" };
    }

    return { valid: true, payload };
  } catch {
    return { valid: false, reason: "DECODE_ERROR" };
  }
}
