import { environment } from '../core/config/environment';
import { ApiClient } from '../core/http/ApiClient';
import { SecureSessionStore } from '../core/storage/SecureSessionStore';
import { RegisterAccountUseCase } from '../modules/auth/application/RegisterAccountUseCase';
import { HttpAuthRepository } from '../modules/auth/infrastructure/HttpAuthRepository';

export function createAppDependencies() {
  const apiClient = new ApiClient({
    baseUrl: environment.apiUrl,
    timeoutMs: environment.requestTimeoutMs,
  });
  const sessionStore = new SecureSessionStore();
  const authRepository = new HttpAuthRepository(apiClient);

  return Object.freeze({
    registerAccount: new RegisterAccountUseCase({
      authRepository,
      sessionStore,
    }),
  });
}

