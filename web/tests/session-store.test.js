import assert from 'node:assert/strict';
import test from 'node:test';

import { SessionStore, normalizeStoredUrl } from '../src/auth/infrastructure/SessionStore.js';

class MemoryStorage {
  constructor() {
    this.values = new Map();
  }

  getItem(key) {
    return this.values.get(key) ?? null;
  }

  setItem(key, value) {
    this.values.set(key, value);
  }

  removeItem(key) {
    this.values.delete(key);
  }
}

test('normalizeStoredUrl retorna URL local por defecto', () => {
  assert.equal(normalizeStoredUrl(''), 'http://localhost:8080');
  assert.equal(normalizeStoredUrl('http://localhost:8080///'), 'http://localhost:8080');
});

test('SessionStore persiste cuenta sin guardar token JWT', () => {
  const store = new SessionStore(new MemoryStorage());
  const account = {
    accountId: 'account-1',
    email: 'user@test.com',
    names: 'Ada',
    token: null,
  };

  store.saveAccount(account);

  assert.deepEqual(store.getAccount(), account);
  store.clearAccount();
  assert.equal(store.getAccount(), null);
});
