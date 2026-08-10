import { FixedExpense } from '../FixedExpense';
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
});
