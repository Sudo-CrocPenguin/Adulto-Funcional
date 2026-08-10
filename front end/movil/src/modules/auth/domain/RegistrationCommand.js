const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const E164_PHONE_PATTERN = /^\+[1-9]\d{7,14}$/;

export class RegistrationValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'RegistrationValidationError';
    this.fieldErrors = fieldErrors;
  }
}

export class RegistrationCommand {
  constructor({ names, lastnames, phone, email, password }) {
    this.names = names.trim();
    this.lastnames = lastnames.trim();
    this.phone = phone.trim();
    this.email = email.trim().toLowerCase();
    this.password = password;
    Object.freeze(this);
  }

  static create(form) {
    const values = {
      names: String(form.names ?? ''),
      lastnames: String(form.lastnames ?? ''),
      phone: String(form.phone ?? ''),
      email: String(form.email ?? ''),
      password: String(form.password ?? ''),
      confirmPassword: String(form.confirmPassword ?? ''),
    };
    const errors = {};

    if (!values.names.trim()) {
      errors.names = 'Escribe tus nombres.';
    } else if (values.names.trim().length > 50) {
      errors.names = 'Los nombres no pueden superar 50 caracteres.';
    }

    if (!values.lastnames.trim()) {
      errors.lastnames = 'Escribe tus apellidos.';
    } else if (values.lastnames.trim().length > 50) {
      errors.lastnames = 'Los apellidos no pueden superar 50 caracteres.';
    }

    if (!E164_PHONE_PATTERN.test(values.phone.trim())) {
      errors.phone = 'Usa formato internacional, por ejemplo +573001234567.';
    }

    if (!EMAIL_PATTERN.test(values.email.trim())) {
      errors.email = 'Escribe un correo electrónico válido.';
    } else if (values.email.trim().length > 255) {
      errors.email = 'El correo no puede superar 255 caracteres.';
    }

    if (values.password.length < 15 || values.password.length > 128) {
      errors.password = 'La contraseña debe tener entre 15 y 128 caracteres.';
    }

    if (values.confirmPassword !== values.password) {
      errors.confirmPassword = 'Las contraseñas no coinciden.';
    }

    if (Object.keys(errors).length > 0) {
      throw new RegistrationValidationError(errors);
    }

    return new RegistrationCommand(values);
  }

  toRequest() {
    return {
      names: this.names,
      lastnames: this.lastnames,
      phone: this.phone,
      email: this.email,
      password: this.password,
    };
  }
}

