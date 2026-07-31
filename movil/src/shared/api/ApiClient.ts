import { secureSessionStorage } from '@/src/auth/infrastructure/SecureSessionStorage';
import {
  ApiResponse,
  AuthResponse,
  CategoryResponse,
  EventResponse,
  FixedExpenseResponse,
  MovementResponse,
  PasswordResponse,
} from './types';

type LoginPayload = {
  email: string;
  password: string;
};

type RegisterPayload = LoginPayload & {
  names: string;
  lastnames: string;
  phone: string;
  masterKey?: string;
};

export class ApiClient {
  constructor(private readonly baseUrl: string) {}

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const auth = await this.request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }, false);
    await this.persistNativeToken(auth);
    return auth;
  }

  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const auth = await this.request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    }, false);
    await this.persistNativeToken(auth);
    return auth;
  }

  async logout(): Promise<void> {
    await this.request<void>('/api/auth/logout', { method: 'POST' });
    await secureSessionStorage.clearToken();
  }

  async listCategories(): Promise<CategoryResponse[]> {
    return this.request<CategoryResponse[]>('/api/finances/categories');
  }

  async listMovements(): Promise<MovementResponse[]> {
    return this.request<MovementResponse[]>('/api/finances/movements');
  }

  async listEvents(): Promise<EventResponse[]> {
    return this.request<EventResponse[]>('/api/agenda/events');
  }

  async listFixedExpenses(): Promise<FixedExpenseResponse[]> {
    return this.request<FixedExpenseResponse[]>('/api/finances/fixed-expenses');
  }

  async verifyMasterKey(masterKey: string): Promise<void> {
    await this.request<void>('/api/security/passwords/master-key/verify', {
      method: 'POST',
      body: JSON.stringify({ masterKey }),
    });
  }

  async listPasswords(): Promise<PasswordResponse[]> {
    return this.request<PasswordResponse[]>('/api/security/passwords');
  }

  private async persistNativeToken(auth: AuthResponse): Promise<void> {
    if (auth.token) {
      await secureSessionStorage.saveToken(auth.token);
    }
  }

  private async request<T>(
    path: string,
    options: RequestInit = {},
    includeAuth = true,
  ): Promise<T> {
    const headers = new Headers(options.headers);
    headers.set('Accept', 'application/json');
    headers.set('Content-Type', 'application/json');
    headers.set('X-Client-Type', 'mobile');

    if (includeAuth) {
      const token = await secureSessionStorage.getToken();
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...options,
      headers,
      credentials: 'include',
    });
    const payload = await this.readPayload<T>(response);

    if (!response.ok) {
      throw new Error(payload?.message ?? `Error HTTP ${response.status}`);
    }

    return payload?.data as T;
  }

  private async readPayload<T>(response: Response): Promise<ApiResponse<T> | null> {
    const text = await response.text();
    if (!text) {
      return null;
    }

    return JSON.parse(text) as ApiResponse<T>;
  }
}
