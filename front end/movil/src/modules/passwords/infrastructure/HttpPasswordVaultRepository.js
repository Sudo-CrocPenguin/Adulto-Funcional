import { PasswordVaultRepository } from '../domain/PasswordVaultRepository';
import { VaultAccess } from '../domain/VaultAccess';
import { VaultCredential } from '../domain/VaultCredential';

const PAGE_SIZE = 100;

function queryPath(path, parameters) {
  const query = Object.entries(parameters)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
  return query ? `${path}?${query}` : path;
}

export class HttpPasswordVaultRepository extends PasswordVaultRepository {
  constructor(apiClient) {
    super();
    this.apiClient = apiClient;
  }

  authorization(session) {
    return {
      Authorization: `${session.tokenType || 'Bearer'} ${session.accessToken}`,
    };
  }

  async status(session) {
    const response = await this.apiClient.get('/api/security/master-key/status', {
      headers: this.authorization(session),
    });
    return VaultAccess.fromApi(response);
  }

  async configure(command, session) {
    const response = await this.apiClient.post(
      '/api/security/master-key',
      command.toRequest(),
      { headers: this.authorization(session) },
    );
    return VaultAccess.fromApi(response);
  }

  async verify(command, session) {
    const response = await this.apiClient.post(
      '/api/security/master-key/verify',
      command.toRequest(),
      { headers: this.authorization(session) },
    );
    return VaultAccess.fromApi(response);
  }

  async changeMasterKey(command, session) {
    const response = await this.apiClient.patch(
      '/api/security/master-key',
      command.toRequest(),
      { headers: this.authorization(session) },
    );
    return VaultAccess.fromApi(response);
  }

  async lock(session) {
    const response = await this.apiClient.delete('/api/security/master-key/session', {
      headers: this.authorization(session),
    });
    return VaultAccess.fromApi(response);
  }

  async listCredentials(session) {
    const credentials = [];
    let page = 0;
    let hasNext = false;
    do {
      const response = await this.apiClient.getPage(queryPath('/api/security/passwords', {
        page,
        size: PAGE_SIZE,
        sortBy: 'applicationName',
        sortDirection: 'ASC',
      }), { headers: this.authorization(session) });
      credentials.push(...response.items.map((item) => VaultCredential.fromApi(item)));
      hasNext = Boolean(response.page?.hasNext);
      page += 1;
    } while (hasNext);
    return credentials;
  }

  async getCredential(id, session) {
    const response = await this.apiClient.get(
      `/api/security/passwords/${encodeURIComponent(id)}`,
      { headers: this.authorization(session) },
    );
    return VaultCredential.fromApi(response);
  }

  async createCredential(draft, session) {
    const response = await this.apiClient.post(
      '/api/security/passwords',
      draft.toRequest(),
      { headers: this.authorization(session) },
    );
    return VaultCredential.fromApi(response);
  }

  async updateCredential(id, draft, session) {
    const response = await this.apiClient.patch(
      `/api/security/passwords/${encodeURIComponent(id)}`,
      draft.toRequest(),
      { headers: this.authorization(session) },
    );
    return VaultCredential.fromApi(response);
  }

  deleteCredential(id, session) {
    return this.apiClient.delete(
      `/api/security/passwords/${encodeURIComponent(id)}`,
      { headers: this.authorization(session) },
    );
  }
}
