import { COMMITMENT_FILTERS } from './Commitment';

const MAX_STREAK_DAYS = 30;

function toIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function previousDate(value) {
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  date.setDate(date.getDate() - 1);
  return toIsoDate(date);
}

function calculateStreak(commitments, today) {
  const completedDates = new Set(
    commitments
      .filter(({ isCompleted }) => isCompleted)
      .map(({ eventDate }) => eventDate)
      .filter(Boolean),
  );

  let cursor = completedDates.has(today) ? today : previousDate(today);
  let streak = 0;

  while (streak < MAX_STREAK_DAYS && completedDates.has(cursor)) {
    streak += 1;
    cursor = previousDate(cursor);
  }

  return streak;
}

export class CommitmentCollection {
  constructor({ categories, commitments, streakDays }) {
    this.categories = Object.freeze([...categories]);
    this.commitments = Object.freeze([...commitments]);
    this.streakDays = streakDays;
    Object.freeze(this);
  }

  static create({ categories = [], commitments = [], now = new Date() }) {
    return new CommitmentCollection({
      categories,
      commitments,
      streakDays: calculateStreak(commitments, toIsoDate(now)),
    });
  }

  filteredBy(filter) {
    if (filter === COMMITMENT_FILTERS.pending) {
      return this.commitments.filter(({ isPending }) => isPending);
    }
    if (filter === COMMITMENT_FILTERS.completed) {
      return this.commitments.filter(({ isCompleted }) => isCompleted);
    }
    return this.commitments;
  }

  withAdded(commitment, now = new Date()) {
    const commitments = [...this.commitments, commitment]
      .sort((left, right) => String(left.startHour).localeCompare(String(right.startHour)));

    return CommitmentCollection.create({
      categories: this.categories,
      commitments,
      now,
    });
  }

  withUpdated(commitment, now = new Date()) {
    return CommitmentCollection.create({
      categories: this.categories,
      commitments: this.commitments
        .map((current) => current.id === commitment.id ? commitment : current)
        .sort((left, right) => String(left.startHour).localeCompare(String(right.startHour))),
      now,
    });
  }

  without(commitmentId, now = new Date()) {
    return CommitmentCollection.create({
      categories: this.categories,
      commitments: this.commitments.filter(({ id }) => id !== commitmentId),
      now,
    });
  }
}
