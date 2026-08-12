export class DeleteCommitmentUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute(commitmentId, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    if (!commitmentId) {
      throw new Error('El compromiso no tiene un identificador válido.');
    }
    await this.repository.delete(commitmentId, session);
    return commitmentId;
  }
}
