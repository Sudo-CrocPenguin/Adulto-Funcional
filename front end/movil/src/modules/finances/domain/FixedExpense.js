export const FIXED_EXPENSE_FREQUENCIES = Object.freeze([
  Object.freeze({ label: 'Semanal', value: 'WEEKLY' }),
  Object.freeze({ label: 'Quincenal', value: 'BIWEEKLY' }),
  Object.freeze({ label: 'Mensual', value: 'MONTHLY' }),
  Object.freeze({ label: 'Trimestral', value: 'QUARTERLY' }),
  Object.freeze({ label: 'Semestral', value: 'SEMIANNUAL' }),
  Object.freeze({ label: 'Anual', value: 'ANNUAL' }),
]);

export const FIXED_EXPENSE_STATUSES = Object.freeze([
  Object.freeze({ label: 'Activo', value: 'ACTIVE' }),
  Object.freeze({ label: 'Inactivo', value: 'INACTIVE' }),
]);

const FREQUENCY_LABELS = Object.freeze(
  Object.fromEntries(FIXED_EXPENSE_FREQUENCIES.map(({ label, value }) => [value, label])),
);

const FREQUENCY_MONTHS = Object.freeze({
  ANNUAL: 12,
  MONTHLY: 1,
  QUARTERLY: 3,
  SEMIANNUAL: 6,
});

function isoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function dateFromIso(value) {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function addMonthsClamped(date, months) {
  const day = date.getDate();
  const target = new Date(date.getFullYear(), date.getMonth() + months, 1);
  const lastDay = new Date(target.getFullYear(), target.getMonth() + 1, 0).getDate();
  target.setDate(Math.min(day, lastDay));
  return target;
}

function advanceDate(date, frequency) {
  const result = new Date(date);
  if (frequency === 'WEEKLY') {
    result.setDate(result.getDate() + 7);
    return result;
  }
  if (frequency === 'BIWEEKLY') {
    result.setDate(result.getDate() + 14);
    return result;
  }
  return addMonthsClamped(result, FREQUENCY_MONTHS[frequency] ?? 1);
}

export class FixedExpense {
  constructor(data) {
    Object.assign(this, data);
    if (this.category) {
      Object.freeze(this.category);
    }
    Object.freeze(this);
  }

  static fromApi(data) {
    return new FixedExpense({
      amount: Number(data?.amount ?? 0),
      category: data?.category ?? null,
      frequency: data?.frequency ?? 'MONTHLY',
      id: data?.id ?? null,
      name: data?.name ?? '',
      nextDueDate: data?.nextDueDate ?? null,
      reminderDays: Number(data?.reminderDays ?? 0),
      startDate: data?.startDate ?? null,
      status: data?.status ?? 'ACTIVE',
    });
  }

  get categoryName() {
    return this.category?.name ?? 'Sin clasificación';
  }

  get frequencyLabel() {
    return FREQUENCY_LABELS[this.frequency] ?? this.frequency;
  }

  get isActive() {
    return this.status === 'ACTIVE';
  }

  nextDueDateAfterPayment(clock = new Date()) {
    let nextDate = advanceDate(dateFromIso(this.nextDueDate), this.frequency);
    const today = new Date(clock.getFullYear(), clock.getMonth(), clock.getDate());

    while (nextDate <= today) {
      nextDate = advanceDate(nextDate, this.frequency);
    }

    return isoDate(nextDate);
  }
}
