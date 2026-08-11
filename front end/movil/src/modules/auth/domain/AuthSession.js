export class AuthSession {
  constructor(data) {
    this.accessToken = data.token;
    this.refreshToken = data.refreshToken;
    this.tokenType = data.tokenType || 'Bearer';
    this.expiresIn = data.expiresIn;
    this.refreshExpiresIn = data.refreshExpiresIn;
    this.sessionId = data.sessionId;
    this.accountId = data.accountId;
    this.names = data.names;
    this.lastnames = data.lastnames;
    this.email = data.email;
    this.phone = data.phone;
    this.createdAt = data.createdAt ?? null;
    this.roles = Array.isArray(data.roles) ? [...data.roles] : [];
    this.hasMasterKey = Boolean(data.hasMasterKey);
    Object.freeze(this.roles);
    Object.freeze(this);
  }

  static fromApi(data) {
    if (!data?.token || !data?.refreshToken || !data?.accountId) {
      throw new Error('La respuesta de autenticación nativa está incompleta.');
    }

    return new AuthSession(data);
  }

  withProfile(profile) {
    return new AuthSession({
      accountId: this.accountId,
      createdAt: profile.createdAt,
      email: profile.email,
      expiresIn: this.expiresIn,
      hasMasterKey: this.hasMasterKey,
      lastnames: profile.lastnames,
      names: profile.names,
      phone: profile.phone,
      refreshExpiresIn: this.refreshExpiresIn,
      refreshToken: this.refreshToken,
      roles: this.roles,
      sessionId: this.sessionId,
      token: this.accessToken,
      tokenType: this.tokenType,
    });
  }
}
