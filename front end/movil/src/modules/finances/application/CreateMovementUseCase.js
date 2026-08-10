import { MovementDraft } from '../domain/MovementDraft';

export class CreateMovementUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(form, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    return this.repository.createMovement(MovementDraft.create(form), session);
  }
}
