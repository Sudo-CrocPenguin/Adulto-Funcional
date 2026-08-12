import {
  FIXED_EXPENSE_FREQUENCIES,
  FIXED_EXPENSE_STATUSES,
} from './FixedExpense';

const FREQUENCIES = new Set(FIXED_EXPENSE_FREQUENCIES.map(({ value }) => value));
const STATUSES = new Set(FIXED_EXPENSE_STATUSES.map(({ value }) => value));
const MONEY_PATTERN = /^\d{1,8}([.,]\d{1,2})?$/;

export class FixedExpenseValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'FixedExpenseValidationError';
    this.fieldErrors = fieldErrors;
  }
}

function isoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isValidDate(value) {
  return value instanceof Date && !Number.isNaN(value.getTime());
}

export class FixedExpenseDraft {
  constructor(data) {
    Object.assign(this, data);
    Object.freeze(this);
  }

  static create(form, clock = new Date()) {
    const data = FixedExpenseDraft.validate(form, clock);

    return new FixedExpenseDraft({
      ...data,
      reminderDays: 0,
      startDate: isoDate(new Date(
        clock.getFullYear(),
        clock.getMonth(),
        clock.getDate(),
      )),
    });
  }

  static update(form, expense, clock = new Date()) {
    if (!expense?.id) {
      throw new Error('El gasto fijo no tiene un identificador válido.');
    }

    return new FixedExpenseDraft(FixedExpenseDraft.validate(form, clock, expense));
  }

  static validate(form, clock, expense = null) {
    const name = String(form.name ?? '').trim();
    const categoryId = String(form.categoryId ?? '').trim();
    const frequency = String(form.frequency ?? '');
    const status = String(form.status ?? '');
    const amountText = String(form.amount ?? '').trim();
    const nextDueDate = form.nextDueDate;
    const errors = {};

    if (!name) {
      errors.name = 'Escribe el nombre del gasto.';
    } else if (name.length > 20) {
      errors.name = 'El nombre no puede superar 20 caracteres.';
    } else if (/<[^>]*>/.test(name)) {
      errors.name = 'El nombre no puede contener HTML.';
    }
    if (!categoryId) {
      errors.categoryId = 'Selecciona una clasificación.';
    }
    if (!FREQUENCIES.has(frequency)) {
      errors.frequency = 'Selecciona una frecuencia válida.';
    }
    if (!STATUSES.has(status)) {
      errors.status = 'Selecciona un estado válido.';
    }
    if (!MONEY_PATTERN.test(amountText) || Number(amountText.replace(',', '.')) <= 0) {
      errors.amount = 'Escribe un monto positivo con máximo dos decimales.';
    }
    const today = new Date(clock.getFullYear(), clock.getMonth(), clock.getDate());
    const unchangedDueDate = isValidDate(nextDueDate)
      && isoDate(nextDueDate) === expense?.nextDueDate;
    if (!isValidDate(nextDueDate) || (!unchangedDueDate && nextDueDate <= today)) {
      errors.nextDueDate = 'La fecha de corte debe ser posterior a hoy.';
    }

    if (Object.keys(errors).length > 0) {
      throw new FixedExpenseValidationError(errors);
    }

    return {
      amount: Number(amountText.replace(',', '.')),
      categoryId,
      frequency,
      nextDueDate: isoDate(nextDueDate),
      status,
      name,
    };
  }

  toRequest() {
    return {
      amount: this.amount,
      categoryId: this.categoryId,
      frequency: this.frequency,
      name: this.name,
      nextDueDate: this.nextDueDate,
      reminderDays: this.reminderDays,
      startDate: this.startDate,
      status: this.status,
    };
  }

  toUpdateRequest(expense) {
    const currentCategoryId = expense.category?.id ?? null;
    const fields = {
      amount: this.amount,
      categoryId: this.categoryId,
      frequency: this.frequency,
      name: this.name,
      nextDueDate: this.nextDueDate,
      status: this.status,
    };

    return Object.fromEntries(Object.entries(fields).filter(([field, value]) => {
      if (field === 'categoryId') {
        return value !== currentCategoryId;
      }
      if (field === 'amount') {
        return value !== Number(expense.amount);
      }
      return value !== expense[field];
    }));
  }
}
