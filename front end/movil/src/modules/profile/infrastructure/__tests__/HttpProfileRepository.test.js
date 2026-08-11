import { HttpProfileRepository } from '../HttpProfileRepository';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

function page(items, totalElements = items.length, hasNext = false) {
  return { items, page: { hasNext, totalElements } };
}

describe('HttpProfileRepository', () => {
  it('compone el perfil y la actividad desde endpoints propios', async () => {
    const apiClient = {
      get: jest.fn(async (path) => path.startsWith('/api/account/')
        ? {
          createdAt: '2026-02-02T10:00:00Z',
          email: 'ana@example.com',
          id: 'account-id',
          lastnames: 'Ruiz',
          names: 'Ana',
          phone: '+573001234567',
        }
        : { configured: true, verified: true }),
      getPage: jest.fn(async (path) => {
        if (path.startsWith('/api/agenda/events')) {
          return page([{ eventDate: '2026-02-01' }, { eventDate: '2026-02-02' }]);
        }
        if (path.startsWith('/api/finances/fixed-expenses')) return page([], 8);
        if (path.startsWith('/api/security/passwords')) return page([], 2);
        throw new Error(`Ruta inesperada: ${path}`);
      }),
    };
    const repository = new HttpProfileRepository(apiClient);

    const snapshot = await repository.load('account-id', session);

    expect(snapshot.profile.fullName).toBe('Ana Ruiz');
    expect(snapshot.activity).toMatchObject({
      completedCommitments: 2,
      fixedExpensesCount: 8,
      maximumStreakDays: 2,
      passwordsCount: 2,
    });
    expect(apiClient.get).toHaveBeenCalledWith('/api/account/account-id', {
      headers: { Authorization: 'Bearer access-token' },
    });
  });

  it('no consulta contraseñas cuando la bóveda está bloqueada', async () => {
    const apiClient = {
      get: jest.fn().mockResolvedValue({ configured: true, verified: false }),
      getPage: jest.fn(),
    };
    const repository = new HttpProfileRepository(apiClient);

    await expect(repository.passwordCount(session)).resolves.toBeNull();
    expect(apiClient.getPage).not.toHaveBeenCalled();
  });

  it('actualiza únicamente los cambios validados por el dominio', async () => {
    const apiClient = {
      patch: jest.fn().mockResolvedValue({ id: 'account-id', names: 'Ana María' }),
    };
    const repository = new HttpProfileRepository(apiClient);
    const draft = { toRequest: () => ({ names: 'Ana María' }) };

    await repository.update('account-id', draft, session);

    expect(apiClient.patch).toHaveBeenCalledWith(
      '/api/account/account-id',
      { names: 'Ana María' },
      { headers: { Authorization: 'Bearer access-token' } },
    );
  });
});
