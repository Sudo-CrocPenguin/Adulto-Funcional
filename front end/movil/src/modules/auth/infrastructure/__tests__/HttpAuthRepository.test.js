import { HttpAuthRepository } from '../HttpAuthRepository';

describe('HttpAuthRepository', () => {
  it('revoca la sesión actual con el access token', async () => {
    const apiClient = {
      delete: jest.fn().mockResolvedValue(null),
    };
    const repository = new HttpAuthRepository(apiClient);

    await repository.logout({
      accessToken: 'access-token',
      tokenType: 'Bearer',
    });

    expect(apiClient.delete).toHaveBeenCalledWith(
      '/api/auth/sessions/current',
      { headers: { Authorization: 'Bearer access-token' } },
    );
  });
});
