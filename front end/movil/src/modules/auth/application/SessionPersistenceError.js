export class SessionPersistenceError extends Error {
  constructor(session, cause) {
    super('La autenticación terminó, pero no pudimos guardar la sesión.', {
      cause,
    });
    this.name = 'SessionPersistenceError';
    this.session = session;
  }
}

