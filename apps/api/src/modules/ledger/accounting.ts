import type {
  BalanceResponse,
  JourneyDto,
} from "../../../../../packages/contracts/src/index.js";
import {
  formatMoney,
  money,
} from "../../../../../packages/domain/src/index.js";

interface StudentAccountState {
  userId: string;
  balanceMinor: number;
  currency: string;
  updatedAt: string;
  journeys: JourneyDto[];
}

const studentLedgers: Map<string, StudentAccountState> = new Map([
  [
    "usr_student_001",
    {
      userId: "usr_student_001",
      balanceMinor: 500, // $5.00
      currency: "USD",
      updatedAt: new Date().toISOString(),
      journeys: [
        {
          id: "jrn_001",
          route: "NUST Main Campus — Bulawayo CBD",
          amountFormatted: "USD 1.00",
          dateFormatted: "Today · 08:12",
          status: "CONFIRMED",
          vehicleReg: "NUST-BUS-01",
        },
        {
          id: "jrn_002",
          route: "NUST Main Campus — Selbourne Park",
          amountFormatted: "USD 0.50",
          dateFormatted: "Yesterday · 16:45",
          status: "CONFIRMED",
          vehicleReg: "NUST-BUS-02",
        },
      ],
    },
  ],
]);

export function getStudentBalance(userId: string): BalanceResponse {
  const ledger = getOrCreateLedger(userId);
  return {
    userId: ledger.userId,
    balanceMinor: ledger.balanceMinor,
    currency: ledger.currency,
    updatedAt: ledger.updatedAt,
  };
}

export function topUpStudentBalance(
  userId: string,
  amountMinor: number,
): BalanceResponse {
  const ledger = getOrCreateLedger(userId);
  ledger.balanceMinor += amountMinor;
  ledger.updatedAt = new Date().toISOString();
  return {
    userId: ledger.userId,
    balanceMinor: ledger.balanceMinor,
    currency: ledger.currency,
    updatedAt: ledger.updatedAt,
  };
}

export function recordJourneyDebit(
  userId: string,
  fareMinor: number,
  routeName: string,
  vehicleReg: string,
  isOffline: boolean = false,
): { success: boolean; journey?: JourneyDto; errorReason?: string } {
  const ledger = getOrCreateLedger(userId);

  if (ledger.balanceMinor < fareMinor) {
    return { success: false, errorReason: "INSUFFICIENT_FUNDS" };
  }

  ledger.balanceMinor -= fareMinor;
  ledger.updatedAt = new Date().toISOString();

  const formattedAmount = formatMoney(
    money(fareMinor, ledger.currency),
  ).toString();
  const journey: JourneyDto = {
    id: `jrn_${Date.now()}`,
    route: routeName,
    amountFormatted: formattedAmount,
    dateFormatted: "Just now",
    status: isOffline ? "PROVISIONAL" : "CONFIRMED",
    vehicleReg,
  };

  ledger.journeys.unshift(journey);

  return { success: true, journey };
}

export function getStudentJourneys(userId: string): JourneyDto[] {
  const ledger = getOrCreateLedger(userId);
  return ledger.journeys;
}

function getOrCreateLedger(userId: string): StudentAccountState {
  let ledger = studentLedgers.get(userId);
  if (!ledger) {
    ledger = {
      userId,
      balanceMinor: 500,
      currency: "USD",
      updatedAt: new Date().toISOString(),
      journeys: [],
    };
    studentLedgers.set(userId, ledger);
  }
  return ledger;
}
