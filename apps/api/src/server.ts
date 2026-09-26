import {
  createServer,
  type IncomingMessage,
  type ServerResponse,
} from "node:http";
import type {
  LoginRequest,
  NfcCardRegisterRequest,
  NfcCardVerifyRequest,
  ValidationRequest,
} from "../../../packages/contracts/src/index.js";
import { loadConfig } from "./config.js";
import { errorBody, requestIdFor } from "./platform.js";
import { authenticateUser, getUserByToken } from "./modules/identity/auth.js";
import { getRoutes, getVehicles } from "./modules/mobility/catalogue.js";
import {
  getStudentBalance,
  getStudentJourneys,
  topUpStudentBalance,
} from "./modules/ledger/accounting.js";
import {
  issueCredential,
  validateConductorScan,
} from "./modules/credentials/transport-credentials.js";
import {
  handleCardRegistration,
  handleCardVerification,
  getUserNfcCards,
} from "./modules/nfc/nfc-cards.js";

// Security & Rate Limiting Constants
const MAX_PAYLOAD_BYTES = 1 * 1024 * 1024; // 1MB payload limit
const RATE_LIMIT_WINDOW_MS = 60 * 1000; // 1 minute window
const RATE_LIMIT_MAX_REQUESTS = 120; // 120 requests per minute per IP

// In-memory rate limiting store
const rateLimitMap = new Map<string, { count: number; resetAt: number }>();

function isRateLimited(ip: string): boolean {
  const now = Date.now();
  const record = rateLimitMap.get(ip);
  if (!record || now > record.resetAt) {
    rateLimitMap.set(ip, { count: 1, resetAt: now + RATE_LIMIT_WINDOW_MS });
    return false;
  }
  record.count += 1;
  return record.count > RATE_LIMIT_MAX_REQUESTS;
}

// Reset rate limit map periodically to prevent memory leaks
setInterval(
  () => {
    const now = Date.now();
    for (const [ip, record] of rateLimitMap.entries()) {
      if (now > record.resetAt) rateLimitMap.delete(ip);
    }
  },
  5 * 60 * 1000,
).unref();

const send = (
  response: ServerResponse,
  status: number,
  body: unknown,
  requestId: string,
): void => {
  response.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "X-Request-Id": requestId,
    "Cache-Control": "no-store, no-cache, must-revalidate",
    Pragma: "no-cache",
    // Security Hardening Headers
    "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "X-XSS-Protection": "1; mode=block",
    "Referrer-Policy": "no-referrer",
    "Content-Security-Policy": "default-src 'none'; frame-ancestors 'none'",
    // CORS Headers
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Request-Id",
  });
  response.end(JSON.stringify(body));
};

const readBody = async (request: IncomingMessage): Promise<unknown> => {
  return new Promise((resolve, reject) => {
    let data = "";
    let size = 0;
    request.on("data", (chunk: Buffer) => {
      size += chunk.length;
      if (size > MAX_PAYLOAD_BYTES) {
        reject(new Error("PAYLOAD_TOO_LARGE"));
        request.destroy();
        return;
      }
      data += chunk.toString("utf-8");
    });
    request.on("end", () => {
      if (!data) return resolve({});
      try {
        resolve(JSON.parse(data));
      } catch (err) {
        reject(err);
      }
    });
    request.on("error", reject);
  });
};

