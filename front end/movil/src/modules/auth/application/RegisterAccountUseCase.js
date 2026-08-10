import {
  RegistrationCommand,
} from '../domain/RegistrationCommand';
import { SessionPersistenceError } from './SessionPersistenceError';

export { SessionPersistenceError } from './SessionPersistenceError';

export class RegisterAccountUseCase {
  constructor({ authRepository, sessionStore }) {
    this.authRepository = authRepository;
    this.sessionStore = sessionStore;
  }

  async execute(form) {
    const command = RegistrationCommand.create(form);
    const session = await this.authRepository.register(command);

    try {
      await this.sessionStore.saveRefreshToken(session.refreshToken);
    } catch (error) {
      throw new SessionPersistenceError(session, error);
    }

    return session;
  }
}
