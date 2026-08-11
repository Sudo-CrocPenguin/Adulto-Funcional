export class VaultValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'VaultValidationError';
    this.fieldErrors = fieldErrors;
  }
}

function requireValue(value, field, label, errors, maximum = 128) {
  const normalized = String(value ?? '');
  if (!normalized) {
    errors[field] = `${label} es obligatoria.`;
  } else if (normalized.length > maximum) {
    errors[field] = `${label} no puede superar ${maximum} caracteres.`;
  }
  return normalized;
}

function validateNewMasterKey(form, errors) {
  const newMasterKey = requireValue(
    form.newMasterKey,
    'newMasterKey',
    'La nueva Master Key',
    errors,
  );
  if (newMasterKey && newMasterKey.length < 15) {
    errors.newMasterKey = 'La Master Key debe tener entre 15 y 128 caracteres.';
  }
  const confirmation = String(form.confirmation ?? '');
  if (!confirmation) {
    errors.confirmation = 'Confirma la Master Key.';
  } else if (newMasterKey !== confirmation) {
    errors.confirmation = 'Las Master Keys no coinciden.';
  }
  return newMasterKey;
}

export class VerifyMasterKeyCommand {
  constructor(masterKey) {
    this.masterKey = masterKey;
    Object.freeze(this);
  }

  static create(form) {
    const errors = {};
    const masterKey = requireValue(form.masterKey, 'masterKey', 'La Master Key', errors);
    if (Object.keys(errors).length) {
      throw new VaultValidationError(errors);
    }
    return new VerifyMasterKeyCommand(masterKey);
  }

  toRequest() {
    return { masterKey: this.masterKey };
  }
}

export class ConfigureMasterKeyCommand {
  constructor({ currentPassword, newMasterKey }) {
    this.currentPassword = currentPassword;
    this.newMasterKey = newMasterKey;
    Object.freeze(this);
  }

  static create(form) {
    const errors = {};
    const currentPassword = requireValue(
      form.currentPassword,
      'currentPassword',
      'La contraseña actual',
      errors,
    );
    const newMasterKey = validateNewMasterKey(form, errors);
    if (Object.keys(errors).length) {
      throw new VaultValidationError(errors);
    }
    return new ConfigureMasterKeyCommand({ currentPassword, newMasterKey });
  }

  toRequest() {
    return {
      currentPassword: this.currentPassword,
      newMasterKey: this.newMasterKey,
    };
  }
}

export class ChangeMasterKeyCommand {
  constructor({ currentMasterKey, currentPassword, newMasterKey }) {
    this.currentMasterKey = currentMasterKey;
    this.currentPassword = currentPassword;
    this.newMasterKey = newMasterKey;
    Object.freeze(this);
  }

  static create(form) {
    const errors = {};
    const currentPassword = requireValue(
      form.currentPassword,
      'currentPassword',
      'La contraseña actual',
      errors,
    );
    const currentMasterKey = requireValue(
      form.currentMasterKey,
      'currentMasterKey',
      'La Master Key actual',
      errors,
    );
    const newMasterKey = validateNewMasterKey(form, errors);
    if (currentMasterKey && currentMasterKey === newMasterKey) {
      errors.newMasterKey = 'La nueva Master Key debe ser diferente de la actual.';
    }
    if (Object.keys(errors).length) {
      throw new VaultValidationError(errors);
    }
    return new ChangeMasterKeyCommand({ currentMasterKey, currentPassword, newMasterKey });
  }

  toRequest() {
    return {
      currentMasterKey: this.currentMasterKey,
      currentPassword: this.currentPassword,
      newMasterKey: this.newMasterKey,
    };
  }
}