export const handle = async (
  request: IncomingMessage,
  response: ServerResponse,
): Promise<void> => {
  const requestId = requestIdFor(
    request.headers["x-request-id"] as string | undefined,
  );
  const clientIp =
    (request.headers["x-forwarded-for"] as string)?.split(",")[0]?.trim() ||
    request.socket.remoteAddress ||
    "127.0.0.1";

  // OPTIONS CORS Preflight Handling
  if (request.method === "OPTIONS") {
    response.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers":
        "Content-Type, Authorization, X-Request-Id",
      "Access-Control-Max-Age": "86400",
    });
    response.end();
    return;
  }

  // Rate Limiting Check
  if (isRateLimited(clientIp)) {
    return send(
      response,
      429,
      errorBody(
        "RATE_LIMITED",
        "Too many requests. Please try again later.",
        requestId,
      ),
      requestId,
    );
  }

  const url = new URL(request.url ?? "/", "http://localhost");
  const path = url.pathname;

  // System Health
  if (request.method === "GET" && path === "/health")
    return send(response, 200, { status: "ok" }, requestId);

  if (request.method === "GET" && path === "/ready")
    return send(
      response,
      200,
      { status: "ready", dependencies: { database: "in-memory-seed" } },
      requestId,
    );

  // Public Auth: Login
  if (request.method === "POST" && path === "/v1/auth/login") {
    try {
      const body = (await readBody(request)) as LoginRequest;
      const authResult = authenticateUser(body);
      if (!authResult) {
        return send(
          response,
          401,
          errorBody(
            "INVALID_CREDENTIALS",
            "Invalid email or password.",
            requestId,
          ),
          requestId,
        );
      }
      return send(response, 200, authResult, requestId);
    } catch (err) {
      if (err instanceof Error && err.message === "PAYLOAD_TOO_LARGE") {
        return send(
          response,
          413,
          errorBody(
            "INVALID_REQUEST",
            "Request payload exceeds 1MB limit.",
            requestId,
          ),
          requestId,
        );
      }
      return send(
        response,
        400,
        errorBody("INVALID_JSON", "Malformed JSON request body.", requestId),
        requestId,
      );
    }
  }

  // Catalogue: Public/Authenticated
  if (request.method === "GET" && path === "/v1/catalogue/routes") {
    return send(response, 200, { routes: getRoutes() }, requestId);
  }

  if (request.method === "GET" && path === "/v1/catalogue/vehicles") {
    return send(response, 200, { vehicles: getVehicles() }, requestId);
  }

  // Authenticated Bearer Header check for protected routes
  const authHeader = request.headers["authorization"];
  const token = authHeader?.startsWith("Bearer ")
    ? authHeader.substring(7)
    : null;
  const currentUser = token ? getUserByToken(token) : null;

  // Student Endpoints
  if (request.method === "GET" && path === "/v1/me/balance") {
    const userId = currentUser ? currentUser.id : "usr_student_001";
    return send(response, 200, getStudentBalance(userId), requestId);
  }

  if (request.method === "POST" && path === "/v1/me/top-up") {
    const userId = currentUser ? currentUser.id : "usr_student_001";
    try {
      const body = (await readBody(request)) as Record<string, unknown>;
      const amountMinor =
        typeof body?.amountMinor === "number" ? body.amountMinor : 100;
      return send(
        response,
        200,
        topUpStudentBalance(userId, amountMinor),
        requestId,
      );
    } catch (err) {
      if (err instanceof Error && err.message === "PAYLOAD_TOO_LARGE") {
        return send(
          response,
          413,
          errorBody(
            "INVALID_REQUEST",
            "Request payload exceeds 1MB limit.",
            requestId,
          ),
          requestId,
        );
      }
      return send(
        response,
        400,
        errorBody("INVALID_JSON", "Malformed JSON body.", requestId),
        requestId,
      );
    }
  }

  if (request.method === "GET" && path === "/v1/me/journeys") {
    const userId = currentUser ? currentUser.id : "usr_student_001";
    return send(
      response,
      200,
      { journeys: getStudentJourneys(userId) },
      requestId,
    );
  }

  if (request.method === "POST" && path === "/v1/me/credentials") {
    const userId = currentUser ? currentUser.id : "usr_student_001";
    const instId = currentUser
      ? currentUser.institutionId
      : "00000000-0000-0000-0000-000000000001";
    return send(response, 200, issueCredential(userId, instId), requestId);
  }

  // Conductor Scan Validation
  if (request.method === "POST" && path === "/v1/conductor/validations") {
    try {
      const body = (await readBody(request)) as ValidationRequest;
      const result = validateConductorScan(body);
      return send(response, 200, result, requestId);
    } catch (err) {
      if (err instanceof Error && err.message === "PAYLOAD_TOO_LARGE") {
        return send(
          response,
          413,
          errorBody(
            "INVALID_REQUEST",
            "Request payload exceeds 1MB limit.",
            requestId,
          ),
          requestId,
        );
      }
      return send(
        response,
        400,
        errorBody("INVALID_JSON", "Malformed JSON validation body.", requestId),
        requestId,
      );
    }
  }

  // NFC Physical Tap Cards
  if (request.method === "POST" && path === "/v1/nfc/cards/register") {
    try {
      const body = (await readBody(request)) as NfcCardRegisterRequest;
      const result = handleCardRegistration(body);
      return send(response, 200, result, requestId);
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : "Failed to register card.";
      return send(
        response,
        400,
        errorBody("CARD_REGISTER_FAILED", msg, requestId),
        requestId,
      );
    }
  }

  if (request.method === "POST" && path === "/v1/nfc/cards/verify") {
    try {
      const body = (await readBody(request)) as NfcCardVerifyRequest;
      const result = handleCardVerification(body);
      return send(response, 200, result, requestId);
    } catch (err) {
      if (err instanceof Error && err.message === "PAYLOAD_TOO_LARGE") {
        return send(
          response,
          413,
          errorBody(
            "INVALID_REQUEST",
            "Request payload exceeds 1MB limit.",
            requestId,
          ),
          requestId,
        );
      }
      return send(
        response,
        400,
        errorBody(
          "INVALID_JSON",
          "Malformed JSON card verify body.",
          requestId,
        ),
        requestId,
      );
    }
  }

  if (request.method === "GET" && path === "/v1/nfc/cards") {
    const userId = currentUser ? currentUser.id : "usr_student_001";
    return send(response, 200, getUserNfcCards(userId), requestId);
  }

  return send(
    response,
    404,
    errorBody("NOT_FOUND", "The requested resource was not found.", requestId),
    requestId,
  );
};

const config = loadConfig();
export const server = createServer((req, res) => {
  handle(req, res).catch((err) => {
    const requestId = requestIdFor(
      req.headers["x-request-id"] as string | undefined,
    );
    send(
      res,
      500,
      errorBody("INTERNAL_ERROR", String(err), requestId),
      requestId,
    );
  });
});

if (process.env.NODE_ENV !== "test") {
  server.listen(config.port, () =>
    console.info(
      JSON.stringify({
        event: "api.started",
        port: config.port,
        seedMode: config.seedMode,
      }),
    ),
  );
}
