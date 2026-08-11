import {
  LoadVaultUseCase,
  VerifyMasterKeyUseCase,
} from '../VaultUseCases';
import { VaultAccess } from '../../domain/VaultAccess';
import { VaultCredential } from '../../domain/VaultCredential';

const session = { accessToken: 'access-token', tokenType: 'Bearer' };

describe('casos de uso de bóveda', () => {
  it('no consulta credenciales cuando la bóveda está bloqueada', async () => {
    const repository = {
      listCredentials: jest.fn(),
      status: jest.fn().mockResolvedValue(VaultAccess.fromApi({ configured: true, verified: false })),
    };
    const useCase = new LoadVaultUseCase(repository);

    const snapshot = await useCase.execute(session);

    expect(snapshot.access.verified).toBe(false);
    expect(snapshot.vault.credentials).toEqual([]);
    expect(repository.listCredentials).not.toHaveBeenCalled();
  });

  it('verifica primero y después carga el listado no sensible', async () => {
    const calls = [];
    const repository = {
      listCredentials: jest.fn(async () => {
        calls.push('list');
        return [VaultCredential.fromApi({ applicationName: 'Netflix', id: '1' })];
      }),
      verify: jest.fn(async (command) => {
        calls.push('verify');
        expect(command.toRequest()).toEqual({ masterKey: 'Master-Key-2026' });
        return VaultAccess.fromApi({ configured: true, verified: true });
      }),
    };
    const useCase = new VerifyMasterKeyUseCase(repository);

    const snapshot = await useCase.execute({ masterKey: 'Master-Key-2026' }, session);

    expect(calls).toEqual(['verify', 'list']);
    expect(snapshot.vault.credentials).toHaveLength(1);
  });
});
