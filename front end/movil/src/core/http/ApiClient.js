import { ApiError } from './ApiError';

const JSON_CONTENT_TYPE = 'application/json';

export class ApiClient {
  constructor({ baseUrl, timeoutMs = 15_000, fetchImplementation = fetch }) {
    this.baseUrl = baseUrl;
    this.timeoutMs = timeoutMs;
    this.fetchImplementation = fetchImplementation;
  }

  async post(path, body, options = {}) {
    return this.request(path, { ...options, method: 'POST', body });
  }

  async get(path, options = {}) {
    return this.request(path, { ...options, method: 'GET' });
  }

  async getPage(path, options = {}) {
    return this.request(path, {
      ...options,
      includePage: true,
      method: 'GET',
    });
  }

  async patch(path, body, options = {}) {
    return this.request(path, { ...options, method: 'PATCH', body });
  }

  async delete(path, options = {}) {
    return this.request(path, { ...options, method: 'DELETE' });
  }

  async request(
    path,
    {
      method = 'GET',
      body,
      headers = {},
      includePage = false,
    } = {},
  ) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.timeoutMs);

    try {
      const response = await this.fetchImplementation(`${this.baseUrl}${path}`, {
        method,
        signal: controller.signal,
        headers: {
          Accept: JSON_CONTENT_TYPE,
          'X-Client-Type': 'mobile',
          'User-Agent': 'MiApp/0.1 AdultoFuncionalMobile',
          ...(body === undefined ? {} : { 'Content-Type': JSON_CONTENT_TYPE }),
          ...headers,
        },
        body: body === undefined ? undefined : JSON.stringify(body),
      });

      const payload = await this.readPayload(response);

      if (!response.ok) {
        throw ApiError.fromResponse(payload, response.status);
      }

      if (includePage) {
        return {
          items: Array.isArray(payload?.data) ? payload.data : [],
          page: payload?.page ?? null,
        };
      }

      return payload?.data ?? null;
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }

      if (error?.name === 'AbortError') {
        throw new ApiError({
          code: 'REQUEST_TIMEOUT',
          message: 'El servidor tardó demasiado en responder.',
          cause: error,
        });
      }

      throw new ApiError({
        code: 'NETWORK_ERROR',
        message: 'No pudimos conectar con el servidor. Revisa tu conexión.',
        cause: error,
      });
    } finally {
      clearTimeout(timeout);
    }
  }

  async readPayload(response) {
    const text = await response.text();

    if (!text) {
      return null;
    }

    try {
      return JSON.parse(text);
    } catch (error) {
      throw new ApiError({
        status: response.status,
        code: 'INVALID_SERVER_RESPONSE',
        message: 'El servidor devolvió una respuesta que no pudimos interpretar.',
        cause: error,
      });
    }
  }
}
