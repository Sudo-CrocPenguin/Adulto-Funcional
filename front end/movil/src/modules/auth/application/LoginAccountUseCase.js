import { LoginCommand } from '../domain/LoginCommand';
import { SessionPersistenceError } from './SessionPersistenceError';

export class LoginAccountUseCase {
  constructor({ authRepository, sessionStore }) {
    this.authRepository = authRepository;
    this.sessionStore = sessionStore;
  }

  async execute(form) {
    const command = LoginCommand.create(form);
    const session = await this.authRepository.login(command);

    try {
      if (command.rememberMe) {
        await this.sessionStore.saveRefreshToken(session.refreshToken);
      } else {
        await this.sessionStore.clear();
      }
    } catch (error) {
      throw new SessionPersistenceError(session, error);
    }

    return session;
  }
}

