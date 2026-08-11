export class UserProfile {
  constructor({ createdAt, email, id, lastnames, names, phone }) {
    this.createdAt = createdAt ?? null;
    this.email = email ?? '';
    this.id = id ?? null;
    this.lastnames = lastnames ?? '';
    this.names = names ?? '';
    this.phone = phone ?? '';
    Object.freeze(this);
  }

  static fromApi(data) {
    return new UserProfile(data ?? {});
  }

  get fullName() {
    return [this.names, this.lastnames].filter(Boolean).join(' ').trim() || 'Usuario';
  }

  get initials() {
    const parts = this.fullName.split(/\s+/u).filter(Boolean);
    if (!parts.length) return 'U';
    return `${parts[0][0] ?? ''}${parts.length > 1 ? parts.at(-1)[0] ?? '' : ''}`
      .toLocaleUpperCase('es-CO');
  }
}
