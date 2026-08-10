import { AuthRepository } from '../domain/AuthRepository';
import { AuthSession } from '../domain/AuthSession';

export class HttpAuthRepository extends AuthRepository {
  constructor(apiClient) {
    super();
    this.apiClient = apiClient;
  }

  async register(command) {
    const data = await this.apiClient.post(
      '/api/auth/register',
      command.toRequest(),
    );

    return AuthSession.fromApi(data);
  }
}

