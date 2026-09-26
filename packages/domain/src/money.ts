export interface Money {
  amountMinor: number;
  currency: string;
}

export const money = (amountMinor: number, currency: string): Money => {
  if (!Number.isSafeInteger(amountMinor))
    throw new Error("Money must use safe integer minor units");
  if (!/^[A-Z]{3}$/.test(currency))
    throw new Error("Currency must be an ISO 4217 code");
  return Object.freeze({ amountMinor, currency });
};

export const formatMoney = (m: Money): string => {
  const major = (m.amountMinor / 100).toFixed(2);
  return `${m.currency} ${major}`;
};

export const addMoney = (a: Money, b: Money): Money => {
  if (a.currency !== b.currency) {
    throw new Error(`Currency mismatch: ${a.currency} vs ${b.currency}`);
  }
  return money(a.amountMinor + b.amountMinor, a.currency);
};

export const subtractMoney = (a: Money, b: Money): Money => {
  if (a.currency !== b.currency) {
    throw new Error(`Currency mismatch: ${a.currency} vs ${b.currency}`);
  }
  if (a.amountMinor < b.amountMinor) {
    throw new Error("Insufficient funds for debit operation");
  }
  return money(a.amountMinor - b.amountMinor, a.currency);
};

export const hasSufficientBalance = (balance: Money, cost: Money): boolean => {
  if (balance.currency !== cost.currency) return false;
  return balance.amountMinor >= cost.amountMinor;
};
