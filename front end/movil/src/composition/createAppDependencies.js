import { environment } from '../core/config/environment';
import { ApiClient } from '../core/http/ApiClient';
import { AsyncThemePreferenceStore } from '../core/preferences/AsyncThemePreferenceStore';
import { SecureSessionStore } from '../core/storage/SecureSessionStore';
import { LoginAccountUseCase } from '../modules/auth/application/LoginAccountUseCase';
import { RegisterAccountUseCase } from '../modules/auth/application/RegisterAccountUseCase';
import { RestoreSessionUseCase } from '../modules/auth/application/RestoreSessionUseCase';
import { HttpAuthRepository } from '../modules/auth/infrastructure/HttpAuthRepository';
import { CreateCommitmentUseCase } from '../modules/commitments/application/CreateCommitmentUseCase';
import { LoadCommitmentsUseCase } from '../modules/commitments/application/LoadCommitmentsUseCase';
import { HttpCommitmentRepository } from '../modules/commitments/infrastructure/HttpCommitmentRepository';
import { LoadDashboardUseCase } from '../modules/dashboard/application/LoadDashboardUseCase';
import { HttpDashboardRepository } from '../modules/dashboard/infrastructure/HttpDashboardRepository';

export function createAppDependencies() {
  const apiClient = new ApiClient({
    baseUrl: environment.apiUrl,
    timeoutMs: environment.requestTimeoutMs,
  });
  const sessionStore = new SecureSessionStore();
  const authRepository = new HttpAuthRepository(apiClient);
  const commitmentRepository = new HttpCommitmentRepository(apiClient);
  const dashboardRepository = new HttpDashboardRepository(apiClient);
  const themePreferenceStore = new AsyncThemePreferenceStore();

  return Object.freeze({
    createCommitment: new CreateCommitmentUseCase(commitmentRepository),
    loginAccount: new LoginAccountUseCase({
      authRepository,
      sessionStore,
    }),
    loadDashboard: new LoadDashboardUseCase(dashboardRepository),
    loadCommitments: new LoadCommitmentsUseCase(commitmentRepository),
    registerAccount: new RegisterAccountUseCase({
      authRepository,
      sessionStore,
    }),
    restoreSession: new RestoreSessionUseCase({
      authRepository,
      sessionStore,
    }),
    themePreferenceStore,
  });
}
