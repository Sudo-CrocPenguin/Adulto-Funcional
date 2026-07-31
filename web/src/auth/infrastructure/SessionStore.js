const API_BASE_URL_KEY = 'adulto-funcional.web.api-base-url';
const ACCOUNT_KEY = 'adulto-funcional.web.account';
const DEFAULT_API_BASE_URL = 'http://localhost:8080';

export class SessionStore {
  constructor(storage = globalThis.localStorage) {
    this.storage = storage;
  }

  getApiBaseUrl() {
    return this.storage?.getItem(API_BASE_URL_KEY) ?? DEFAULT_API_BASE_URL;
  }

  saveApiBaseUrl(apiBaseUrl) {
    this.storage?.setItem(API_BASE_URL_KEY, normalizeStoredUrl(apiBaseUrl));
  }

  getAccount() {
    const rawAccount = this.storage?.getItem(ACCOUNT_KEY);
    if (!rawAccount) {
      return null;
    }

    try {
      return JSON.parse(rawAccount);
    } catch {
      this.clearAccount();
      return null;
    }
  }

  saveAccount(account) {
    this.storage?.setItem(ACCOUNT_KEY, JSON.stringify(account));
  }

  clearAccount() {
    this.storage?.removeItem(ACCOUNT_KEY);
  }
}

export function normalizeStoredUrl(apiBaseUrl) {
  const trimmed = String(apiBaseUrl ?? '').trim();
  return (trimmed || DEFAULT_API_BASE_URL).replace(/\/+$/, '');
}
