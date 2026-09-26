export interface ApiError {
  error: { code: string; message: string; requestId: string };
}

export interface HealthResponse {
  status: "ok";
}

export interface ReadyResponse {
  status: "ready";
  dependencies: Record<string, string>;
}

// Authentication
export interface LoginRequest {
  email: string;
  passwordHash: string;
}

export interface AuthTokenResponse {
  token: string;
  userId: string;
  email: string;
  role:
    | "STUDENT"
    | "CONDUCTOR"
    | "DRIVER"
    | "OPERATOR_ADMIN"
    | "UNIVERSITY_ADMIN"
    | "AUDITOR";
  institutionId: string;
  expiresAt: string;
}

// Student Data
export interface BalanceResponse {
  userId: string;
  balanceMinor: number;
  currency: string;
  updatedAt: string;
}

export interface JourneyDto {
  id: string;
  route: string;
  amountFormatted: string;
  dateFormatted: string;
  status: "CONFIRMED" | "PROVISIONAL" | "DECLINED";
  vehicleReg: string;
}

export interface JourneyListResponse {
  journeys: JourneyDto[];
}

// Transport Credentials
export interface CredentialResponse {
  credentialId: string;
  opaqueToken: string;
  expiresAt: string;
  ttlSeconds: number;
}

// Mobility Catalogue
export interface RouteDto {
  id: string;
  name: string;
  origin: string;
  destination: string;
  defaultFareMinor: number;
}

export interface VehicleDto {
  id: string;
  registration: string;
  operatorName: string;
  capacity: number;
  assignedRouteId: string;
}

export interface FareDto {
  id: string;
  routeId: string;
  amountMinor: number;
  currency: string;
}

// Conductor / Driver Validation
export interface ValidationRequest {
  opaqueToken: string;
  vehicleRegistration: string;
  routeId: string;
  fareMinor: number;
  isOfflineMode?: boolean;
}

export interface ValidationResponse {
  validationId: string;
  outcome: "CONFIRMED" | "PROVISIONAL" | "DECLINED";
  reason?: string;
  timestamp: string;
  fareMinor: number;
  currency: string;
}

export interface OfflineBatchItem {
  clientValidationId: string;
  opaqueToken: string;
  timestamp: string;
  deviceSequence: number;
}

export interface OfflineBatchRequest {
  vehicleRegistration: string;
  routeId: string;
  items: OfflineBatchItem[];
}

export interface OfflineBatchResponse {
  processed: number;
  accepted: number;
  rejected: number;
  reconciledAt: string;
}

// Physical NFC Card DTOs
export interface NfcCardDto {
  cardUid: string;
  userId: string;
  status: "ACTIVE" | "BLOCKED" | "EXPIRED";
  cardType: "MIFARE_DESFIRE" | "STUDENT_ID_NFC" | "GENERIC_NFC";
  issuedAt: string;
}

export interface NfcCardRegisterRequest {
  cardUid: string;
  userId: string;
  cardType?: "MIFARE_DESFIRE" | "STUDENT_ID_NFC" | "GENERIC_NFC";
}

export interface NfcCardRegisterResponse {
  card: NfcCardDto;
}

export interface NfcCardVerifyRequest {
  cardUid: string;
  vehicleRegistration: string;
  routeId: string;
  fareMinor: number;
  isOfflineMode?: boolean;
}

export interface NfcCardVerifyResponse {
  validationId: string;
  outcome: "CONFIRMED" | "PROVISIONAL" | "DECLINED";
  reason?: string;
  cardUid: string;
  userId?: string;
  timestamp: string;
  fareMinor: number;
  currency: string;
}
