import { FixedExpenseDraft } from '../domain/FixedExpenseDraft';

export class UpdateFixedExpenseUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  execute(expense, form, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    const draft = FixedExpenseDraft.update(form, expense, this.clock());
    return this.repository.updateFixedExpense(
      expense.id,
      draft.toUpdateRequest(expense),
      session,
    );
  }
}
