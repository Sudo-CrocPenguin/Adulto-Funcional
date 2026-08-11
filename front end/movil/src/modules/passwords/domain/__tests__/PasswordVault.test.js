import { PasswordVault } from '../PasswordVault';
import { VaultAccess } from '../VaultAccess';
import { VaultCredential } from '../VaultCredential';

function credential(id, applicationName, lastChangeDate) {
  return VaultCredential.fromApi({ applicationName, id, lastChangeDate });
}

describe('PasswordVault y VaultAccess', () => {
  it('ordena, reemplaza y elimina credenciales de manera inmutable', () => {
    const vault = PasswordVault.create([
      credential('2', 'Spotify', '2026-08-01'),
      credential('1', 'Netflix', '2026-08-01'),
    ]);
    const updated = vault.withCredential(credential('1', 'GitHub', '2026-08-10'));

    expect(vault.credentials.map(({ applicationName }) => applicationName))
      .toEqual(['Netflix', 'Spotify']);
    expect(updated.credentials.map(({ applicationName }) => applicationName))
      .toEqual(['GitHub', 'Spotify']);
    expect(updated.withoutCredential('2').credentials).toHaveLength(1);
  });

  it('genera avisos después de dos meses sin exponer contraseñas', () => {
    const vault = PasswordVault.create([
      credential('old', 'Antigua', '2026-05-01'),
      credential('new', 'Reciente', '2026-08-01'),
    ]);

    expect(vault.changeNotifications(new Date(2026, 7, 11))).toEqual([
      expect.objectContaining({ id: 'password:old', subject: 'Antigua' }),
    ]);
  });

  it('considera la expiración pública al determinar el desbloqueo', () => {
    const access = VaultAccess.fromApi({
      configured: true,
      expiresAt: '2026-08-11T15:00:00.000Z',
      verified: true,
    });

    expect(access.isUnlockedAt(new Date('2026-08-11T14:59:00.000Z'))).toBe(true);
    expect(access.isUnlockedAt(new Date('2026-08-11T15:01:00.000Z'))).toBe(false);
  });
});
