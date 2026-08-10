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
import { CreateFixedExpenseUseCase } from '../modules/finances/application/CreateFixedExpenseUseCase';
import { CreateMovementUseCase } from '../modules/finances/application/CreateMovementUseCase';
import { LoadFinancesUseCase } from '../modules/finances/application/LoadFinancesUseCase';
import { LoadFixedExpensesUseCase } from '../modules/finances/application/LoadFixedExpensesUseCase';
import { PayFixedExpenseUseCase } from '../modules/finances/application/PayFixedExpenseUseCase';
import { HttpFinanceRepository } from '../modules/finances/infrastructure/HttpFinanceRepository';
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
  const financeRepository = new HttpFinanceRepository(apiClient);
  const themePreferenceStore = new AsyncThemePreferenceStore();

  return Object.freeze({
    createCommitment: new CreateCommitmentUseCase(commitmentRepository),
    createFixedExpense: new CreateFixedExpenseUseCase(financeRepository),
    createMovement: new CreateMovementUseCase(financeRepository),
    loginAccount: new LoginAccountUseCase({
      authRepository,
      sessionStore,
    }),
    loadDashboard: new LoadDashboardUseCase(dashboardRepository),
    loadCommitments: new LoadCommitmentsUseCase(commitmentRepository),
    loadFinances: new LoadFinancesUseCase(financeRepository),
    loadFixedExpenses: new LoadFixedExpensesUseCase(financeRepository),
    payFixedExpense: new PayFixedExpenseUseCase(financeRepository),
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
