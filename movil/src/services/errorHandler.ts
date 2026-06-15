/**
 * Utilidades para normalizar errores de red/API antes de mostrarlos en UI.
 *
 * El backend responde con ApiResponse y, en validaciones, puede enviar
 * `data` como mapa campo -> mensaje. Axios tambien puede producir errores sin
 * `response` cuando no hay conexion. Este helper evita textos vacios o
 * valores `undefined`.
 *
 * @author Miguel Angel Blandon Montes
 */

type ApiErrorPayload = {
  message?: unknown;
  data?: unknown;
};

type HttpError = {
  response?: {
    data?: ApiErrorPayload;
  };
  message?: unknown;
};

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null;

const asCleanString = (value: unknown): string | null => {
  if (typeof value !== 'string') return null;

  const clean = value.trim();
  return clean && clean !== 'undefined' && clean !== 'null' ? clean : null;
};

const validationMessages = (value: unknown): string | null => {
  if (!isRecord(value)) return asCleanString(value);

  const messages = Object.values(value)
    .map(asCleanString)
    .filter((message): message is string => Boolean(message));

  return messages.length ? messages.join('\n') : null;
};

export const getApiErrorMessage = (
  error: unknown,
  fallback = 'No se pudo completar la operación',
): string => {
  if (typeof error === 'string') {
    return asCleanString(error) ?? fallback;
  }

  if (error instanceof Error) {
    const httpError = error as Error & HttpError;
    const responseData = httpError.response?.data;
    const responseMessage = asCleanString(responseData?.message);
    const fieldMessages = validationMessages(responseData?.data);
    const nativeMessage = asCleanString(error.message);

    return responseMessage ?? fieldMessages ?? nativeMessage ?? fallback;
  }

  if (isRecord(error)) {
    const httpError = error as HttpError;
    const responseData = httpError.response?.data;
    const responseMessage = asCleanString(responseData?.message);
    const fieldMessages = validationMessages(responseData?.data);
    const nativeMessage = asCleanString(httpError.message);

    return responseMessage ?? fieldMessages ?? nativeMessage ?? fallback;
  }

  return fallback;
};
