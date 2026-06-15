import api from './api.config';

/**
 * Credencial de contraseña almacenada.
 * GET /api/security/passwords/{id} retorna la contraseña descifrada.
 */
export interface PasswordCredential {
  id: string;
  applicationName: string;
  password?: string;
  lastChangeDate?: string;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

/**
 * Servicio para gestión de contraseñas cifradas.
 * Todos los endpoints requieren autenticación JWT.
 */
export const passwordService = {
  /**
   * Lista todas las credenciales del usuario autenticado.
   * GET /api/security/passwords
   */
  getAll: async (): Promise<PasswordCredential[]> => {
    const { data: body } = await api.get<ApiResponse<PasswordCredential[]>>('/security/passwords');
    return body.data;
  },

  /**
   * Obtiene una credencial descifrada por ID.
   * GET /api/security/passwords/{id}
   */
  getById: async (id: string): Promise<PasswordCredential> => {
    const { data: body } = await api.get<ApiResponse<PasswordCredential>>(`/security/passwords/${id}`);
    return body.data;
  },

  /**
   * Guarda una nueva credencial.
   * POST /api/security/passwords
   */
  create: async (payload: { applicationName: string; password: string }): Promise<PasswordCredential> => {
    const { data: body } = await api.post<ApiResponse<PasswordCredential>>('/security/passwords', payload);
    return body.data;
  },

  /**
   * Actualiza nombre o contraseña de una credencial.
   * PATCH /api/security/passwords/{id}
   */
  update: async (
    id: string,
    payload: Partial<Pick<PasswordCredential, 'applicationName' | 'password'>>,
  ): Promise<PasswordCredential> => {
    const { data: body } = await api.patch<ApiResponse<PasswordCredential>>(`/security/passwords/${id}`, payload);
    return body.data;
  },

  /**
   * Elimina una credencial.
   * DELETE /api/security/passwords/{id}
   */
  remove: async (id: string): Promise<void> => {
    await api.delete(`/security/passwords/${id}`);
  },
};
