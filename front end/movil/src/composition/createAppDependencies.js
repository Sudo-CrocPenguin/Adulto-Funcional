import { environment } from '../core/config/environment';
import { ApiClient } from '../core/http/ApiClient';
import { AsyncThemePreferenceStore } from '../core/preferences/AsyncThemePreferenceStore';
import { SecureSessionStore } from '../core/storage/SecureSessionStore';
import { LoginAccountUseCase } from '../modules/auth/application/LoginAccountUseCase';
import { LogoutAccountUseCase } from '../modules/auth/application/LogoutAccountUseCase';
import { RegisterAccountUseCase } from '../modules/auth/application/RegisterAccountUseCase';
import { RestoreSessionUseCase } from '../modules/auth/application/RestoreSessionUseCase';
import { HttpAuthRepository } from '../modules/auth/infrastructure/HttpAuthRepository';
import { CreateCommitmentUseCase } from '../modules/commitments/application/CreateCommitmentUseCase';
import { DeleteCommitmentUseCase } from '../modules/commitments/application/DeleteCommitmentUseCase';
import { LoadCommitmentsUseCase } from '../modules/commitments/application/LoadCommitmentsUseCase';
import { UpdateCommitmentUseCase } from '../modules/commitments/application/UpdateCommitmentUseCase';
import { HttpCommitmentRepository } from '../modules/commitments/infrastructure/HttpCommitmentRepository';
import { CreateFixedExpenseUseCase } from '../modules/finances/application/CreateFixedExpenseUseCase';
import { DeleteFixedExpenseUseCase } from '../modules/finances/application/DeleteFixedExpenseUseCase';
import { CreateMovementUseCase } from '../modules/finances/application/CreateMovementUseCase';
import { LoadFinancesUseCase } from '../modules/finances/application/LoadFinancesUseCase';
import { LoadFixedExpensesUseCase } from '../modules/finances/application/LoadFixedExpensesUseCase';
import { PayFixedExpenseUseCase } from '../modules/finances/application/PayFixedExpenseUseCase';
import { UpdateFixedExpenseUseCase } from '../modules/finances/application/UpdateFixedExpenseUseCase';
import { HttpFinanceRepository } from '../modules/finances/infrastructure/HttpFinanceRepository';
import { LoadDashboardUseCase } from '../modules/dashboard/application/LoadDashboardUseCase';
import { HttpDashboardRepository } from '../modules/dashboard/infrastructure/HttpDashboardRepository';
import {
  ChangeMasterKeyUseCase,
  ConfigureMasterKeyUseCase,
  CreateCredentialUseCase,
  DeleteCredentialUseCase,
  LoadVaultUseCase,
  LockVaultUseCase,
  RevealCredentialUseCase,
  UpdateCredentialUseCase,
  VerifyMasterKeyUseCase,
} from '../modules/passwords/application/VaultUseCases';
import { HttpPasswordVaultRepository } from '../modules/passwords/infrastructure/HttpPasswordVaultRepository';
import { LoadProfileUseCase, UpdateProfileUseCase } from '../modules/profile/application/ProfileUseCases';
import { HttpProfileRepository } from '../modules/profile/infrastructure/HttpProfileRepository';
import { EnsureLatestUpdateUseCase } from '../modules/updates/application/EnsureLatestUpdateUseCase';
import { ExpoApplicationUpdateRepository } from '../modules/updates/infrastructure/ExpoApplicationUpdateRepository';

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
  const passwordVaultRepository = new HttpPasswordVaultRepository(apiClient);
  const profileRepository = new HttpProfileRepository(apiClient);
  const themePreferenceStore = new AsyncThemePreferenceStore();
  const applicationUpdateRepository = new ExpoApplicationUpdateRepository();

  return Object.freeze({
    changeMasterKey: new ChangeMasterKeyUseCase(passwordVaultRepository),
    configureMasterKey: new ConfigureMasterKeyUseCase(passwordVaultRepository),
    createCommitment: new CreateCommitmentUseCase(commitmentRepository),
    createFixedExpense: new CreateFixedExpenseUseCase(financeRepository),
    createMovement: new CreateMovementUseCase(financeRepository),
    createCredential: new CreateCredentialUseCase(passwordVaultRepository),
    deleteCommitment: new DeleteCommitmentUseCase(commitmentRepository),
    deleteCredential: new DeleteCredentialUseCase(passwordVaultRepository),
    deleteFixedExpense: new DeleteFixedExpenseUseCase(financeRepository),
    ensureLatestUpdate: new EnsureLatestUpdateUseCase(applicationUpdateRepository),
    loginAccount: new LoginAccountUseCase({
      authRepository,
      sessionStore,
    }),
    logoutAccount: new LogoutAccountUseCase({
      authRepository,
      sessionStore,
    }),
    loadDashboard: new LoadDashboardUseCase(dashboardRepository),
    loadCommitments: new LoadCommitmentsUseCase(commitmentRepository),
    loadFinances: new LoadFinancesUseCase(financeRepository),
    loadFixedExpenses: new LoadFixedExpensesUseCase(financeRepository),
    loadVault: new LoadVaultUseCase(passwordVaultRepository),
    loadProfile: new LoadProfileUseCase(profileRepository),
    lockVault: new LockVaultUseCase(passwordVaultRepository),
    payFixedExpense: new PayFixedExpenseUseCase(financeRepository),
    revealCredential: new RevealCredentialUseCase(passwordVaultRepository),
    registerAccount: new RegisterAccountUseCase({
      authRepository,
      sessionStore,
    }),
    restoreSession: new RestoreSessionUseCase({
      authRepository,
      sessionStore,
    }),
    themePreferenceStore,
    updateCredential: new UpdateCredentialUseCase(passwordVaultRepository),
    updateCommitment: new UpdateCommitmentUseCase(commitmentRepository),
    updateFixedExpense: new UpdateFixedExpenseUseCase(financeRepository),
    updateProfile: new UpdateProfileUseCase(profileRepository),
    verifyMasterKey: new VerifyMasterKeyUseCase(passwordVaultRepository),
  });
}
