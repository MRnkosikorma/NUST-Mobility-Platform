export interface NfcCard {
  cardUid: string;
  userId: string;
  status: "ACTIVE" | "BLOCKED" | "EXPIRED";
  cardType: "MIFARE_DESFIRE" | "STUDENT_ID_NFC" | "GENERIC_NFC";
  issuedAt: string;
}

// In-memory mock store for pilot development
const mockCardStore = new Map<string, NfcCard>([
  [
    "04A28B3C901280",
    {
      cardUid: "04A28B3C901280",
      userId: "usr_student_001",
      status: "ACTIVE",
      cardType: "STUDENT_ID_NFC",
      issuedAt: new Date("2026-01-15").toISOString(),
    },
  ],
  [
    "11223344556677",
    {
      cardUid: "11223344556677",
      userId: "usr_student_002",
      status: "BLOCKED",
      cardType: "MIFARE_DESFIRE",
      issuedAt: new Date("2026-02-01").toISOString(),
    },
  ],
]);

export function normalizeCardUid(rawUid: string): string {
  if (!rawUid) return "";
  return rawUid
    .replace(/^card_uid:/i, "")
    .replace(/[:\s-]/g, "")
    .toUpperCase();
}

export function registerNfcCard(
  rawUid: string,
  userId: string,
  cardType:
    "MIFARE_DESFIRE" | "STUDENT_ID_NFC" | "GENERIC_NFC" = "STUDENT_ID_NFC",
): NfcCard {
  const cardUid = normalizeCardUid(rawUid);
  if (!cardUid) {
    throw new Error("INVALID_CARD_UID");
  }

  if (mockCardStore.has(cardUid)) {
    const existing = mockCardStore.get(cardUid)!;
    if (existing.userId !== userId) {
      throw new Error("CARD_ALREADY_REGISTERED_TO_ANOTHER_USER");
    }
    return existing;
  }

  const card: NfcCard = {
    cardUid,
    userId,
    status: "ACTIVE",
    cardType,
    issuedAt: new Date().toISOString(),
  };

  mockCardStore.set(cardUid, card);
  return card;
}

export function verifyNfcCard(rawUid: string): {
  valid: boolean;
  card?: NfcCard;
  reason?: string;
} {
  const cardUid = normalizeCardUid(rawUid);
  if (!cardUid) {
    return { valid: false, reason: "INVALID_CARD_UID" };
  }

  const card = mockCardStore.get(cardUid);
  if (!card) {
    return { valid: false, reason: "CARD_NOT_FOUND" };
  }

  if (card.status === "BLOCKED") {
    return { valid: false, card, reason: "CARD_BLOCKED" };
  }

  if (card.status === "EXPIRED") {
    return { valid: false, card, reason: "CARD_EXPIRED" };
  }

  return { valid: true, card };
}

export function getCardsByUserId(userId: string): NfcCard[] {
  return Array.from(mockCardStore.values()).filter(
    (card) => card.userId === userId,
  );
}

export function convertCardUidToCredentialToken(rawUid: string): string {
  const cardUid = normalizeCardUid(rawUid);
  return `card_uid:${cardUid}`;
}
