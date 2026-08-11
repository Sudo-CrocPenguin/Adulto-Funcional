function normalizedDates(commitments) {
  return [...new Set(
    commitments
      .map(({ eventDate }) => eventDate)
      .filter((value) => /^\d{4}-\d{2}-\d{2}$/u.test(value ?? '')),
  )].sort();
}

function previousDay(value) {
  const date = new Date(`${value}T00:00:00.000Z`);
  date.setUTCDate(date.getUTCDate() - 1);
  return date.toISOString().slice(0, 10);
}

export function maximumCommitmentStreak(commitments) {
  const dates = normalizedDates(commitments);
  let maximum = 0;
  let current = 0;
  let lastDate = null;

  for (const date of dates) {
    current = lastDate && previousDay(date) === lastDate ? current + 1 : 1;
    maximum = Math.max(maximum, current);
    lastDate = date;
  }

  return maximum;
}

export class ProfileActivity {
  constructor({ completedCommitments, fixedExpensesCount, maximumStreakDays, passwordsCount }) {
    this.completedCommitments = completedCommitments;
    this.fixedExpensesCount = fixedExpensesCount;
    this.maximumStreakDays = maximumStreakDays;
    this.passwordsCount = passwordsCount;
    Object.freeze(this);
  }

  static fromSources({ commitments = [], fixedExpensesCount = 0, passwordsCount = null }) {
    return new ProfileActivity({
      completedCommitments: commitments.length,
      fixedExpensesCount: Number(fixedExpensesCount) || 0,
      maximumStreakDays: maximumCommitmentStreak(commitments),
      passwordsCount: passwordsCount !== null && passwordsCount !== undefined
        && Number.isFinite(Number(passwordsCount))
        ? Number(passwordsCount)
        : null,
    });
  }
}
