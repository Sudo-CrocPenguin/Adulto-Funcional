import {
  FixedExpensePaymentSyncError,
  PayFixedExpenseUseCase,
} from '../PayFixedExpenseUseCase';
import { FixedExpense } from '../../domain/FixedExpense';

const session = { accessToken: 'token', tokenType: 'Bearer' };

function expense() {
  return FixedExpense.fromApi({
    amount: 45,
    category: { id: 'category-health', name: 'Salud' },
    frequency: 'MONTHLY',
    id: 'fixed-1',
    name: 'Gimnasio',
    nextDueDate: '2026-08-27',
    status: 'ACTIVE',
  });
}

describe('PayFixedExpenseUseCase', () => {
  it('registra primero el egreso y después avanza el vencimiento', async () => {
    const calls = [];
    const repository = {
      createMovement: jest.fn(async (draft) => {
        calls.push('movement');
        return { id: 'movement-1', request: draft.toRequest() };
      }),
      updateFixedExpense: jest.fn(async (_id, changes) => {
        calls.push('fixed-expense');
        return { id: 'fixed-1', nextDueDate: changes.nextDueDate };
      }),
    };
    const useCase = new PayFixedExpenseUseCase(
      repository,
      () => new Date(2026, 7, 27, 12, 0),
    );

    const result = await useCase.execute(expense(), session);

    expect(calls).toEqual(['movement', 'fixed-expense']);
    expect(result.movement.request).toMatchObject({
      amount: 45,
      categoryId: 'category-health',
      description: 'Pago de gasto fijo: Gimnasio',
      movementDate: '2026-08-27',
      movementType: 'EXPENSE',
    });
    expect(repository.updateFixedExpense).toHaveBeenCalledWith(
      'fixed-1',
      { nextDueDate: '2026-09-27' },
      session,
    );
  });

  it('informa el estado parcial si el egreso existe pero falla el vencimiento', async () => {
    const repository = {
      createMovement: jest.fn().mockResolvedValue({ id: 'movement-1' }),
      updateFixedExpense: jest.fn().mockRejectedValue(new Error('falló el patch')),
    };
    const useCase = new PayFixedExpenseUseCase(
      repository,
      () => new Date(2026, 7, 27, 12, 0),
    );

    await expect(useCase.execute(expense(), session)).rejects.toMatchObject({
      name: FixedExpensePaymentSyncError.name,
      movement: { id: 'movement-1' },
    });
  });
});
