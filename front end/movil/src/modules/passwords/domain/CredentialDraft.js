const HTML_PATTERN = /<[^>]*>|&(?:lt|gt|amp|quot|#\d+);/iu;
const MAX_SECRET_BYTES = 2032;

export class CredentialValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'CredentialValidationError';
    this.fieldErrors = fieldErrors;
  }
}

export function utf8ByteLength(value) {
  let bytes = 0;
  for (const character of String(value ?? '')) {
    const codePoint = character.codePointAt(0);
    if (codePoint <= 0x7f) bytes += 1;
    else if (codePoint <= 0x7ff) bytes += 2;
    else if (codePoint <= 0xffff) bytes += 3;
    else bytes += 4;
  }
  return bytes;
}

function validateName(value, errors) {
  const applicationName = String(value ?? '').trim();
  if (!applicationName) {
    errors.applicationName = 'Escribe el nombre de la aplicación.';
  } else if (applicationName.length > 35) {
    errors.applicationName = 'El nombre no puede superar 35 caracteres.';
  } else if (HTML_PATTERN.test(applicationName)) {
    errors.applicationName = 'El nombre no puede contener HTML.';
  }
  return applicationName;
}

function validateSecret(value, errors, required) {
  const password = String(value ?? '');
  if (required && !password) {
    errors.password = 'Escribe la contraseña que deseas proteger.';
  } else if (password && utf8ByteLength(password) > MAX_SECRET_BYTES) {
    errors.password = 'La contraseña no puede superar 2032 bytes en UTF-8.';
  }
  return password;
}

export class CreateCredentialDraft {
  constructor({ applicationName, password }) {
    this.applicationName = applicationName;
    this.password = password;
    Object.freeze(this);
  }

  static create(form) {
    const errors = {};
    const applicationName = validateName(form.applicationName, errors);
    const password = validateSecret(form.password, errors, true);
    if (Object.keys(errors).length) {
      throw new CredentialValidationError(errors);
    }
    return new CreateCredentialDraft({ applicationName, password });
  }

  toRequest() {
    return {
      applicationName: this.applicationName,
      password: this.password,
    };
  }
}

export class UpdateCredentialDraft {
  constructor(request) {
    this.request = Object.freeze(request);
    Object.freeze(this);
  }

  static create(form, credential) {
    const errors = {};
    const applicationName = validateName(form.applicationName, errors);
    const password = validateSecret(form.password, errors, false);
    const request = {};
    if (applicationName && applicationName !== credential.applicationName) {
      request.applicationName = applicationName;
    }
    if (password) {
      request.password = password;
    }
    if (!Object.keys(errors).length && !Object.keys(request).length) {
      errors.form = 'Cambia el nombre o escribe una nueva contraseña.';
    }
    if (Object.keys(errors).length) {
      throw new CredentialValidationError(errors);
    }
    return new UpdateCredentialDraft(request);
  }

  toRequest() {
    return this.request;
  }
}
