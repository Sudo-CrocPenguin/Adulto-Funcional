import { LoginCommand, LoginValidationError } from '../LoginCommand';

describe('LoginCommand', () => {
  it('normaliza el correo y conserva la contraseña histórica', () => {
    const command = LoginCommand.create({
      email: ' ANA@EXAMPLE.COM ',
      password: 'corta-pero-existente',
      rememberMe: true,
    });

    expect(command.toRequest()).toEqual({
      email: 'ana@example.com',
      password: 'corta-pero-existente',
    });
    expect(command.rememberMe).toBe(true);
  });

  it('rechaza credenciales vacías o con email inválido', () => {
    expect(() =>
      LoginCommand.create({ email: 'invalido', password: '' }),
    ).toThrow(LoginValidationError);
  });
});

