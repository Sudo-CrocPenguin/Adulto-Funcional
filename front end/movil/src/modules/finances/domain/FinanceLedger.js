export class FinanceLedger {
  constructor({ categories, movements }) {
    this.categories = Object.freeze([...categories]);
    this.movements = Object.freeze([...movements]);
    this.totalExpenses = movements
      .filter(({ isExpense }) => isExpense)
      .reduce((total, { amount }) => total + amount, 0);
    this.totalIncome = movements
      .filter(({ isIncome }) => isIncome)
      .reduce((total, { amount }) => total + amount, 0);
    this.balance = this.totalIncome - this.totalExpenses;
    Object.freeze(this);
  }

  static create({ categories = [], movements = [] }) {
    return new FinanceLedger({ categories, movements });
  }

  filteredBy(searchTerm) {
    const term = String(searchTerm ?? '').trim().toLocaleLowerCase('es');
    if (!term) {
      return this.movements;
    }
    return this.movements.filter((movement) => (
      movement.title.toLocaleLowerCase('es').includes(term)
      || movement.categoryName.toLocaleLowerCase('es').includes(term)
    ));
  }

  withMovement(movement) {
    const movements = [movement, ...this.movements]
      .sort((left, right) => String(right.movementDate).localeCompare(String(left.movementDate)));
    return FinanceLedger.create({ categories: this.categories, movements });
  }
}
