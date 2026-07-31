export class ApiError extends Error {
  constructor(status, message, payload = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}

export class ApiClient {
  constructor({ baseUrl = 'http://localhost:8080', fetcher = globalThis.fetch } = {}) {
    if (typeof fetcher !== 'function') {
      throw new TypeError('ApiClient requiere una implementacion de fetch');
    }

    this.baseUrl = normalizeBaseUrl(baseUrl);
    this.fetcher = fetcher;
  }

  setBaseUrl(baseUrl) {
    this.baseUrl = normalizeBaseUrl(baseUrl);
  }

  login(payload) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  register(payload) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async logout() {
    await this.request('/api/auth/logout', { method: 'POST' });
  }

  listCategories() {
    return this.request('/api/finances/categories');
  }

  listMovements() {
    return this.request('/api/finances/movements');
  }

  listEvents() {
    return this.request('/api/agenda/events');
  }

  listFixedExpenses() {
    return this.request('/api/finances/fixed-expenses');
  }

  verifyMasterKey(masterKey) {
    return this.request('/api/security/passwords/master-key/verify', {
      method: 'POST',
      body: JSON.stringify({ masterKey }),
    });
  }

  listPasswords() {
    return this.request('/api/security/passwords');
  }

  async request(path, options = {}) {
    const headers = new Headers(options.headers);
    headers.set('Accept', 'application/json');
    headers.set('X-Client-Type', 'web');

    if (options.body !== undefined && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    const response = await this.fetcher(`${this.baseUrl}${path}`, {
      ...options,
      headers,
      credentials: 'include',
    });
    const payload = await parseResponse(response);

    if (!response.ok) {
      throw new ApiError(response.status, payload?.message ?? `Error HTTP ${response.status}`, payload);
    }

    return payload?.data ?? null;
  }
}

export function normalizeBaseUrl(baseUrl) {
  const trimmed = String(baseUrl ?? '').trim();
  return (trimmed || 'http://localhost:8080').replace(/\/+$/, '');
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return {
      status: response.status,
      message: text,
      data: null,
    };
  }
}
