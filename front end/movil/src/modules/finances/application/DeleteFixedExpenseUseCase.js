export class DeleteFixedExpenseUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute(expenseId, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    if (!expenseId) {
      throw new Error('El gasto fijo no tiene un identificador válido.');
    }
    await this.repository.deleteFixedExpense(expenseId, session);
    return expenseId;
  }
}
