import { ProfileValidationError, UpdateProfileDraft } from '../UpdateProfileDraft';
import { UserProfile } from '../UserProfile';

const profile = UserProfile.fromApi({
  email: 'ana@example.com',
  id: 'account-id',
  lastnames: 'O’Connor Ruiz',
  names: 'Ana María',
  phone: '+573001234567',
});

describe('UpdateProfileDraft', () => {
  it('normaliza y envía únicamente los campos modificados', () => {
    const draft = UpdateProfileDraft.create({
      email: ' NUEVO@EXAMPLE.COM ',
      lastnames: ' O’Connor Ruiz ',
      names: ' Ana María ',
      phone: ' +573001234567 ',
    }, profile);

    expect(draft.toRequest()).toEqual({ email: 'nuevo@example.com' });
  });

  it('acepta nombres Unicode y rechaza teléfono fuera de E.164', () => {
    expect(() => UpdateProfileDraft.create({
      email: profile.email,
      lastnames: 'Muñoz-Díaz',
      names: 'Łucja María',
      phone: '3001234567',
    }, profile)).toThrow(ProfileValidationError);

    try {
      UpdateProfileDraft.create({
        email: profile.email,
        lastnames: 'Muñoz-Díaz',
        names: 'Łucja María',
        phone: '3001234567',
      }, profile);
    } catch (error) {
      expect(error.fieldErrors.phone).toEqual(expect.any(String));
      expect(error.fieldErrors.names).toBeUndefined();
    }
  });

  it('impide enviar un formulario sin cambios', () => {
    expect(() => UpdateProfileDraft.create(profile, profile)).toThrow(ProfileValidationError);
  });
});
