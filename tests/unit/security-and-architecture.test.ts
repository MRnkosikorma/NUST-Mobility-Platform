import { test } from "node:test";
import assert from "node:assert/strict";
import { EventEmitter } from "node:events";
import type { IncomingMessage, ServerResponse } from "node:http";
import { handle } from "../../apps/api/src/server.js";

async function makeRequest(
  method: string,
  url: string,
  headers: Record<string, string> = {},
  body: string | Buffer | null = null,
): Promise<{ status: number; headers: Record<string, string>; body: unknown }> {
  return new Promise((resolve, reject) => {
    const req = new EventEmitter() as unknown as IncomingMessage;
    req.method = method;
    req.url = url;
    req.headers = headers;
    req.socket = {
      remoteAddress: "127.0.0.1",
    } as unknown as IncomingMessage["socket"];

    const resHeaders: Record<string, string> = {};
    let statusCode = 200;
    let responseData = "";

    const res = new EventEmitter() as unknown as ServerResponse;
    res.writeHead = (status: number, headersArgs?: unknown) => {
      statusCode = status;
      if (headersArgs && typeof headersArgs === "object") {
        for (const [k, v] of Object.entries(
          headersArgs as Record<string, unknown>,
        )) {
          resHeaders[k.toLowerCase()] = String(v);
        }
      }
      return res;
    };
    res.end = (chunk?: unknown) => {
      if (chunk) responseData += String(chunk);
      let parsed: unknown = responseData;
      try {
        parsed = JSON.parse(responseData);
      } catch {
        // Keep raw string if not JSON
      }
      resolve({ status: statusCode, headers: resHeaders, body: parsed });
      return res;
    };

    handle(req, res).catch(reject);

    if (body) {
      req.emit(
        "data",
        typeof body === "string" ? Buffer.from(body, "utf-8") : body,
      );
    }
    req.emit("end");
  });
}

// ══════════════════════════════════════════════════════════════════════════════
//  SYSTEM-WIDE SECURITY & ARCHITECTURE TESTS
// ══════════════════════════════════════════════════════════════════════════════

test("Security Control 1: Response headers include strict HTTP security controls", async () => {
  const result = await makeRequest("GET", "/health");

  assert.equal(result.status, 200);
  assert.equal(result.headers["x-content-type-options"], "nosniff");
  assert.equal(result.headers["x-frame-options"], "DENY");
  assert.equal(
    result.headers["strict-transport-security"],
    "max-age=31536000; includeSubDomains",
  );
  assert.equal(result.headers["referrer-policy"], "no-referrer");
  assert.equal(
    result.headers["content-security-policy"],
    "default-src 'none'; frame-ancestors 'none'",
  );
  assert.ok(result.headers["x-request-id"]);
});

test("Security Control 2: CORS OPTIONS preflight handles origin preflight correctly", async () => {
  const result = await makeRequest("OPTIONS", "/v1/auth/login", {
    origin: "https://campus.nust.ac.zw",
  });

  assert.equal(result.status, 204);
  assert.equal(result.headers["access-control-allow-origin"], "*");
  assert.equal(
    result.headers["access-control-allow-methods"],
    "GET, POST, OPTIONS",
  );
});

test("Security Control 3: Authentication rejects invalid credentials safely with 401", async () => {
  const result = await makeRequest(
    "POST",
    "/v1/auth/login",
    { "content-type": "application/json" },
    JSON.stringify({
      email: "invalid@nust.ac.zw",
      passwordHash: "wrong_password",
    }),
  );

  assert.equal(result.status, 401);
  const body = result.body as { error: { code: string; requestId: string } };
  assert.equal(body.error.code, "INVALID_CREDENTIALS");
  assert.ok(body.error.requestId);
});

test("Security Control 4: Malformed JSON request payload is caught safely", async () => {
  const result = await makeRequest(
    "POST",
    "/v1/auth/login",
    { "content-type": "application/json" },
    "{ invalid_json: ",
  );

  assert.equal(result.status, 400);
  const body = result.body as { error: { code: string } };
  assert.equal(body.error.code, "INVALID_JSON");
});

test("Architecture Check: System health & readiness probes respond OK", async () => {
  const healthRes = await makeRequest("GET", "/health");
  assert.equal(healthRes.status, 200);
  assert.equal((healthRes.body as { status: string }).status, "ok");

  const readyRes = await makeRequest("GET", "/ready");
  assert.equal(readyRes.status, 200);
  assert.equal((readyRes.body as { status: string }).status, "ready");
});
