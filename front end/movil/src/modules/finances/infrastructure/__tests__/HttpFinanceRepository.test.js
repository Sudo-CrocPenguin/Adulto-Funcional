import { HttpFinanceRepository } from '../HttpFinanceRepository';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

function page(items, hasNext = false) {
  return { items, page: { hasNext } };
}

describe('HttpFinanceRepository', () => {
  it('pagina movimientos y consulta únicamente categorías FINANCES', async () => {
    const apiClient = {
      getPage: jest.fn()
        .mockResolvedValueOnce(page([{ id: 'movement-1', movementType: 'INCOME' }], true))
        .mockResolvedValueOnce(page([{ id: 'movement-2', movementType: 'EXPENSE' }]))
        .mockResolvedValueOnce(page([{ id: 'category-1', name: 'Trabajo' }])),
    };
    const repository = new HttpFinanceRepository(apiClient);

    const movements = await repository.listMovements(session);
    const categories = await repository.listCategories(session);

    expect(movements.map(({ id }) => id)).toEqual(['movement-1', 'movement-2']);
    expect(categories).toEqual([{ id: 'category-1', name: 'Trabajo' }]);
    expect(apiClient.getPage.mock.calls[1][0]).toContain('page=1');
    expect(apiClient.getPage.mock.calls[2][0]).toContain('type=FINANCES');
  });

  it('crea el movimiento y actualiza el vencimiento con autenticación', async () => {
    const request = {
      amount: 45,
      categoryId: 'category-1',
      movementDate: '2026-08-27',
      movementType: 'EXPENSE',
    };
    const apiClient = {
      patch: jest.fn().mockResolvedValue({
        amount: 45,
        id: 'fixed-1',
        nextDueDate: '2026-09-27',
      }),
      post: jest.fn().mockResolvedValue({ ...request, id: 'movement-1' }),
    };
    const repository = new HttpFinanceRepository(apiClient);

    await repository.createMovement({ toRequest: () => request }, session);
    await repository.updateFixedExpense('fixed-1', { nextDueDate: '2026-09-27' }, session);

    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/finances/movements',
      request,
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(apiClient.patch).toHaveBeenCalledWith(
      '/api/finances/fixed-expenses/fixed-1',
      { nextDueDate: '2026-09-27' },
      { headers: { Authorization: 'Bearer access-token' } },
    );
  });
});
