import { AuthRepository } from '../domain/AuthRepository';
import { AuthSession } from '../domain/AuthSession';

export class HttpAuthRepository extends AuthRepository {
  constructor(apiClient) {
    super();
    this.apiClient = apiClient;
  }

  async login(command) {
    const data = await this.apiClient.post(
      '/api/auth/login',
      command.toRequest(),
    );

    return AuthSession.fromApi(data);
  }

  async register(command) {
    const data = await this.apiClient.post(
      '/api/auth/register',
      command.toRequest(),
    );

    return AuthSession.fromApi(data);
  }

  async refresh(refreshToken) {
    const data = await this.apiClient.post('/api/auth/refresh', { refreshToken });

    return AuthSession.fromApi(data);
  }
}
