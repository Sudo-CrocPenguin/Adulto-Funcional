export const FIXED_EXPENSE_TABS = Object.freeze({
  all: 'all',
  dueSoon: 'due-soon',
});

function daysBetween(from, to) {
  const milliseconds = to.getTime() - from.getTime();
  return Math.ceil(milliseconds / 86_400_000);
}

function fromIso(value) {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export class FixedExpenseCollection {
  constructor({ categories, expenses, now }) {
    this.categories = Object.freeze([...categories]);
    this.expenses = Object.freeze([...expenses]);
    this.now = now;
    Object.freeze(this);
  }

  static create({ categories = [], expenses = [], now = new Date() }) {
    return new FixedExpenseCollection({ categories, expenses, now });
  }

  filteredBy({ categoryId, frequency, status, tab }) {
    const today = new Date(
      this.now.getFullYear(),
      this.now.getMonth(),
      this.now.getDate(),
    );
    return this.expenses.filter((expense) => {
      if (frequency && expense.frequency !== frequency) {
        return false;
      }
      if (status && expense.status !== status) {
        return false;
      }
      if (categoryId && expense.category?.id !== categoryId) {
        return false;
      }
      if (tab === FIXED_EXPENSE_TABS.dueSoon) {
        const days = daysBetween(today, fromIso(expense.nextDueDate));
        return expense.isActive && days >= 0 && days <= 7;
      }
      return true;
    });
  }

  withExpense(expense) {
    const expenses = [...this.expenses, expense]
      .sort((left, right) => String(left.nextDueDate).localeCompare(String(right.nextDueDate)));
    return FixedExpenseCollection.create({
      categories: this.categories,
      expenses,
      now: this.now,
    });
  }

  withUpdated(expense) {
    return FixedExpenseCollection.create({
      categories: this.categories,
      expenses: this.expenses.map((current) => (
        current.id === expense.id ? expense : current
      )),
      now: this.now,
    });
  }
}
