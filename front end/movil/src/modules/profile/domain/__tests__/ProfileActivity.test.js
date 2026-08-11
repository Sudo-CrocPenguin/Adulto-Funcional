import { maximumCommitmentStreak, ProfileActivity } from '../ProfileActivity';

describe('ProfileActivity', () => {
  it('calcula la mayor racha histórica por días únicos consecutivos', () => {
    const commitments = [
      { eventDate: '2026-02-01' },
      { eventDate: '2026-02-02' },
      { eventDate: '2026-02-02' },
      { eventDate: '2026-02-04' },
      { eventDate: '2026-02-05' },
      { eventDate: '2026-02-06' },
    ];

    expect(maximumCommitmentStreak(commitments)).toBe(3);
    expect(ProfileActivity.fromSources({ commitments, fixedExpensesCount: 8, passwordsCount: 2 }))
      .toMatchObject({
        completedCommitments: 6,
        fixedExpensesCount: 8,
        maximumStreakDays: 3,
        passwordsCount: 2,
      });
  });

  it('conserva el contador de contraseñas como desconocido si la bóveda está bloqueada', () => {
    expect(ProfileActivity.fromSources({ passwordsCount: null }).passwordsCount).toBeNull();
  });
});
