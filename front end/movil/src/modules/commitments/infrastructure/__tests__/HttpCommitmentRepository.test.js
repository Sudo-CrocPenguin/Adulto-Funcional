import { HttpCommitmentRepository } from '../HttpCommitmentRepository';

const session = {
  accessToken: 'access-token',
  tokenType: 'Bearer',
};

function page(items, hasNext = false) {
  return { items, page: { hasNext } };
}

describe('HttpCommitmentRepository', () => {
  it('recorre las páginas autenticadas de eventos y categorías AGENDA', async () => {
    const apiClient = {
      getPage: jest.fn()
        .mockResolvedValueOnce(page([{
          id: 'event-1',
          status: 'Pendiente',
          title: 'Reunión',
        }], true))
        .mockResolvedValueOnce(page([{
          id: 'event-2',
          status: 'Completado',
          title: 'Presentación',
        }]))
        .mockResolvedValueOnce(page([{
          id: 'category-1',
          name: 'Trabajo',
          type: 'AGENDA',
        }], false)),
    };
    const repository = new HttpCommitmentRepository(apiClient);

    const events = await repository.list(session);
    const categories = await repository.listCategories(session);

    expect(events.map(({ id }) => id)).toEqual(['event-1', 'event-2']);
    expect(categories).toEqual([expect.objectContaining({ id: 'category-1' })]);
    expect(apiClient.getPage).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/agenda/events?'),
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(apiClient.getPage.mock.calls[1][0]).toContain('page=1');
    expect(apiClient.getPage.mock.calls[2][0]).toContain('type=AGENDA');
  });

  it('envía el comando validado al endpoint de creación', async () => {
    const request = {
      categoryId: 'category-1',
      eventDate: '2026-08-11',
      title: 'Reunión',
    };
    const apiClient = {
      post: jest.fn().mockResolvedValue({
        ...request,
        id: 'event-1',
        status: 'Pendiente',
      }),
    };
    const repository = new HttpCommitmentRepository(apiClient);

    const result = await repository.create({
      toRequest: () => request,
    }, session);

    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/agenda/events',
      request,
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(result).toMatchObject({ id: 'event-1', title: 'Reunión' });
  });

  it('actualiza y elimina un compromiso por su endpoint autenticado', async () => {
    const apiClient = {
      delete: jest.fn().mockResolvedValue(null),
      patch: jest.fn().mockResolvedValue({
        id: 'event/1',
        status: 'Completado',
        title: 'Reunión',
      }),
    };
    const repository = new HttpCommitmentRepository(apiClient);

    const result = await repository.update('event/1', { status: 'Completado' }, session);
    await repository.delete('event/1', session);

    expect(apiClient.patch).toHaveBeenCalledWith(
      '/api/agenda/events/event%2F1',
      { status: 'Completado' },
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(apiClient.delete).toHaveBeenCalledWith(
      '/api/agenda/events/event%2F1',
      { headers: { Authorization: 'Bearer access-token' } },
    );
    expect(result).toMatchObject({ id: 'event/1', status: 'Completado' });
  });
});
