import { ApiError } from '../../../../core/http/ApiError';
import { RestoreSessionUseCase } from '../RestoreSessionUseCase';

function setup({ storedToken = 'refresh-anterior' } = {}) {
  const session = { refreshToken: 'refresh-rotado' };
  const authRepository = {
    refresh: jest.fn().mockResolvedValue(session),
  };
  const sessionStore = {
    clear: jest.fn().mockResolvedValue(),
    getRefreshToken: jest.fn().mockResolvedValue(storedToken),
    saveRefreshToken: jest.fn().mockResolvedValue(),
  };
  const useCase = new RestoreSessionUseCase({ authRepository, sessionStore });

  return { authRepository, session, sessionStore, useCase };
}

describe('RestoreSessionUseCase', () => {
  it('mantiene la sesión cerrada cuando no existe refresh token', async () => {
    const { authRepository, sessionStore, useCase } = setup({ storedToken: null });

    await expect(useCase.execute()).resolves.toBeNull();
    expect(authRepository.refresh).not.toHaveBeenCalled();
    expect(sessionStore.saveRefreshToken).not.toHaveBeenCalled();
  });

  it('rota y reemplaza el refresh token al restaurar la sesión', async () => {
    const { authRepository, session, sessionStore, useCase } = setup();

    await expect(useCase.execute()).resolves.toBe(session);
    expect(authRepository.refresh).toHaveBeenCalledWith('refresh-anterior');
    expect(sessionStore.saveRefreshToken).toHaveBeenCalledWith('refresh-rotado');
  });

  it('elimina una sesión rechazada definitivamente por la API', async () => {
    const { authRepository, sessionStore, useCase } = setup();
    const error = new ApiError({
      code: 'REFRESH_TOKEN_REUSED',
      message: 'La sesión fue revocada',
      status: 401,
    });
    authRepository.refresh.mockRejectedValue(error);

    await expect(useCase.execute()).rejects.toBe(error);
    expect(sessionStore.clear).toHaveBeenCalledTimes(1);
  });

  it('conserva el token si la red falla temporalmente', async () => {
    const { authRepository, sessionStore, useCase } = setup();
    const error = new ApiError({
      code: 'NETWORK_ERROR',
      message: 'Sin conexión',
    });
    authRepository.refresh.mockRejectedValue(error);

    await expect(useCase.execute()).rejects.toBe(error);
    expect(sessionStore.clear).not.toHaveBeenCalled();
  });
});
