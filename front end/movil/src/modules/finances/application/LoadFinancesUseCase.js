import { FinanceLedger } from '../domain/FinanceLedger';

export class LoadFinancesUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute(session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    const [movements, categories] = await Promise.all([
      this.repository.listMovements(session),
      this.repository.listCategories(session),
    ]);
    return FinanceLedger.create({ categories, movements });
  }
}
