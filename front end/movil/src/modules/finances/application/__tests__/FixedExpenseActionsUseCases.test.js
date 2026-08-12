import { DeleteFixedExpenseUseCase } from '../DeleteFixedExpenseUseCase';
import { UpdateFixedExpenseUseCase } from '../UpdateFixedExpenseUseCase';
import { FixedExpense } from '../../domain/FixedExpense';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

describe('acciones de gastos fijos', () => {
  it('valida y actualiza solo los campos editados', async () => {
    const expense = FixedExpense.fromApi({
      amount: 100,
      category: { id: 'category-1', name: 'Hogar' },
      frequency: 'MONTHLY',
      id: 'fixed-1',
      name: 'Arriendo',
      nextDueDate: '2026-09-01',
      status: 'ACTIVE',
    });
    const repository = {
      updateFixedExpense: jest.fn().mockResolvedValue(expense),
    };
    const useCase = new UpdateFixedExpenseUseCase(
      repository,
      () => new Date(2026, 7, 10),
    );

    await useCase.execute(expense, {
      amount: '120',
      categoryId: 'category-1',
      frequency: 'MONTHLY',
      name: 'Arriendo',
      nextDueDate: new Date(2026, 8, 1),
      status: 'ACTIVE',
    }, session);

    expect(repository.updateFixedExpense).toHaveBeenCalledWith(
      'fixed-1',
      { amount: 120 },
      session,
    );
  });

  it('elimina el gasto y devuelve su identificador', async () => {
    const repository = { deleteFixedExpense: jest.fn().mockResolvedValue(null) };
    const useCase = new DeleteFixedExpenseUseCase(repository);

    await expect(useCase.execute('fixed-1', session)).resolves.toBe('fixed-1');
    expect(repository.deleteFixedExpense).toHaveBeenCalledWith('fixed-1', session);
  });
});
