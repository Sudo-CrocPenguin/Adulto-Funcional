const NOTIFICATION_TYPES = Object.freeze({
  commitment: 'commitment',
  finance: 'finance',
  fixedExpense: 'fixed-expense',
});

function resourceIdentity(resource, fallback) {
  return resource?.id
    ?? resource?.eventDate
    ?? resource?.nextDueDate
    ?? resource?.title
    ?? resource?.name
    ?? fallback;
}

export class DashboardNotification {
  constructor({ date = null, id, message = null, subject = null, title, type }) {
    this.date = date;
    this.id = id;
    this.message = message;
    this.subject = subject;
    this.title = title;
    this.type = type;
    Object.freeze(this);
  }

  static fromSnapshot(snapshot) {
    if (!snapshot) {
      return [];
    }

    const notifications = [];

    if (snapshot.nextFixedExpense) {
      notifications.push(new DashboardNotification({
        date: snapshot.nextFixedExpense.nextDueDate ?? null,
        id: `fixed-expense:${resourceIdentity(snapshot.nextFixedExpense, 'next')}`,
        subject: snapshot.nextFixedExpense.name ?? 'Próximo gasto',
        title: 'Gastos fijos',
        type: NOTIFICATION_TYPES.fixedExpense,
      }));
    }

    if (snapshot.nextCommitment) {
      notifications.push(new DashboardNotification({
        date: snapshot.nextCommitment.eventDate ?? null,
        id: `commitment:${resourceIdentity(snapshot.nextCommitment, 'next')}`,
        subject: snapshot.nextCommitment.title ?? 'Próximo compromiso',
        title: 'Compromisos',
        type: NOTIFICATION_TYPES.commitment,
      }));
    }

    if (snapshot.balance < 0) {
      notifications.push(new DashboardNotification({
        id: 'finance:negative-balance',
        message: 'Tu saldo actual se encuentra en negativo.',
        title: 'Finanzas',
        type: NOTIFICATION_TYPES.finance,
      }));
    }

    return notifications;
  }
}
