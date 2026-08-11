import {
  CreateCredentialDraft,
  CredentialValidationError,
  UpdateCredentialDraft,
  utf8ByteLength,
} from '../CredentialDraft';
import { passwordStrength, VaultCredential } from '../VaultCredential';

describe('borradores de credenciales', () => {
  const credential = VaultCredential.fromApi({
    applicationName: 'Netflix',
    id: 'credential-1',
    lastChangeDate: '2026-08-10',
  });

  it('crea una solicitud sin fecha ficticia para que el servidor asigne hoy', () => {
    const draft = CreateCredentialDraft.create({
      applicationName: ' Netflix ',
      password: 'Secreto-2026!',
    });

    expect(draft.toRequest()).toEqual({
      applicationName: 'Netflix',
      password: 'Secreto-2026!',
    });
  });

  it('actualiza únicamente los campos que cambiaron', () => {
    expect(UpdateCredentialDraft.create({
      applicationName: 'Netflix Premium',
      password: '',
    }, credential).toRequest()).toEqual({ applicationName: 'Netflix Premium' });
  });

  it('rechaza HTML, secretos vacíos y actualizaciones sin cambios', () => {
    expect(() => CreateCredentialDraft.create({
      applicationName: '<b>Netflix</b>',
      password: '',
    })).toThrow(CredentialValidationError);
    expect(() => UpdateCredentialDraft.create({
      applicationName: 'Netflix',
      password: '',
    }, credential)).toThrow(expect.objectContaining({
      fieldErrors: { form: expect.any(String) },
    }));
  });

  it('mide el límite UTF-8 y clasifica fuerza solo cuando el secreto está en memoria', () => {
    expect(utf8ByteLength('A🔐')).toBe(5);
    expect(passwordStrength('').label).toBe('Cifrada');
    expect(passwordStrength('Una-Clave-Muy-Segura-2026!')).toMatchObject({
      label: 'Fuerte',
      level: 3,
    });
  });
});
