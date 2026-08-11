import {
  ChangeMasterKeyCommand,
  ConfigureMasterKeyCommand,
  VaultValidationError,
  VerifyMasterKeyCommand,
} from '../VaultCommands';

describe('comandos de Master Key', () => {
  it('serializa la configuración reautenticada exigida por el backend', () => {
    const command = ConfigureMasterKeyCommand.create({
      confirmation: 'Master-Key-Segura-2026',
      currentPassword: 'contraseña de cuenta',
      newMasterKey: 'Master-Key-Segura-2026',
    });

    expect(command.toRequest()).toEqual({
      currentPassword: 'contraseña de cuenta',
      newMasterKey: 'Master-Key-Segura-2026',
    });
  });

  it('permite verificar claves históricas sin imponer el mínimo de creación', () => {
    expect(VerifyMasterKeyCommand.create({ masterKey: 'histórica' }).toRequest())
      .toEqual({ masterKey: 'histórica' });
  });

  it('rechaza confirmación distinta y una nueva clave menor de 15 caracteres', () => {
    expect(() => ConfigureMasterKeyCommand.create({
      confirmation: 'otra',
      currentPassword: '',
      newMasterKey: 'corta',
    })).toThrow(VaultValidationError);
  });

  it('exige una nueva Master Key diferente durante la rotación', () => {
    expect(() => ChangeMasterKeyCommand.create({
      confirmation: 'Master-Key-Igual-2026',
      currentMasterKey: 'Master-Key-Igual-2026',
      currentPassword: 'contraseña de cuenta',
      newMasterKey: 'Master-Key-Igual-2026',
    })).toThrow(expect.objectContaining({
      fieldErrors: { newMasterKey: expect.any(String) },
    }));
  });
});
