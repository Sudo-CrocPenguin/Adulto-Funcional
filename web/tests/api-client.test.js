import assert from 'node:assert/strict';
import test from 'node:test';

import { ApiClient, ApiError, normalizeBaseUrl } from '../src/shared/api/ApiClient.js';

test('normalizeBaseUrl limpia espacios y slash final', () => {
  assert.equal(normalizeBaseUrl(' http://localhost:8080/ '), 'http://localhost:8080');
  assert.equal(normalizeBaseUrl(''), 'http://localhost:8080');
});

test('ApiClient envia cookies y marca cliente web', async () => {
  let capturedRequest = null;
  const client = new ApiClient({
    baseUrl: 'http://api.local/',
    fetcher: async (url, options) => {
      capturedRequest = { url, options };
      return new Response(JSON.stringify({
        status: 200,
        message: 'ok',
        data: { accountId: 'account-1' },
      }), { status: 200 });
    },
  });

  const response = await client.login({ email: 'user@test.com', password: 'secret' });

  assert.deepEqual(response, { accountId: 'account-1' });
  assert.equal(capturedRequest.url, 'http://api.local/api/auth/login');
  assert.equal(capturedRequest.options.credentials, 'include');
  assert.equal(capturedRequest.options.headers.get('X-Client-Type'), 'web');
  assert.equal(capturedRequest.options.headers.get('Content-Type'), 'application/json');
});

test('ApiClient propaga errores normalizados de la API', async () => {
  const client = new ApiClient({
    fetcher: async () => new Response(JSON.stringify({
      status: 409,
      message: 'Conflicto de datos',
      data: null,
    }), { status: 409 }),
  });

  await assert.rejects(
    () => client.listCategories(),
    (error) => error instanceof ApiError
      && error.status === 409
      && error.message === 'Conflicto de datos',
  );
});
