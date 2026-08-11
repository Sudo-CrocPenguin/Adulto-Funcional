import { DashboardSnapshot } from '../DashboardSnapshot';

describe('DashboardSnapshot', () => {
  it('calcula el resumen financiero, agenda y racha con datos reales', () => {
    const snapshot = DashboardSnapshot.fromSources({
      completedEvents: [
        { eventDate: '2026-08-10' },
        { eventDate: '2026-08-09' },
        { eventDate: '2026-08-08' },
        { eventDate: '2026-08-06' },
      ],
      fixedExpenses: [{ id: 'fixed-1', name: 'Internet' }],
      movements: [
        {
          amount: 3500,
          category: { name: 'Salario' },
          movementDate: '2026-08-01',
          movementType: 'INCOME',
        },
        {
          amount: 1000,
          category: { name: 'Mercado' },
          movementDate: '2026-07-20',
          movementType: 'EXPENSE',
        },
        {
          amount: 1500,
          category: { name: 'Ocio' },
          movementDate: '2026-06-10',
          movementType: 'EXPENSE',
        },
        {
          amount: 500,
          category: { name: 'Ahorros' },
          movementDate: '2026-08-02',
          movementType: 'INCOME',
        },
        {
          amount: 10000,
          category: { name: 'Salario' },
          movementDate: '2026-04-01',
          movementType: 'INCOME',
        },
      ],
      passwordsCount: 12,
      pendingCommitmentsCount: 8,
      today: '2026-08-10',
      upcomingCommitments: [{ id: 'event-1', title: 'Reunión' }],
      upcomingExpensesCount: 3,
      vaultConfigured: true,
      vaultVerified: true,
    });

    expect(snapshot).toMatchObject({
      balance: 11500,
      nextCommitment: { id: 'event-1', title: 'Reunión' },
      nextFixedExpense: { id: 'fixed-1', name: 'Internet' },
      passwordsCount: 12,
      pendingCommitmentsCount: 8,
      statistics: {
        expenses: 2500,
        income: 4000,
        leisure: 1500,
        savings: 500,
      },
      streakDays: 3,
      upcomingExpensesCount: 3,
    });
  });

  it('admite iniciar la racha ayer y representar la bóveda bloqueada', () => {
    const snapshot = DashboardSnapshot.fromSources({
      completedEvents: [
        { eventDate: '2026-08-09' },
        { eventDate: '2026-08-08' },
      ],
      passwordsCount: null,
      today: '2026-08-10',
      vaultConfigured: true,
      vaultVerified: false,
    });

    expect(snapshot.streakDays).toBe(2);
    expect(snapshot.passwordsCount).toBeNull();
    expect(snapshot.vaultVerified).toBe(false);
  });
});
