import { Commitment, COMMITMENT_FILTERS } from '../Commitment';
import { CommitmentCollection } from '../CommitmentCollection';

function commitment(id, eventDate, status, startHour = '09:00:00') {
  return Commitment.fromApi({
    eventDate,
    frequency: 0,
    id,
    startHour: `${eventDate}T${startHour}`,
    status,
    title: id,
  });
}

describe('CommitmentCollection', () => {
  it('calcula la racha y aplica los filtros visuales', () => {
    const collection = CommitmentCollection.create({
      commitments: [
        commitment('today', '2026-08-10', 'Completado'),
        commitment('yesterday', '2026-08-09', 'Completado'),
        commitment('before', '2026-08-08', 'Completado'),
        commitment('pending', '2026-08-11', 'Pendiente'),
      ],
      now: new Date(2026, 7, 10, 12),
    });

    expect(collection.streakDays).toBe(3);
    expect(collection.filteredBy(COMMITMENT_FILTERS.pending)).toHaveLength(1);
    expect(collection.filteredBy(COMMITMENT_FILTERS.completed)).toHaveLength(3);
    expect(collection.filteredBy(COMMITMENT_FILTERS.all)).toHaveLength(4);
  });

  it('incorpora un compromiso nuevo conservando el orden temporal', () => {
    const collection = CommitmentCollection.create({
      commitments: [commitment('later', '2026-08-12', 'Pendiente')],
      now: new Date(2026, 7, 10, 12),
    });
    const next = collection.withAdded(
      commitment('sooner', '2026-08-11', 'Pendiente'),
      new Date(2026, 7, 10, 12),
    );

    expect(next.commitments.map(({ id }) => id)).toEqual(['sooner', 'later']);
    expect(collection.commitments).toHaveLength(1);
  });

  it('actualiza y elimina compromisos sin mutar la colección original', () => {
    const original = commitment('event-1', '2026-08-11', 'Pendiente');
    const updated = commitment('event-1', '2026-08-11', 'Completado');
    const collection = CommitmentCollection.create({
      commitments: [original],
      now: new Date(2026, 7, 10, 12),
    });

    const afterUpdate = collection.withUpdated(updated, new Date(2026, 7, 10, 12));
    const afterDelete = afterUpdate.without('event-1', new Date(2026, 7, 10, 12));

    expect(afterUpdate.commitments[0].isCompleted).toBe(true);
    expect(afterDelete.commitments).toHaveLength(0);
    expect(collection.commitments[0].isPending).toBe(true);
  });
});
