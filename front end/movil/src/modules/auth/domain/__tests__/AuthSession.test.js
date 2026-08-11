import { AuthSession } from '../AuthSession';

const response = {
  accountId: 'account-id',
  createdAt: '2026-02-02T10:00:00Z',
  email: 'ana@example.com',
  lastnames: 'Ruiz',
  names: 'Ana',
  phone: '+573001234567',
  refreshToken: 'refresh-token',
  token: 'access-token',
};

describe('AuthSession', () => {
  it('sincroniza datos del perfil sin sustituir los tokens', () => {
    const session = AuthSession.fromApi(response);

    const updated = session.withProfile({
      ...response,
      email: 'nueva@example.com',
      names: 'Ana María',
    });

    expect(updated).toMatchObject({
      accessToken: 'access-token',
      createdAt: '2026-02-02T10:00:00Z',
      email: 'nueva@example.com',
      names: 'Ana María',
      refreshToken: 'refresh-token',
    });
    expect(session.email).toBe('ana@example.com');
  });
});
