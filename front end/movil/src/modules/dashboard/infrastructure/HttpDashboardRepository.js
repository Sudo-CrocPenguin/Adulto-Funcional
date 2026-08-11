import { DashboardRepository } from '../domain/DashboardRepository';
import { DashboardSnapshot } from '../domain/DashboardSnapshot';

const PAGE_SIZE = 100;
const STREAK_WINDOW_DAYS = 30;

function toIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function daysBefore(date, days) {
  const result = new Date(date);
  result.setDate(result.getDate() - days);
  return toIsoDate(result);
}

function queryPath(path, parameters) {
  const query = Object.entries(parameters)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');

  return query ? `${path}?${query}` : path;
}

export class HttpDashboardRepository extends DashboardRepository {
  constructor(apiClient, clock = () => new Date()) {
    super();
    this.apiClient = apiClient;
    this.clock = clock;
  }

  authorization(session) {
    return {
      Authorization: `${session.tokenType || 'Bearer'} ${session.accessToken}`,
    };
  }

  getPage(path, parameters, session) {
    return this.apiClient.getPage(queryPath(path, parameters), {
      headers: this.authorization(session),
    });
  }

  async getAll(path, parameters, session) {
    const items = [];
    let pageNumber = 0;
    let hasNext = false;

    do {
      const response = await this.getPage(path, {
        ...parameters,
        page: pageNumber,
        size: PAGE_SIZE,
      }, session);
      items.push(...response.items);
      hasNext = Boolean(response.page?.hasNext);
      pageNumber += 1;
    } while (hasNext);

    return items;
  }

  async load(session) {
    const now = this.clock();
    const today = toIsoDate(now);
    const streakStart = daysBefore(now, STREAK_WINDOW_DAYS);

    const [
      movements,
      fixedExpenses,
      pendingCommitments,
      upcomingCommitments,
      completedEvents,
      masterKeyStatus,
    ] = await Promise.all([
      this.getAll('/api/finances/movements', {
        sortBy: 'movementDate',
        sortDirection: 'DESC',
      }, session),
      this.getPage('/api/finances/fixed-expenses', {
        page: 0,
        size: 1,
        sortBy: 'nextDueDate',
        sortDirection: 'ASC',
        status: 'ACTIVE',
      }, session),
      this.getPage('/api/agenda/events', {
        page: 0,
        size: 1,
        status: 'Pendiente',
      }, session),
      this.getPage('/api/agenda/events', {
        page: 0,
        size: 1,
        sortBy: 'startHour',
        sortDirection: 'ASC',
        startDate: today,
        status: 'Pendiente',
      }, session),
      this.getAll('/api/agenda/events', {
        endDate: today,
        sortBy: 'eventDate',
        sortDirection: 'DESC',
        startDate: streakStart,
        status: 'Completado',
      }, session),
      this.apiClient.get('/api/security/master-key/status', {
        headers: this.authorization(session),
      }),
    ]);

    let passwordsCount = null;
    if (masterKeyStatus?.verified) {
      const passwords = await this.getPage('/api/security/passwords', {
        page: 0,
        size: 1,
      }, session);
      passwordsCount = passwords.page?.totalElements ?? passwords.items.length;
    }

    return DashboardSnapshot.fromSources({
      completedEvents,
      fixedExpenses: fixedExpenses.items,
      movements,
      passwordsCount,
      pendingCommitmentsCount:
        pendingCommitments.page?.totalElements
        ?? pendingCommitments.items.length,
      today,
      upcomingCommitments: upcomingCommitments.items,
      upcomingExpensesCount:
        fixedExpenses.page?.totalElements
        ?? fixedExpenses.items.length,
      vaultConfigured: Boolean(masterKeyStatus?.configured),
      vaultVerified: Boolean(masterKeyStatus?.verified),
    });
  }
}
