import { ApiError } from '../../../core/http/ApiError';
import { ProfileActivity } from '../domain/ProfileActivity';
import { ProfileRepository } from '../domain/ProfileRepository';
import { ProfileSnapshot } from '../domain/ProfileSnapshot';
import { UserProfile } from '../domain/UserProfile';

const PAGE_SIZE = 100;

function queryPath(path, parameters) {
  const query = Object.entries(parameters)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
  return query ? `${path}?${query}` : path;
}

export class HttpProfileRepository extends ProfileRepository {
  constructor(apiClient) {
    super();
    this.apiClient = apiClient;
  }

  authorization(session) {
    return { Authorization: `${session.tokenType || 'Bearer'} ${session.accessToken}` };
  }

  getPage(path, parameters, session) {
    return this.apiClient.getPage(queryPath(path, parameters), {
      headers: this.authorization(session),
    });
  }

  async getAll(path, parameters, session) {
    const items = [];
    let page = 0;
    let hasNext = false;
    do {
      const response = await this.getPage(path, { ...parameters, page, size: PAGE_SIZE }, session);
      items.push(...response.items);
      hasNext = Boolean(response.page?.hasNext);
      page += 1;
    } while (hasNext);
    return items;
  }

  async passwordCount(session) {
    const status = await this.apiClient.get('/api/security/master-key/status', {
      headers: this.authorization(session),
    });
    if (!status?.verified) return null;

    try {
      const response = await this.getPage('/api/security/passwords', { page: 0, size: 1 }, session);
      return response.page?.totalElements ?? response.items.length;
    } catch (error) {
      if (error instanceof ApiError && error.code === 'MASTER_KEY_REQUIRED') return null;
      throw error;
    }
  }

  async load(accountId, session) {
    const headers = this.authorization(session);
    const [account, commitments, fixedExpenses, passwordsCount] = await Promise.all([
      this.apiClient.get(`/api/account/${encodeURIComponent(accountId)}`, { headers }),
      this.getAll('/api/agenda/events', {
        sortBy: 'eventDate',
        sortDirection: 'ASC',
        status: 'Completado',
      }, session),
      this.getPage('/api/finances/fixed-expenses', { page: 0, size: 1 }, session),
      this.passwordCount(session),
    ]);

    return new ProfileSnapshot({
      activity: ProfileActivity.fromSources({
        commitments,
        fixedExpensesCount: fixedExpenses.page?.totalElements ?? fixedExpenses.items.length,
        passwordsCount,
      }),
      profile: UserProfile.fromApi(account),
    });
  }

  async update(accountId, draft, session) {
    const account = await this.apiClient.patch(
      `/api/account/${encodeURIComponent(accountId)}`,
      draft.toRequest(),
      { headers: this.authorization(session) },
    );
    return UserProfile.fromApi(account);
  }
}
