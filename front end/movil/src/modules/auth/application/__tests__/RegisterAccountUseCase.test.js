import {
  RegisterAccountUseCase,
  SessionPersistenceError,
} from '../RegisterAccountUseCase';

const form = {
  names: 'Ana',
  lastnames: 'Pérez',
  phone: '+573001234567',
  email: 'ana@example.com',
  password: 'frase-segura-15',
  confirmPassword: 'frase-segura-15',
};

describe('RegisterAccountUseCase', () => {
  it('registra la cuenta y guarda solamente el refresh token', async () => {
    const session = {
      accountId: 'account-id',
      refreshToken: 'refresh-token',
    };
    const authRepository = { register: jest.fn().mockResolvedValue(session) };
    const sessionStore = { saveRefreshToken: jest.fn().mockResolvedValue() };
    const useCase = new RegisterAccountUseCase({
      authRepository,
      sessionStore,
    });

    await expect(useCase.execute(form)).resolves.toBe(session);
    expect(authRepository.register).toHaveBeenCalledTimes(1);
    expect(sessionStore.saveRefreshToken).toHaveBeenCalledWith('refresh-token');
  });

  it('informa que la cuenta existe si falla el almacenamiento local', async () => {
    const session = { refreshToken: 'refresh-token' };
    const useCase = new RegisterAccountUseCase({
      authRepository: { register: jest.fn().mockResolvedValue(session) },
      sessionStore: {
        saveRefreshToken: jest.fn().mockRejectedValue(new Error('keychain')),
      },
    });

    await expect(useCase.execute(form)).rejects.toMatchObject({
      name: SessionPersistenceError.name,
      session,
    });
  });
});

