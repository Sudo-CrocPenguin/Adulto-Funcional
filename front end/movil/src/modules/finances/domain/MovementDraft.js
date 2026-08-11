import { MOVEMENT_TYPES } from './FinanceMovement';

const ALLOWED_TYPES = new Set(Object.values(MOVEMENT_TYPES));
const MONEY_PATTERN = /^\d{1,8}([.,]\d{1,2})?$/;

export class MovementValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'MovementValidationError';
    this.fieldErrors = fieldErrors;
  }
}

function localIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function validDate(value) {
  return value instanceof Date && !Number.isNaN(value.getTime());
}

export class MovementDraft {
  constructor(data) {
    Object.assign(this, data);
    Object.freeze(this);
  }

  static create(form) {
    const movementType = String(form.movementType ?? '');
    const categoryId = String(form.categoryId ?? '').trim();
    const description = String(form.description ?? '').trim();
    const amountText = String(form.amount ?? '').trim();
    const movementDate = form.movementDate;
    const errors = {};

    if (!ALLOWED_TYPES.has(movementType)) {
      errors.movementType = 'Selecciona ingreso o egreso.';
    }
    if (!categoryId) {
      errors.categoryId = 'Selecciona una clasificación.';
    }
    if (!MONEY_PATTERN.test(amountText) || Number(amountText.replace(',', '.')) <= 0) {
      errors.amount = 'Escribe un monto positivo con máximo dos decimales.';
    }
    if (!validDate(movementDate)) {
      errors.movementDate = 'Selecciona una fecha válida.';
    }
    if (description.length > 65535) {
      errors.description = 'La descripción es demasiado larga.';
    } else if (/<[^>]*>/.test(description)) {
      errors.description = 'La descripción no puede contener HTML.';
    }

    if (Object.keys(errors).length > 0) {
      throw new MovementValidationError(errors);
    }

    return new MovementDraft({
      amount: Number(amountText.replace(',', '.')),
      categoryId,
      description,
      movementDate: localIsoDate(movementDate),
      movementType,
    });
  }

  static forFixedExpense(expense, clock = new Date()) {
    return new MovementDraft({
      amount: expense.amount,
      categoryId: expense.category?.id,
      description: `Pago de gasto fijo: ${expense.name}`,
      movementDate: localIsoDate(clock),
      movementType: MOVEMENT_TYPES.expense,
    });
  }

  toRequest() {
    return {
      amount: this.amount,
      categoryId: this.categoryId,
      description: this.description || undefined,
      movementDate: this.movementDate,
      movementType: this.movementType,
    };
  }
}
