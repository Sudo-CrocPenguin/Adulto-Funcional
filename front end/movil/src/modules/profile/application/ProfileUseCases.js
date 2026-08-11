import { UpdateProfileDraft } from '../domain/UpdateProfileDraft';

export class LoadProfileUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(session) {
    return this.repository.load(session.accountId, session);
  }
}

export class UpdateProfileUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  execute(currentProfile, form, session) {
    const draft = UpdateProfileDraft.create(form, currentProfile);
    return this.repository.update(currentProfile.id, draft, session);
  }
}
