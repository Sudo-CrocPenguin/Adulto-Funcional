import api from './api.config';

/**
 * Datos de la cuenta del usuario.
 * GET /api/account/{id}
 */
export interface Account {
  id: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
  hasMasterKey: boolean;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

/**
 * Servicio para obtener datos de la cuenta.
 * Solo se usa en el Layout para mostrar el nombre del usuario.
 */
export const accountService = {
  /**
   * Obtiene los datos de una cuenta por ID.
   * GET /api/account/{id}
   */
  getById: async (id: string): Promise<Account> => {
    const { data: body } = await api.get<ApiResponse<Account>>(`/account/${id}`);
    return body.data;
  },

  /**
   * Elimina una cuenta por ID.
   * DELETE /api/account/{id}
   * Retorna 501 Not Implemented por ahora.
   */
  remove: async (id: string): Promise<void> => {
    await api.delete(`/account/${id}`);
  },
  
};