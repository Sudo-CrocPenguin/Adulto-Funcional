export class VaultAccess {
  constructor({ configured, expiresAt, verified }) {
    this.configured = Boolean(configured);
    this.expiresAt = expiresAt || null;
    this.verified = Boolean(verified);
    Object.freeze(this);
  }

  static fromApi(data) {
    return new VaultAccess({
      configured: data?.configured,
      expiresAt: data?.expiresAt,
      verified: data?.verified,
    });
  }

  isUnlockedAt(clock = new Date()) {
    if (!this.verified) {
      return false;
    }
    if (!this.expiresAt) {
      return true;
    }
    const expiration = new Date(this.expiresAt);
    return !Number.isNaN(expiration.getTime()) && expiration > clock;
  }
}
