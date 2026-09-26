export interface AuditEvent {
  id: string;
  institutionId: string;
  actorId: string;
  eventType: string;
  targetType: string;
  targetId: string;
  requestId: string;
  occurredAt: string;
  before?: Record<string, unknown>;
  after?: Record<string, unknown>;
}

/** Development-only in-memory stand-in for an append-only audit table. */
export class AuditLog {
  readonly #events: AuditEvent[] = [];

  append(event: AuditEvent): void {
    this.#events.push(Object.freeze({ ...event }));
  }

  forInstitution(institutionId: string): readonly AuditEvent[] {
    return this.#events.filter(
      (event) => event.institutionId === institutionId,
    );
  }
}
