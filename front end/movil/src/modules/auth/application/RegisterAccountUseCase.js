import {
  RegistrationCommand,
} from '../domain/RegistrationCommand';

export class SessionPersistenceError extends Error {
  constructor(session, cause) {
    super('La cuenta fue creada, pero no pudimos guardar la sesión.', { cause });
    this.name = 'SessionPersistenceError';
    this.session = session;
  }
}

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

