import { CommitmentDraft } from '../domain/CommitmentDraft';

export class UpdateCommitmentUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  execute(commitment, form, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    const draft = CommitmentDraft.update(form, commitment, this.clock());
    return this.repository.update(
      commitment.id,
      draft.toUpdateRequest(commitment),
      session,
    );
  }
}
