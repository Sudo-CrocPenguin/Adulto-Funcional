export const MOVEMENT_TYPES = Object.freeze({
  expense: 'EXPENSE',
  income: 'INCOME',
});

export class FinanceMovement {
  constructor(data) {
    Object.assign(this, data);
    if (this.category) {
      Object.freeze(this.category);
    }
    Object.freeze(this);
  }

  static fromApi(data) {
    return new FinanceMovement({
      amount: Number(data?.amount ?? 0),
      category: data?.category ?? null,
      description: data?.description?.trim() ?? '',
      id: data?.id ?? null,
      movementDate: data?.movementDate ?? null,
      movementType: data?.movementType ?? MOVEMENT_TYPES.expense,
      registerDate: data?.registerDate ?? null,
    });
  }

  get categoryName() {
    return this.category?.name ?? 'Sin clasificación';
  }

  get isExpense() {
    return this.movementType === MOVEMENT_TYPES.expense;
  }

  get isIncome() {
    return this.movementType === MOVEMENT_TYPES.income;
  }

  get title() {
    return this.description || this.categoryName;
  }
}
