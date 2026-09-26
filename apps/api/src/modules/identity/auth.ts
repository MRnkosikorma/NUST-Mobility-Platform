import { createHash, randomBytes } from "node:crypto";
import type {
  AuthTokenResponse,
  LoginRequest,
} from "../../../../../packages/contracts/src/index.js";

export interface UserAccount {
  id: string;
  institutionId: string;
  email: string;
  passwordHash: string;
  fullName: string;
  role:
    | "STUDENT"
    | "CONDUCTOR"
    | "DRIVER"
    | "OPERATOR_ADMIN"
    | "UNIVERSITY_ADMIN"
    | "AUDITOR";
}

export interface Session {
  token: string;
  userId: string;
  expiresAt: string;
}

/** In-memory store for development seed users and active sessions. */
const DEFAULT_INSTITUTION_ID = "00000000-0000-0000-0000-000000000001";

const devUsers: Map<string, UserAccount> = new Map([
  [
    "student@nust.ac.zw",
    {
      id: "usr_student_001",
      institutionId: DEFAULT_INSTITUTION_ID,
      email: "student@nust.ac.zw",
      passwordHash: hashPassword("StudentPass123!"),
      fullName: "Sipho Ndlovu",
      role: "STUDENT",
    },
  ],
  [
    "conductor@nust.ac.zw",
    {
      id: "usr_conductor_001",
      institutionId: DEFAULT_INSTITUTION_ID,
      email: "conductor@nust.ac.zw",
      passwordHash: hashPassword("ConductorPass123!"),
      fullName: "Tinashe Moyo",
      role: "CONDUCTOR",
    },
  ],
  [
    "driver@nust.ac.zw",
    {
      id: "usr_driver_001",
      institutionId: DEFAULT_INSTITUTION_ID,
      email: "driver@nust.ac.zw",
      passwordHash: hashPassword("DriverPass123!"),
      fullName: "Blessing Mpofu",
      role: "DRIVER",
    },
  ],
]);

const activeSessions: Map<string, Session> = new Map();

export function hashPassword(password: string): string {
  return createHash("sha256").update(`nust_salt_${password}`).digest("hex");
}

export function authenticateUser(req: LoginRequest): AuthTokenResponse | null {
  const user = devUsers.get(req.email.toLowerCase());
  if (!user) return null;

  if (
    user.passwordHash !== req.passwordHash &&
    user.passwordHash !== hashPassword(req.passwordHash)
  ) {
    return null;
  }

  const token = `bearer_${randomBytes(16).toString("hex")}`;
  const expiresAt = new Date(Date.now() + 24 * 3600 * 1000).toISOString();

  activeSessions.set(token, {
    token,
    userId: user.id,
    expiresAt,
  });

  return {
    token,
    userId: user.id,
    email: user.email,
    role: user.role,
    institutionId: user.institutionId,
    expiresAt,
  };
}

export function getUserByToken(token: string): UserAccount | null {
  const session = activeSessions.get(token);
  if (!session) return null;

  if (new Date(session.expiresAt) < new Date()) {
    activeSessions.delete(token);
    return null;
  }

  for (const user of devUsers.values()) {
    if (user.id === session.userId) return user;
  }
  return null;
}

export function registerUser(user: UserAccount): UserAccount {
  devUsers.set(user.email.toLowerCase(), user);
  return user;
}
