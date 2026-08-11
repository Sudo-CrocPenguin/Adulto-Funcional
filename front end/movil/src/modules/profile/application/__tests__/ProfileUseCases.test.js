import { LoadProfileUseCase, UpdateProfileUseCase } from '../ProfileUseCases';
import { UserProfile } from '../../domain/UserProfile';

const session = { accountId: 'account-id' };

describe('casos de uso de Perfil', () => {
  it('carga siempre el perfil de la cuenta autenticada', async () => {
    const repository = { load: jest.fn().mockResolvedValue({ profile: {} }) };
    const useCase = new LoadProfileUseCase(repository);

    await useCase.execute(session);

    expect(repository.load).toHaveBeenCalledWith('account-id', session);
  });

  it('valida los cambios antes de actualizar la cuenta', async () => {
    const profile = UserProfile.fromApi({
      email: 'ana@example.com',
      id: 'account-id',
      lastnames: 'Ruiz',
      names: 'Ana',
      phone: '+573001234567',
    });
    const repository = { update: jest.fn().mockResolvedValue(profile) };
    const useCase = new UpdateProfileUseCase(repository);

    await useCase.execute(profile, { ...profile, names: 'Ana María' }, session);

    expect(repository.update).toHaveBeenCalledWith(
      'account-id',
      expect.objectContaining({ toRequest: expect.any(Function) }),
      session,
    );
    expect(repository.update.mock.calls[0][1].toRequest()).toEqual({ names: 'Ana María' });
  });
});
