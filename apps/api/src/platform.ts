import { randomUUID } from "node:crypto";

export type ErrorCode =
  | "NOT_FOUND"
  | "UNAUTHORIZED"
  | "FORBIDDEN"
  | "INVALID_REQUEST"
  | "INVALID_CREDENTIALS"
  | "INVALID_JSON"
  | "CARD_REGISTER_FAILED"
  | "RATE_LIMITED"
  | "PAYLOAD_TOO_LARGE"
  | "INTERNAL_ERROR";

export interface ApiErrorBody {
  error: { code: ErrorCode; message: string; requestId: string };
}

export const requestIdFor = (candidate: string | undefined): string =>
  candidate && /^[a-zA-Z0-9_-]{8,128}$/.test(candidate)
    ? candidate
    : randomUUID();

export const errorBody = (
  code: ErrorCode,
  message: string,
  requestId: string,
): ApiErrorBody => ({
  error: { code, message, requestId },
});

export const redactForLog = (value: unknown): unknown => {
  if (!value || typeof value !== "object") return value;
  if (Array.isArray(value)) return value.map(redactForLog);
  return Object.fromEntries(
    Object.entries(value as Record<string, unknown>).map(([key, entry]) => [
      key,
      /password|token|secret|authorization/i.test(key)
        ? "[REDACTED]"
        : redactForLog(entry),
    ]),
  );
};
