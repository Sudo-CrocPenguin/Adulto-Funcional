const MAX_STREAK_DAYS = 30;

function amountOf(value) {
  const amount = Number(value);
  return Number.isFinite(amount) ? amount : 0;
}

function normalizeText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}

function dateAtUtc(value) {
  return new Date(`${value}T00:00:00.000Z`);
}

function isoDate(date) {
  return date.toISOString().slice(0, 10);
}

function addDays(value, days) {
  const date = dateAtUtc(value);
  date.setUTCDate(date.getUTCDate() + days);
  return isoDate(date);
}

function statisticsStart(today) {
  const date = dateAtUtc(today);
  date.setUTCMonth(date.getUTCMonth() - 3);
  return isoDate(date);
}

function calculateStreak(completedEvents, today) {
  const completedDates = new Set(
    completedEvents
      .map((event) => event?.eventDate)
      .filter(Boolean),
  );

  let cursor = completedDates.has(today) ? today : addDays(today, -1);
  let streak = 0;

  while (streak < MAX_STREAK_DAYS && completedDates.has(cursor)) {
    streak += 1;
    cursor = addDays(cursor, -1);
  }

  return streak;
}

function movementStatistics(movements, today) {
  const startDate = statisticsStart(today);
  const recent = movements.filter(
    ({ movementDate }) => movementDate && movementDate >= startDate,
  );

  return recent.reduce((totals, movement) => {
    const amount = amountOf(movement.amount);
    const category = normalizeText(movement.category?.name);

    if (movement.movementType === 'INCOME') {
      totals.income += amount;
    } else if (movement.movementType === 'EXPENSE') {
      totals.expenses += amount;
    }

    if (category === 'ocio' || category === 'osio') {
      totals.leisure += amount;
    }
    if (category === 'ahorro' || category === 'ahorros') {
      totals.savings += amount;
    }

    return totals;
  }, {
    expenses: 0,
    income: 0,
    leisure: 0,
    savings: 0,
  });
}

function currentBalance(movements) {
  return movements.reduce((balance, movement) => {
    const amount = amountOf(movement.amount);
    return movement.movementType === 'INCOME'
      ? balance + amount
      : balance - amount;
  }, 0);
}

export class DashboardSnapshot {
  constructor(data) {
    Object.assign(this, data);
    Object.freeze(this.statistics);
    Object.freeze(this);
  }

  static fromSources({
    completedEvents = [],
    fixedExpenses = [],
    movements = [],
    passwordsCount = null,
    pendingCommitmentsCount = 0,
    upcomingCommitments = [],
    upcomingExpensesCount = 0,
    vaultConfigured = false,
    vaultVerified = false,
    today,
  }) {
    return new DashboardSnapshot({
      balance: currentBalance(movements),
      nextCommitment: upcomingCommitments[0] ?? null,
      nextFixedExpense: fixedExpenses[0] ?? null,
      passwordsCount,
      pendingCommitmentsCount,
      statistics: movementStatistics(movements, today),
      streakDays: calculateStreak(completedEvents, today),
      upcomingExpensesCount,
      vaultConfigured,
      vaultVerified,
    });
  }
}
