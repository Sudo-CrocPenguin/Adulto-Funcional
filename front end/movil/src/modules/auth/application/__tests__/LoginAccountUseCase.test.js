import { LoginAccountUseCase } from '../LoginAccountUseCase';
import { SessionPersistenceError } from '../SessionPersistenceError';

const baseForm = {
  email: 'ana@example.com',
  password: 'contraseña-existente',
};

function setup() {
  const session = { refreshToken: 'refresh-token' };
  const authRepository = { login: jest.fn().mockResolvedValue(session) };
  const sessionStore = {
    clear: jest.fn().mockResolvedValue(),
    saveRefreshToken: jest.fn().mockResolvedValue(),
  };
  const useCase = new LoginAccountUseCase({ authRepository, sessionStore });

  return { authRepository, session, sessionStore, useCase };
}

describe('LoginAccountUseCase', () => {
  it('persiste el refresh token cuando Recuérdame está activo', async () => {
    const { session, sessionStore, useCase } = setup();

    await expect(
      useCase.execute({ ...baseForm, rememberMe: true }),
    ).resolves.toBe(session);
    expect(sessionStore.saveRefreshToken).toHaveBeenCalledWith('refresh-token');
    expect(sessionStore.clear).not.toHaveBeenCalled();
  });

  it('elimina persistencia previa cuando Recuérdame está inactivo', async () => {
    const { sessionStore, useCase } = setup();

    await useCase.execute({ ...baseForm, rememberMe: false });

    expect(sessionStore.clear).toHaveBeenCalledTimes(1);
    expect(sessionStore.saveRefreshToken).not.toHaveBeenCalled();
  });

  it('distingue un fallo local posterior al login correcto', async () => {
    const { session, sessionStore, useCase } = setup();
    sessionStore.saveRefreshToken.mockRejectedValue(new Error('keychain'));

    await expect(
      useCase.execute({ ...baseForm, rememberMe: true }),
    ).rejects.toMatchObject({
      name: SessionPersistenceError.name,
      session,
    });
  });
});

