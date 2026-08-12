import { Commitment } from '../domain/Commitment';
import { CommitmentRepository } from '../domain/CommitmentRepository';

const PAGE_SIZE = 100;

function queryPath(path, parameters) {
  const query = Object.entries(parameters)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');

  return query ? `${path}?${query}` : path;
}

export class HttpCommitmentRepository extends CommitmentRepository {
  constructor(apiClient) {
    super();
    this.apiClient = apiClient;
  }

  authorization(session) {
    return {
      Authorization: `${session.tokenType || 'Bearer'} ${session.accessToken}`,
    };
  }

  async getAll(path, parameters, session) {
    const items = [];
    let pageNumber = 0;
    let hasNext = false;

    do {
      const response = await this.apiClient.getPage(queryPath(path, {
        ...parameters,
        page: pageNumber,
        size: PAGE_SIZE,
      }), {
        headers: this.authorization(session),
      });
      items.push(...response.items);
      hasNext = Boolean(response.page?.hasNext);
      pageNumber += 1;
    } while (hasNext);

    return items;
  }

  async list(session) {
    const events = await this.getAll('/api/agenda/events', {
      sortBy: 'startHour',
      sortDirection: 'ASC',
    }, session);

    return events.map((event) => Commitment.fromApi(event));
  }

  listCategories(session) {
    return this.getAll('/api/finances/categories', {
      sortBy: 'name',
      sortDirection: 'ASC',
      type: 'AGENDA',
    }, session);
  }

  async create(draft, session) {
    const event = await this.apiClient.post(
      '/api/agenda/events',
      draft.toRequest(),
      { headers: this.authorization(session) },
    );

    return Commitment.fromApi(event);
  }

  async update(id, changes, session) {
    const event = await this.apiClient.patch(
      `/api/agenda/events/${encodeURIComponent(id)}`,
      changes,
      { headers: this.authorization(session) },
    );
    return Commitment.fromApi(event);
  }

  async delete(id, session) {
    await this.apiClient.delete(
      `/api/agenda/events/${encodeURIComponent(id)}`,
      { headers: this.authorization(session) },
    );
  }
}
