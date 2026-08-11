export class PasswordVault {
  constructor(credentials = []) {
    this.credentials = Object.freeze([...credentials]);
    Object.freeze(this);
  }

  static create(credentials = []) {
    return new PasswordVault(
      [...credentials].sort((left, right) => (
        left.applicationName.localeCompare(right.applicationName, 'es', { sensitivity: 'base' })
      )),
    );
  }

  withCredential(credential) {
    const existing = this.credentials.some(({ id }) => id === credential.id);
    const credentials = existing
      ? this.credentials.map((current) => current.id === credential.id ? credential : current)
      : [...this.credentials, credential];
    return PasswordVault.create(credentials);
  }

  withoutCredential(credentialId) {
    return PasswordVault.create(
      this.credentials.filter(({ id }) => id !== credentialId),
    );
  }

  changeNotifications(clock = new Date(), limit = 3) {
    return this.credentials
      .filter((credential) => credential.needsChangeAt(clock))
      .slice(0, limit)
      .map((credential) => ({
        date: credential.lastChangeDate,
        id: `password:${credential.id}`,
        message: 'Han pasado al menos dos meses desde el último cambio.',
        subject: credential.applicationName,
        title: 'Contraseñas',
        type: 'password',
      }));
  }
}
