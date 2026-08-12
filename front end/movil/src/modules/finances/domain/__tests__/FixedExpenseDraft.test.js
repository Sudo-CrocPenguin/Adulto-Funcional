import { FixedExpense } from '../FixedExpense';
import { FixedExpenseCollection } from '../FixedExpenseCollection';
import {
  FixedExpenseDraft,
  FixedExpenseValidationError,
} from '../FixedExpenseDraft';

describe('FixedExpenseDraft', () => {
  it('crea el contrato de gasto fijo con fechas civiles y recordatorio explícito', () => {
    const draft = FixedExpenseDraft.create({
      amount: '45.00',
      categoryId: 'category-health',
      frequency: 'MONTHLY',
      name: 'Gimnasio',
      nextDueDate: new Date(2026, 8, 5),
      status: 'ACTIVE',
    }, new Date(2026, 7, 10, 14, 0));

    expect(draft.toRequest()).toEqual({
      amount: 45,
      categoryId: 'category-health',
      frequency: 'MONTHLY',
      name: 'Gimnasio',
      nextDueDate: '2026-09-05',
      reminderDays: 0,
      startDate: '2026-08-10',
      status: 'ACTIVE',
    });
  });

  it('exige fecha futura, clasificación, nombre y catálogos válidos', () => {
    expect(() => FixedExpenseDraft.create({
      amount: '0',
      categoryId: '',
      frequency: 'DAILY',
      name: '',
      nextDueDate: new Date(2026, 7, 10),
      status: 'PAID',
    }, new Date(2026, 7, 10))).toThrow(FixedExpenseValidationError);
  });

  it('construye un PATCH mínimo y permite conservar un vencimiento anterior', () => {
    const expense = FixedExpense.fromApi({
      amount: 100,
      category: { id: 'category-home', name: 'Hogar' },
      frequency: 'MONTHLY',
      id: 'fixed-1',
      name: 'Alquiler',
      nextDueDate: '2026-08-01',
      status: 'ACTIVE',
    });
    const draft = FixedExpenseDraft.update({
      amount: '100',
      categoryId: 'category-home',
      frequency: 'MONTHLY',
      name: 'Arriendo',
      nextDueDate: new Date(2026, 7, 1),
      status: 'INACTIVE',
    }, expense, new Date(2026, 7, 10));

    expect(draft.toUpdateRequest(expense)).toEqual({
      name: 'Arriendo',
      status: 'INACTIVE',
    });
  });

  it('avanza vencimientos mensuales y conserva correctamente el fin de mes', () => {
    const expense = FixedExpense.fromApi({
      amount: 100,
      category: { id: 'category-home', name: 'Hogar' },
      frequency: 'MONTHLY',
      id: 'fixed-1',
      name: 'Alquiler',
      nextDueDate: '2026-01-31',
      status: 'ACTIVE',
    });

    expect(expense.nextDueDateAfterPayment(new Date(2026, 1, 1))).toBe('2026-02-28');
  });

  it('actualiza, reordena y elimina gastos sin mutar la colección original', () => {
    const later = FixedExpense.fromApi({
      id: 'later',
      nextDueDate: '2026-09-10',
    });
    const current = FixedExpense.fromApi({
      id: 'current',
      nextDueDate: '2026-09-05',
    });
    const updated = FixedExpense.fromApi({
      id: 'later',
      nextDueDate: '2026-09-01',
    });
    const collection = FixedExpenseCollection.create({ expenses: [current, later] });

    const afterUpdate = collection.withUpdated(updated);
    const afterDelete = afterUpdate.without('current');

    expect(afterUpdate.expenses.map(({ id }) => id)).toEqual(['later', 'current']);
    expect(afterDelete.expenses.map(({ id }) => id)).toEqual(['later']);
    expect(collection.expenses.map(({ id }) => id)).toEqual(['current', 'later']);
  });
});
