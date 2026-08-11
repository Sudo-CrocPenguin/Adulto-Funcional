import { DashboardNotification } from '../DashboardNotification';

describe('DashboardNotification', () => {
  it('crea avisos con las próximas acciones del inicio', () => {
    const notifications = DashboardNotification.fromSnapshot({
      balance: 2500,
      nextCommitment: {
        eventDate: '2026-08-12',
        id: 'event-1',
        title: 'Reunión con equipo',
      },
      nextFixedExpense: {
        id: 'fixed-1',
        name: 'Internet',
        nextDueDate: '2026-08-11',
      },
    });

    expect(notifications).toEqual([
      expect.objectContaining({
        date: '2026-08-11',
        id: 'fixed-expense:fixed-1',
        subject: 'Internet',
        title: 'Gastos fijos',
      }),
      expect.objectContaining({
        date: '2026-08-12',
        id: 'commitment:event-1',
        subject: 'Reunión con equipo',
        title: 'Compromisos',
      }),
    ]);
  });

  it('agrega una alerta financiera solo cuando el saldo es negativo', () => {
    expect(DashboardNotification.fromSnapshot({ balance: -20 })).toEqual([
      expect.objectContaining({
        id: 'finance:negative-balance',
        title: 'Finanzas',
      }),
    ]);

    expect(DashboardNotification.fromSnapshot({ balance: 0 })).toEqual([]);
    expect(DashboardNotification.fromSnapshot(null)).toEqual([]);
  });
});
