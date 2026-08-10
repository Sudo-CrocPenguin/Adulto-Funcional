import { FinanceMovement } from '../domain/FinanceMovement';
import { FinanceRepository } from '../domain/FinanceRepository';
import { FixedExpense } from '../domain/FixedExpense';

const PAGE_SIZE = 100;

function queryPath(path, parameters) {
  const query = Object.entries(parameters)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
  return query ? `${path}?${query}` : path;
}

export class HttpFinanceRepository extends FinanceRepository {
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
    let page = 0;
    let hasNext = false;

    do {
      const response = await this.apiClient.getPage(queryPath(path, {
        ...parameters,
        page,
        size: PAGE_SIZE,
      }), {
        headers: this.authorization(session),
      });
      items.push(...response.items);
      hasNext = Boolean(response.page?.hasNext);
      page += 1;
    } while (hasNext);

    return items;
  }

  async listMovements(session) {
    const movements = await this.getAll('/api/finances/movements', {
      sortBy: 'movementDate',
      sortDirection: 'DESC',
    }, session);
    return movements.map((movement) => FinanceMovement.fromApi(movement));
  }

  async listFixedExpenses(session) {
    const expenses = await this.getAll('/api/finances/fixed-expenses', {
      sortBy: 'nextDueDate',
      sortDirection: 'ASC',
    }, session);
    return expenses.map((expense) => FixedExpense.fromApi(expense));
  }

  listCategories(session) {
    return this.getAll('/api/finances/categories', {
      sortBy: 'name',
      sortDirection: 'ASC',
      type: 'FINANCES',
    }, session);
  }

  async createMovement(draft, session) {
    const movement = await this.apiClient.post(
      '/api/finances/movements',
      draft.toRequest(),
      { headers: this.authorization(session) },
    );
    return FinanceMovement.fromApi(movement);
  }

  async createFixedExpense(draft, session) {
    const expense = await this.apiClient.post(
      '/api/finances/fixed-expenses',
      draft.toRequest(),
      { headers: this.authorization(session) },
    );
    return FixedExpense.fromApi(expense);
  }

  async updateFixedExpense(id, changes, session) {
    const expense = await this.apiClient.patch(
      `/api/finances/fixed-expenses/${encodeURIComponent(id)}`,
      changes,
      { headers: this.authorization(session) },
    );
    return FixedExpense.fromApi(expense);
  }
}
