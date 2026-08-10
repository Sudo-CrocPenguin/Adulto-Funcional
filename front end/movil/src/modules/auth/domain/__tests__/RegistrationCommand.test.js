import {
  RegistrationCommand,
  RegistrationValidationError,
} from '../RegistrationCommand';

const validForm = {
  names: '  Ana María  ',
  lastnames: " O'Connor Ruiz ",
  phone: ' +573001234567 ',
  email: ' ANA@EXAMPLE.COM ',
  password: 'frase-segura-15',
  confirmPassword: 'frase-segura-15',
};

describe('RegistrationCommand', () => {
  it('normaliza los datos y omite la confirmación del request', () => {
    const command = RegistrationCommand.create(validForm);

    expect(command.toRequest()).toEqual({
      names: 'Ana María',
      lastnames: "O'Connor Ruiz",
      phone: '+573001234567',
      email: 'ana@example.com',
      password: 'frase-segura-15',
    });
  });

  it('rechaza teléfono, contraseña y confirmación inválidos', () => {
    const invalidForm = {
      ...validForm,
      phone: '3001234567',
      password: 'corta',
      confirmPassword: 'diferente',
    };

    expect(() => RegistrationCommand.create(invalidForm)).toThrow(
      RegistrationValidationError,
    );

    try {
      RegistrationCommand.create(invalidForm);
    } catch (error) {
      expect(error.fieldErrors).toEqual(
        expect.objectContaining({
          phone: expect.any(String),
          password: expect.any(String),
          confirmPassword: expect.any(String),
        }),
      );
    }
  });
});

