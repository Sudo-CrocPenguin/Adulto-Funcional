const MONTH_LABELS = [
  'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
  'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic',
];

function amount(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function monthKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function monthKeyFromIso(value) {
  return String(value ?? '').slice(0, 7);
}

function monthStart(date, offset = 0) {
  return new Date(date.getFullYear(), date.getMonth() + offset, 1);
}

function clamp(value, minimum = 0, maximum = 100) {
  return Math.min(Math.max(value, minimum), maximum);
}

function aggregateByCategory(movements) {
  const totals = new Map();
  movements.forEach((movement) => {
    const key = movement.category?.id ?? movement.categoryName;
    const current = totals.get(key) ?? {
      colorKey: totals.size,
      id: key,
      label: movement.categoryName,
      value: 0,
    };
    current.value += amount(movement.amount);
    totals.set(key, current);
  });
  return [...totals.values()].sort((left, right) => right.value - left.value);
}

function totalsFor(movements) {
  return movements.reduce((totals, movement) => {
    if (movement.isIncome) {
      totals.income += amount(movement.amount);
    } else if (movement.isExpense) {
      totals.expenses += amount(movement.amount);
    }
    totals.balance = totals.income - totals.expenses;
    return totals;
  }, { balance: 0, expenses: 0, income: 0 });
}

function cumulativeSeries(movements) {
  const sorted = [...movements].sort((left, right) => (
    String(left.movementDate).localeCompare(String(right.movementDate))
  ));
  let balance = 0;
  return sorted.map((movement) => {
    balance += movement.isIncome ? movement.amount : -movement.amount;
    return {
      balance,
      date: movement.movementDate,
      id: movement.id,
    };
  });
}

function monthlyPeriods(now, count = 6) {
  return Array.from({ length: count }, (_, index) => {
    const date = monthStart(now, index - count + 1);
    return {
      key: monthKey(date),
      label: MONTH_LABELS[date.getMonth()],
      month: date.getMonth(),
      year: date.getFullYear(),
    };
  });
}

function monthlySeries(movements, now) {
  return monthlyPeriods(now).map((period) => {
    const totals = totalsFor(movements.filter(({ movementDate }) => (
      monthKeyFromIso(movementDate) === period.key
    )));
    return { ...period, ...totals };
  });
}

function dailyExpenses(movements, now) {
  const days = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
  const values = Array.from({ length: days }, (_, index) => ({
    day: index + 1,
    value: 0,
  }));
  movements
    .filter(({ isExpense, movementDate }) => (
      isExpense && monthKeyFromIso(movementDate) === monthKey(now)
    ))
    .forEach((movement) => {
      const day = Number(movement.movementDate.slice(8, 10));
      if (values[day - 1]) {
        values[day - 1].value += amount(movement.amount);
      }
    });
  return values;
}

function categoryComparison(currentExpenses, previousExpenses) {
  const current = new Map(
    aggregateByCategory(currentExpenses).map((item) => [item.id, item]),
  );
  const previous = new Map(
    aggregateByCategory(previousExpenses).map((item) => [item.id, item]),
  );
  const keys = new Set([...current.keys(), ...previous.keys()]);
  return [...keys].map((key, index) => ({
    budget: previous.get(key)?.value ?? 0,
    colorKey: index,
    current: current.get(key)?.value ?? 0,
    id: key,
    label: current.get(key)?.label ?? previous.get(key)?.label ?? 'Sin categoría',
    previous: previous.get(key)?.value ?? 0,
  })).sort((left, right) => right.current - left.current);
}

function healthScores({
  balance,
  categoryComparison: comparison,
  current,
  incomeSources,
  series,
}) {
  const positiveMonths = series.filter(({ balance: monthBalance }) => monthBalance > 0).length;
  const savingsRate = current.income > 0 ? current.balance / current.income : 0;
  const expenseRatio = current.income > 0 ? current.expenses / current.income : 1;
  const budgetReference = comparison.reduce((total, item) => total + item.budget, 0);
  return [
    { label: 'Ahorro', value: clamp((savingsRate / 0.2) * 100) },
    { label: 'Gasto', value: clamp((1 - expenseRatio) * 100) },
    { label: 'Estabilidad', value: (positiveMonths / Math.max(series.length, 1)) * 100 },
    { label: 'Ingresos', value: clamp((incomeSources.length / 3) * 100) },
    {
      label: 'Liquidez',
      value: clamp((Math.max(balance, 0) / Math.max(current.expenses, 1)) * 100),
    },
    {
      label: 'Referencia',
      value: budgetReference > 0
        ? clamp((budgetReference / Math.max(current.expenses, 1)) * 100)
        : 0,
    },
  ];
}

export class FinanceAnalytics {
  constructor(data) {
    Object.assign(this, data);
    Object.values(this).forEach((value) => {
      if (Array.isArray(value)) {
        value.forEach((item) => Object.freeze(item));
        Object.freeze(value);
      } else if (value && typeof value === 'object') {
        Object.freeze(value);
      }
    });
    Object.freeze(this);
  }

  static fromMovements(movements = [], now = new Date()) {
    const series = monthlySeries(movements, now);
    const currentKey = monthKey(now);
    const previousKey = monthKey(monthStart(now, -1));
    const currentMovements = movements.filter(({ movementDate }) => (
      monthKeyFromIso(movementDate) === currentKey
    ));
    const previousMovements = movements.filter(({ movementDate }) => (
      monthKeyFromIso(movementDate) === previousKey
    ));
    const current = totalsFor(currentMovements);
    const previous = totalsFor(previousMovements);
    const currentExpenses = currentMovements.filter(({ isExpense }) => isExpense);
    const previousExpenses = previousMovements.filter(({ isExpense }) => isExpense);
    const currentIncome = currentMovements.filter(({ isIncome }) => isIncome);
    const expensesByCategory = aggregateByCategory(currentExpenses);
    const incomeBySource = aggregateByCategory(currentIncome);
    const comparison = categoryComparison(currentExpenses, previousExpenses);
    const balance = totalsFor(movements).balance;
    const openingBalance = totalsFor(movements.filter(({ movementDate }) => (
      String(movementDate) < `${currentKey}-01`
    ))).balance;
    const savingGoal = current.income * 0.2;
    const savingActual = Math.max(current.balance, 0);
    const trend = series.map((period) => ({
      key: period.key,
      label: period.label,
      value: period.balance,
    }));
    let savingsStreak = 0;
    for (let index = trend.length - 1; index >= 0 && trend[index].value > 0; index -= 1) {
      savingsStreak += 1;
    }

    return new FinanceAnalytics({
      balance,
      categoryComparison: comparison,
      cumulative: cumulativeSeries(movements),
      current,
      dailyExpenses: dailyExpenses(movements, now),
      expensesByCategory,
      health: healthScores({
        balance,
        categoryComparison: comparison,
        current,
        incomeSources: incomeBySource,
        series,
      }),
      incomeBySource,
      monthly: series,
      openingBalance,
      previous,
      savingActual,
      savingGoal,
      savingsStreak,
      savingsTrend: trend,
      waterfall: [
        { label: 'Inicial', type: 'total', value: openingBalance },
        { label: 'Ingresos', type: 'increase', value: current.income },
        { label: 'Egresos', type: 'decrease', value: -current.expenses },
        { label: 'Final', type: 'total', value: balance },
      ],
    });
  }
}
