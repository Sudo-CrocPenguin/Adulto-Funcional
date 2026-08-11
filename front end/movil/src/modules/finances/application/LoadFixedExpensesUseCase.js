import { FixedExpenseCollection } from '../domain/FixedExpenseCollection';

export class LoadFixedExpensesUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  async execute(session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    const [expenses, categories] = await Promise.all([
      this.repository.listFixedExpenses(session),
      this.repository.listCategories(session),
    ]);
    return FixedExpenseCollection.create({
      categories,
      expenses,
      now: this.clock(),
    });
  }
}
