import { ApiError } from '../../../core/http/ApiError';

const TERMINAL_SESSION_STATUSES = new Set([401, 409]);

export class RestoreSessionUseCase {
  constructor({ authRepository, sessionStore }) {
    this.authRepository = authRepository;
    this.sessionStore = sessionStore;
  }

  async execute() {
    const refreshToken = await this.sessionStore.getRefreshToken();

    if (!refreshToken) {
      return null;
    }

    try {
      const session = await this.authRepository.refresh(refreshToken);
      await this.sessionStore.saveRefreshToken(session.refreshToken);
      return session;
    } catch (error) {
      if (
        error instanceof ApiError
        && TERMINAL_SESSION_STATUSES.has(error.status)
      ) {
        await this.sessionStore.clear();
      }

      throw error;
    }
  }
}
