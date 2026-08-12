import {
  CommitmentDraft,
  CommitmentValidationError,
} from '../CommitmentDraft';
import { Commitment } from '../Commitment';

function validForm(overrides = {}) {
  return {
    categoryId: '01988e6b-0c00-7000-8000-000000000011',
    endTime: new Date(2026, 7, 11, 10, 0),
    eventDate: new Date(2026, 7, 11, 0, 0),
    frequency: 7,
    priority: 'Alta',
    reminderMinutes: 60,
    startTime: new Date(2026, 7, 11, 9, 0),
    title: 'Reunión de equipo',
    ...overrides,
  };
}

describe('CommitmentDraft', () => {
  it('serializa el horario civil exigido por la API de agenda', () => {
    const draft = CommitmentDraft.create(
      validForm(),
      new Date(2026, 7, 10, 12, 0),
    );

    expect(draft.toRequest()).toEqual({
      categoryId: '01988e6b-0c00-7000-8000-000000000011',
      endHour: '2026-08-11T10:00:00',
      eventDate: '2026-08-11',
      frequency: 7,
      priority: 'Alta',
      reminder: '2026-08-11T08:00:00',
      startHour: '2026-08-11T09:00:00',
      status: 'Pendiente',
      title: 'Reunión de equipo',
      zoneId: expect.any(String),
    });
  });

  it('rechaza catálogos, fechas y campos obligatorios inválidos', () => {
    expect(() => CommitmentDraft.create(
      validForm({
        categoryId: '',
        eventDate: new Date(2026, 7, 9),
        frequency: 2,
        priority: 'Urgente',
        reminderMinutes: 5,
        title: '',
      }),
      new Date(2026, 7, 10, 12, 0),
    )).toThrow(CommitmentValidationError);

    try {
      CommitmentDraft.create(
        validForm({
          categoryId: '',
          eventDate: new Date(2026, 7, 9),
          frequency: 2,
          priority: 'Urgente',
          reminderMinutes: 5,
          title: '',
        }),
        new Date(2026, 7, 10, 12, 0),
      );
    } catch (error) {
      expect(error.fieldErrors).toMatchObject({
        categoryId: expect.any(String),
        eventDate: expect.any(String),
        frequency: expect.any(String),
        priority: expect.any(String),
        reminderMinutes: expect.any(String),
        title: expect.any(String),
      });
    }
  });

  it('exige que la hora final sea posterior a la inicial', () => {
    expect(() => CommitmentDraft.create(
      validForm({ endTime: new Date(2026, 7, 11, 8, 30) }),
      new Date(2026, 7, 10, 12, 0),
    )).toThrow(expect.objectContaining({
      fieldErrors: { endTime: expect.any(String) },
    }));
  });

  it('construye un PATCH mínimo y permite editar un evento pasado sin mover su fecha', () => {
    const commitment = Commitment.fromApi({
      category: { id: '01988e6b-0c00-7000-8000-000000000011', name: 'Trabajo' },
      endHour: '2026-08-11T10:00:00',
      eventDate: '2026-08-11',
      frequency: 7,
      id: 'event-1',
      priority: 'Alta',
      reminder: '2026-08-11T08:00:00',
      startHour: '2026-08-11T09:00:00',
      status: 'Pendiente',
      title: 'Reunión de equipo',
      zoneId: 'America/Bogota',
    });
    const draft = CommitmentDraft.update(
      validForm({ status: 'Completado', title: 'Reunión terminada' }),
      commitment,
      new Date(2026, 7, 12, 12, 0),
    );

    expect(draft.toUpdateRequest(commitment)).toEqual({
      status: 'Completado',
      title: 'Reunión terminada',
    });
  });
});
