import { FixedExpenseDraft } from '../domain/FixedExpenseDraft';

export class CreateFixedExpenseUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  execute(form, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    const draft = FixedExpenseDraft.create(form, this.clock());
    return this.repository.createFixedExpense(draft, session);
  }
}
