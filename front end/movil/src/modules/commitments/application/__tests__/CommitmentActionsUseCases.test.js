import { DeleteCommitmentUseCase } from '../DeleteCommitmentUseCase';
import { UpdateCommitmentUseCase } from '../UpdateCommitmentUseCase';
import { Commitment } from '../../domain/Commitment';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

function commitment() {
  return Commitment.fromApi({
    category: { id: 'category-1', name: 'Trabajo' },
    endHour: '2026-08-11T10:00:00',
    eventDate: '2026-08-11',
    frequency: 0,
    id: 'event-1',
    priority: 'Media',
    reminder: '2026-08-11T08:00:00',
    startHour: '2026-08-11T09:00:00',
    status: 'Pendiente',
    title: 'Reunión',
    zoneId: 'America/Bogota',
  });
}

describe('acciones de compromisos', () => {
  it('valida y actualiza solo los campos editados', async () => {
    const current = commitment();
    const repository = { update: jest.fn().mockResolvedValue(current) };
    const useCase = new UpdateCommitmentUseCase(
      repository,
      () => new Date(2026, 7, 10),
    );

    await useCase.execute(current, {
      categoryId: 'category-1',
      endTime: new Date(2026, 7, 11, 10),
      eventDate: new Date(2026, 7, 11),
      frequency: 0,
      priority: 'Media',
      reminderMinutes: 60,
      startTime: new Date(2026, 7, 11, 9),
      status: 'Completado',
      title: 'Reunión',
    }, session);

    expect(repository.update).toHaveBeenCalledWith(
      'event-1',
      { status: 'Completado' },
      session,
    );
  });

  it('elimina el compromiso y devuelve su identificador', async () => {
    const repository = { delete: jest.fn().mockResolvedValue(null) };
    const useCase = new DeleteCommitmentUseCase(repository);

    await expect(useCase.execute('event-1', session)).resolves.toBe('event-1');
    expect(repository.delete).toHaveBeenCalledWith('event-1', session);
  });
});
