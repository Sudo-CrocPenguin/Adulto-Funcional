export class ApiError extends Error {
  constructor({
    message,
    status = 0,
    code = 'UNEXPECTED_ERROR',
    fieldErrors = [],
    traceId = null,
    cause,
  }) {
    super(message, cause ? { cause } : undefined);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.fieldErrors = Array.isArray(fieldErrors) ? fieldErrors : [];
    this.traceId = traceId;
  }

  fieldMessage(fieldName) {
    return this.fieldErrors.find(({ field }) => field === fieldName)?.message;
  }

  static fromResponse(payload, status) {
    return new ApiError({
      status,
      code: payload?.code || `HTTP_${status}`,
      message: payload?.message || 'No fue posible completar la solicitud.',
      fieldErrors: payload?.fieldErrors,
      traceId: payload?.traceId,
    });
  }
}

