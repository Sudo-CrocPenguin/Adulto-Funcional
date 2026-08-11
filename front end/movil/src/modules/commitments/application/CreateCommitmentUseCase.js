import { CommitmentDraft } from '../domain/CommitmentDraft';

export class CreateCommitmentUseCase {
  constructor(commitmentRepository, clock = () => new Date()) {
    this.commitmentRepository = commitmentRepository;
    this.clock = clock;
  }

  execute(form, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }

    const draft = CommitmentDraft.create(form, this.clock());
    return this.commitmentRepository.create(draft, session);
  }
}
