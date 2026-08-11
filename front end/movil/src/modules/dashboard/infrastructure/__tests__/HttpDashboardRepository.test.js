import { HttpDashboardRepository } from '../HttpDashboardRepository';

const session = {
  accessToken: 'access-token',
  tokenType: 'Bearer',
};

function page(items, metadata = {}) {
  return {
    items,
    page: {
      hasNext: false,
      totalElements: items.length,
      ...metadata,
    },
  };
}

describe('HttpDashboardRepository', () => {
  it('compone los endpoints autenticados y omite la bóveda bloqueada', async () => {
    const apiClient = {
      get: jest.fn().mockResolvedValue({ configured: true, verified: false }),
      getPage: jest.fn()
        .mockResolvedValueOnce(page([{
          amount: 2000,
          movementDate: '2026-08-01',
          movementType: 'INCOME',
        }]))
        .mockResolvedValueOnce(page([{ id: 'fixed-1', name: 'Internet' }], {
          totalElements: 3,
        }))
        .mockResolvedValueOnce(page([], { totalElements: 8 }))
        .mockResolvedValueOnce(page([{ id: 'event-1', title: 'Reunión' }]))
        .mockResolvedValueOnce(page([{ eventDate: '2026-08-09' }])),
    };
    const repository = new HttpDashboardRepository(
      apiClient,
      () => new Date(2026, 7, 10, 12),
    );

    await expect(repository.load(session)).resolves.toMatchObject({
      balance: 2000,
      nextCommitment: { id: 'event-1' },
      passwordsCount: null,
      streakDays: 1,
      upcomingExpensesCount: 3,
      vaultVerified: false,
    });
    expect(apiClient.getPage).toHaveBeenCalledTimes(5);
    expect(apiClient.getPage).toHaveBeenCalledWith(
      expect.stringContaining('/api/finances/movements?'),
      {
        headers: { Authorization: 'Bearer access-token' },
      },
    );
  });

  it('consulta el total de contraseñas solo con la Master Key verificada', async () => {
    const apiClient = {
      get: jest.fn().mockResolvedValue({ configured: true, verified: true }),
      getPage: jest.fn()
        .mockResolvedValueOnce(page([]))
        .mockResolvedValueOnce(page([]))
        .mockResolvedValueOnce(page([]))
        .mockResolvedValueOnce(page([]))
        .mockResolvedValueOnce(page([]))
        .mockResolvedValueOnce(page([], { totalElements: 12 })),
    };
    const repository = new HttpDashboardRepository(
      apiClient,
      () => new Date(2026, 7, 10, 12),
    );

    const snapshot = await repository.load(session);

    expect(snapshot.passwordsCount).toBe(12);
    expect(apiClient.getPage).toHaveBeenLastCalledWith(
      expect.stringContaining('/api/security/passwords?'),
      {
        headers: { Authorization: 'Bearer access-token' },
      },
    );
  });
});
