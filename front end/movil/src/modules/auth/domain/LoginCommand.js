const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export class LoginValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'LoginValidationError';
    this.fieldErrors = fieldErrors;
  }
}

export class LoginCommand {
  constructor({ email, password, rememberMe }) {
    this.email = email.trim().toLowerCase();
    this.password = password;
    this.rememberMe = Boolean(rememberMe);
    Object.freeze(this);
  }

  static create(form) {
    const values = {
      email: String(form.email ?? ''),
      password: String(form.password ?? ''),
      rememberMe: Boolean(form.rememberMe),
    };
    const errors = {};

    if (!EMAIL_PATTERN.test(values.email.trim())) {
      errors.email = 'Escribe un correo electrónico válido.';
    } else if (values.email.trim().length > 255) {
      errors.email = 'El correo no puede superar 255 caracteres.';
    }

    if (!values.password) {
      errors.password = 'Escribe tu contraseña.';
    } else if (values.password.length > 128) {
      errors.password = 'La contraseña no puede superar 128 caracteres.';
    }

    if (Object.keys(errors).length > 0) {
      throw new LoginValidationError(errors);
    }

    return new LoginCommand(values);
  }

  toRequest() {
    return {
      email: this.email,
      password: this.password,
    };
  }
}

