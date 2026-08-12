import {
  LogoutAccountError,
  LogoutAccountUseCase,
} from '../LogoutAccountUseCase';

const session = {
  accessToken: 'access-token',
  tokenType: 'Bearer',
};

function setup() {
  const authRepository = {
    logout: jest.fn().mockResolvedValue(),
  };
  const sessionStore = {
    clear: jest.fn().mockResolvedValue(),
  };
  const useCase = new LogoutAccountUseCase({ authRepository, sessionStore });

  return { authRepository, sessionStore, useCase };
}

describe('LogoutAccountUseCase', () => {
  it('elimina la sesión persistida e inicia la revocación remota', async () => {
    const { authRepository, sessionStore, useCase } = setup();

    await expect(useCase.execute(session)).resolves.toBeUndefined();

    expect(sessionStore.clear).toHaveBeenCalledTimes(1);
    expect(authRepository.logout).toHaveBeenCalledWith(session);
  });

  it('permite salir localmente cuando el servidor no está disponible', async () => {
    const { authRepository, sessionStore, useCase } = setup();
    authRepository.logout.mockRejectedValue(new Error('network'));

    await expect(useCase.execute(session)).resolves.toBeUndefined();

    expect(sessionStore.clear).toHaveBeenCalledTimes(1);
  });

  it('no espera la respuesta remota después de limpiar la sesión local', async () => {
    const { authRepository, sessionStore, useCase } = setup();
    authRepository.logout.mockReturnValue(new Promise(() => {}));

    await expect(useCase.execute(session)).resolves.toBeUndefined();

    expect(sessionStore.clear).toHaveBeenCalledTimes(1);
    expect(authRepository.logout).toHaveBeenCalledWith(session);
  });

  it('permite salir si el servidor revocó la sesión aunque falle SecureStore', async () => {
    const { sessionStore, useCase } = setup();
    sessionStore.clear.mockRejectedValue(new Error('keychain'));

    await expect(useCase.execute(session)).resolves.toBeUndefined();
  });

  it('conserva la sesión visible cuando fallan la revocación y la limpieza local', async () => {
    const { authRepository, sessionStore, useCase } = setup();
    authRepository.logout.mockRejectedValue(new Error('network'));
    sessionStore.clear.mockRejectedValue(new Error('keychain'));

    await expect(useCase.execute(session)).rejects.toBeInstanceOf(LogoutAccountError);
  });
});
