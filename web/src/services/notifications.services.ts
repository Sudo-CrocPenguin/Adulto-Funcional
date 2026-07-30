import api from './api.config';

/**
 * Representa una notificacion del sistema.
 * Las notificaciones informan al usuario sobre vencimientos,
 * recordatorios y eventos importantes.
 */
export interface Notification {
  id: number;
  category: string;
  message: string;
  createdAt: string;
  read: boolean;
}

const BASE = '/notifications';

/**
 * Servicio para gestion de notificaciones del usuario.
 * Permite obtener, marcar como leidas y eliminar notificaciones.
 * Todas las peticiones requieren autenticacion JWT.
 */
export const notificationsService = {
    /**
   * Obtiene todas las notificaciones del usuario autenticado.
   * Las notificaciones se devuelven ordenadas por fecha, las mas recientes primero.
   * 
   * @returns {Promise<Notification[]>} Lista de notificaciones
   * @throws {Error} Si el token ha expirado o no es valido
   * 
   * @example
   * const notifications = await notificationsService.getAll();
   * const unread = notifications.filter(n => !n.read);
   */
  getAll: async (): Promise<Notification[]> => {
    const { data } = await api.get<Notification[]>(BASE);
    return data;
  },

  /**
   * Marca una notificacion especifica como leida.
   * 
   * @param {number} id - Identificador de la notificacion
   * @returns {Promise<void>}
   * @throws {Error} Si la notificacion no existe o el token no es valido
   * 
   * @example
   * await notificationsService.markAsRead(5);
   */
  markAsRead: async (id: number): Promise<void> => {
    await api.patch(`${BASE}/${id}`, { read: true });
  },

  /**
   * Marca todas las notificaciones como leidas.
   * Util cuando el usuario quiere limpiar todas las notificaciones pendientes.
   * 
   * @returns {Promise<void>}
   * @throws {Error} Si el token no es valido
   * 
   * @example
   * await notificationsService.markAllAsRead();
   */
  markAllAsRead: async (): Promise<void> => {
    await api.patch(`${BASE}/read-all`, {});
  },

  /**
   * Elimina una notificacion permanentemente.
   * 
   * @param {number} id - Identificador de la notificacion a eliminar
   * @returns {Promise<void>}
   * @throws {Error} Si la notificacion no existe o el token no es valido
   * 
   * @example
   * await notificationsService.delete(5);
   */
  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE}/${id}`);
  }
};

export default notificationsService;