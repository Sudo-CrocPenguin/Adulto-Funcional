export class LogoutAccountError extends Error {
  constructor(cause) {
    super('No pudimos cerrar la sesión de forma segura. Inténtalo nuevamente.', {
      cause,
    });
    this.name = 'LogoutAccountError';
  }
}

export class LogoutAccountUseCase {
  constructor({ authRepository, sessionStore }) {
    this.authRepository = authRepository;
    this.sessionStore = sessionStore;
  }

  async execute(session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }

    const remoteRevocation = Promise.resolve()
      .then(() => this.authRepository.logout(session))
      .then(() => true)
      .catch(() => false);

    try {
      await this.sessionStore.clear();
    } catch (error) {
      const revoked = await remoteRevocation;
      if (!revoked) {
        throw new LogoutAccountError(error);
      }
    }
  }
}
