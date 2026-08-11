const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/u;
const E164_PHONE_PATTERN = /^\+[1-9]\d{7,14}$/u;
const HTML_PATTERN = /<[^>]*>|&(?:lt|gt|amp|quot|#\d+);/iu;
const PERSON_NAME_PATTERN = /^[\p{L}\p{M}]+(?:[ '\u2019-][\p{L}\p{M}]+)*$/u;

export class ProfileValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'ProfileValidationError';
    this.fieldErrors = fieldErrors;
  }
}

function validateName(value, field, label, errors) {
  const normalized = String(value ?? '').trim();
  if (!normalized) errors[field] = `Escribe ${label}.`;
  else if (normalized.length > 50) errors[field] = `${label} no puede superar 50 caracteres.`;
  else if (!PERSON_NAME_PATTERN.test(normalized) || HTML_PATTERN.test(normalized)) {
    errors[field] = `${label} solo admite letras, espacios, apóstrofes y guiones.`;
  }
  return normalized;
}

export class UpdateProfileDraft {
  constructor(request) {
    this.request = Object.freeze(request);
    Object.freeze(this);
  }

  static create(form, currentProfile) {
    const errors = {};
    const values = {
      email: String(form.email ?? '').trim().toLowerCase(),
      lastnames: validateName(form.lastnames, 'lastnames', 'tus apellidos', errors),
      names: validateName(form.names, 'names', 'tus nombres', errors),
      phone: String(form.phone ?? '').trim(),
    };

    if (!EMAIL_PATTERN.test(values.email)) errors.email = 'Escribe un correo electrónico válido.';
    else if (values.email.length > 255) errors.email = 'El correo no puede superar 255 caracteres.';
    else if (HTML_PATTERN.test(values.email)) errors.email = 'El correo no puede contener HTML.';

    if (!E164_PHONE_PATTERN.test(values.phone)) {
      errors.phone = 'Usa formato internacional, por ejemplo +573001234567.';
    }

    const request = Object.fromEntries(
      Object.entries(values).filter(([field, value]) => value !== currentProfile[field]),
    );

    if (!Object.keys(errors).length && !Object.keys(request).length) {
      errors.form = 'Modifica al menos un dato del perfil.';
    }
    if (Object.keys(errors).length) throw new ProfileValidationError(errors);

    return new UpdateProfileDraft(request);
  }

  toRequest() {
    return this.request;
  }
}
