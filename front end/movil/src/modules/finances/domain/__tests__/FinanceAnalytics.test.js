import { FinanceAnalytics } from '../FinanceAnalytics';
import { FinanceLedger } from '../FinanceLedger';
import { FinanceMovement } from '../FinanceMovement';

function movement({ amount, category, date, id, type }) {
  return FinanceMovement.fromApi({
    amount,
    category: { id: category, name: category },
    description: id,
    id,
    movementDate: date,
    movementType: type,
  });
}

describe('FinanceLedger y FinanceAnalytics', () => {
  const movements = [
    movement({ amount: 1000, category: 'Trabajo', date: '2026-07-05', id: 'old-income', type: 'INCOME' }),
    movement({ amount: 200, category: 'Hogar', date: '2026-07-09', id: 'old-expense', type: 'EXPENSE' }),
    movement({ amount: 3500, category: 'Trabajo', date: '2026-08-01', id: 'income', type: 'INCOME' }),
    movement({ amount: 600, category: 'Hogar', date: '2026-08-02', id: 'rent', type: 'EXPENSE' }),
    movement({ amount: 400, category: 'Alimentación', date: '2026-08-03', id: 'market', type: 'EXPENSE' }),
  ];

  it('calcula totales y filtra por descripción o categoría', () => {
    const ledger = FinanceLedger.create({ categories: [], movements });

    expect(ledger.totalIncome).toBe(4500);
    expect(ledger.totalExpenses).toBe(1200);
    expect(ledger.balance).toBe(3300);
    expect(ledger.filteredBy('alimentación')).toHaveLength(1);
    expect(ledger.filteredBy('rent')).toHaveLength(1);
  });

  it('deriva todas las series sin inventar presupuestos persistidos', () => {
    const analytics = FinanceAnalytics.fromMovements(movements, new Date(2026, 7, 10));

    expect(analytics.current).toEqual({ balance: 2500, expenses: 1000, income: 3500 });
    expect(analytics.previous).toEqual({ balance: 800, expenses: 200, income: 1000 });
    expect(analytics.openingBalance).toBe(800);
    expect(analytics.balance).toBe(3300);
    expect(analytics.savingGoal).toBe(700);
    expect(analytics.dailyExpenses[1].value).toBe(600);
    expect(analytics.categoryComparison).toEqual(expect.arrayContaining([
      expect.objectContaining({ budget: 200, current: 600, label: 'Hogar' }),
    ]));
    expect(analytics.waterfall).toHaveLength(4);
    expect(analytics.health).toHaveLength(6);
  });
});
