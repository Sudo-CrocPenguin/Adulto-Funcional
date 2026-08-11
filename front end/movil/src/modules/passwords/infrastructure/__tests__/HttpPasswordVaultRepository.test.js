import { HttpPasswordVaultRepository } from '../HttpPasswordVaultRepository';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

function page(items, hasNext = false) {
  return { items, page: { hasNext } };
}

describe('HttpPasswordVaultRepository', () => {
  it('usa el contrato canónico de Master Key con autorización', async () => {
    const apiClient = {
      delete: jest.fn().mockResolvedValue({ configured: true, verified: false }),
      get: jest.fn().mockResolvedValue({ configured: true, verified: false }),
      patch: jest.fn().mockResolvedValue({ configured: true, verified: false }),
      post: jest.fn().mockResolvedValue({ configured: true, verified: true }),
    };
    const repository = new HttpPasswordVaultRepository(apiClient);
    const command = { toRequest: () => ({ masterKey: 'Master-Key-2026' }) };

    await repository.status(session);
    await repository.verify(command, session);
    await repository.lock(session);

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/security/master-key/status',
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/security/master-key/verify',
      { masterKey: 'Master-Key-2026' },
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(apiClient.delete).toHaveBeenCalledWith(
      '/api/security/master-key/session',
      { headers: { Authorization: 'Bearer access-token' } },
    );
  });

  it('pagina metadatos sin depender de secretos en el listado', async () => {
    const apiClient = {
      getPage: jest.fn()
        .mockResolvedValueOnce(page([{ applicationName: 'Netflix', id: '1' }], true))
        .mockResolvedValueOnce(page([{ applicationName: 'Spotify', id: '2' }], false)),
    };
    const repository = new HttpPasswordVaultRepository(apiClient);

    const credentials = await repository.listCredentials(session);

    expect(credentials.map(({ applicationName }) => applicationName)).toEqual(['Netflix', 'Spotify']);
    expect(credentials.every(({ password }) => password === null)).toBe(true);
    expect(apiClient.getPage.mock.calls[1][0]).toContain('page=1');
  });

  it('revela individualmente y aplica CRUD en rutas protegidas', async () => {
    const apiClient = {
      delete: jest.fn().mockResolvedValue(null),
      get: jest.fn().mockResolvedValue({ applicationName: 'Netflix', id: '1', password: 'secreto' }),
      patch: jest.fn().mockResolvedValue({ applicationName: 'Netflix+', id: '1' }),
      post: jest.fn().mockResolvedValue({ applicationName: 'Netflix', id: '1' }),
    };
    const repository = new HttpPasswordVaultRepository(apiClient);

    await expect(repository.getCredential('1', session)).resolves.toMatchObject({ password: 'secreto' });
    await repository.createCredential({ toRequest: () => ({ applicationName: 'Netflix', password: 'secreto' }) }, session);
    await repository.updateCredential('1', { toRequest: () => ({ applicationName: 'Netflix+' }) }, session);
    await repository.deleteCredential('1', session);

    expect(apiClient.delete).toHaveBeenCalledWith(
      '/api/security/passwords/1',
      { headers: { Authorization: 'Bearer access-token' } },
    );
  });
});
