import { CommitmentCollection } from '../domain/CommitmentCollection';

export class LoadCommitmentsUseCase {
  constructor(commitmentRepository, clock = () => new Date()) {
    this.commitmentRepository = commitmentRepository;
    this.clock = clock;
  }

  async execute(session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }

    const [commitments, categories] = await Promise.all([
      this.commitmentRepository.list(session),
      this.commitmentRepository.listCategories(session),
    ]);

    return CommitmentCollection.create({
      categories,
      commitments,
      now: this.clock(),
    });
  }
}
