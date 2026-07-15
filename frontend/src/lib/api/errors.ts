/**
 * A failed backend call. The backend always answers with the same envelope
 * (Ref: SRS 2.8), so `message` is a sentence written to be shown to the
 * user, and `fieldErrors` maps 1:1 onto form fields for validation failures.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }

  /** True when the caller is not allowed to do this - as opposed to merely not logged in. */
  get isForbidden() {
    return this.status === 403;
  }

  get isUnauthorized() {
    return this.status === 401;
  }

  /** Ref: SRS 16.12 - the whole system is in maintenance mode. */
  get isMaintenance() {
    return this.status === 503;
  }
}
