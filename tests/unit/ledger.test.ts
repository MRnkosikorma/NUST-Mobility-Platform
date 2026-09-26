import { test } from "node:test";
import assert from "node:assert/strict";
import {
  addMoney,
  formatMoney,
  money,
  subtractMoney,
} from "../../packages/domain/src/money.js";

test("money formatting formats minor units to standard major string", () => {
  const m = money(150, "USD");
  assert.equal(formatMoney(m), "USD 1.50");
});

test("addMoney adds identical currency minor units", () => {
  const m1 = money(200, "USD");
  const m2 = money(350, "USD");
  const sum = addMoney(m1, m2);
  assert.equal(sum.amountMinor, 550);
  assert.equal(sum.currency, "USD");
});

test("subtractMoney debits safely and throws on insufficient funds", () => {
  const balance = money(500, "USD");
  const fare = money(100, "USD");
  const remaining = subtractMoney(balance, fare);
  assert.equal(remaining.amountMinor, 400);

  assert.throws(() => {
    subtractMoney(money(50, "USD"), money(100, "USD"));
  }, /Insufficient funds/);
});
