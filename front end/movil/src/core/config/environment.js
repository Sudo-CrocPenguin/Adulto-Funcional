const DEFAULT_API_URL = 'http://localhost:8080';

function normalizeApiUrl(value) {
  const normalized = String(value ?? '').trim().replace(/\/+$/, '');

  if (!/^https?:\/\/[^\s]+$/i.test(normalized)) {
    throw new Error(
      'EXPO_PUBLIC_API_URL debe ser una URL HTTP valida para el backend.',
    );
  }

  return normalized;
}

export const environment = Object.freeze({
  apiUrl: normalizeApiUrl(
    process.env.EXPO_PUBLIC_API_URL || DEFAULT_API_URL,
  ),
  requestTimeoutMs: 15_000,
});

