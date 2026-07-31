/**
 * Tipos relacionados con la autenticación.
 *
 * @author Miguel Angel Blandon Montes
 */

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  names: string;
  lastnames: string;
  phone: string;
  email: string;
  password: string;
  masterKey?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  accountId: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
  hasMasterKey: boolean;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}