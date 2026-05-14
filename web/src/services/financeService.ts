import api from './api.config';

/**
 * Representa un movimiento financiero (ingreso o egreso).
 */

export interface Movement {
  id: number;
  type: 'Ingreso' | 'Egreso';
  title: string;
  category: string;
  amount: number;
  date: string;
  description?: string;
}

/**
 * DTO para crear un nuevo movimiento.
 * Contiene todos los campos de Movement excepto el id,
 * que es generado automaticamente por el backend.
 */
export type CreateMovementDTO = Omit<Movement, 'id'>;

const BASE = '/finances/movements';
/**
 * Servicio para gestion de movimientos financieros.
 * Todas las peticiones requieren autenticacion JWT.
 */

export const financesService = {

  /**
   * Obtiene todos los movimientos del usuario autenticado.
   * 
   * @returns {Promise<Movement[]>} Lista de movimientos financieros
   * @throws {Error} Si el token ha expirado o no es valido
   * 
   * @example
   * const movements = await financesService.getAll();
   */
  getAll: async (): Promise<Movement[]> => {
    const { data } = await api.get<Movement[]>(BASE);
    return data;
  },

  /**
   * Obtiene un movimiento especifico por su ID.
   * 
   * @param {number} id - Identificador del movimiento
   * @returns {Promise<Movement>} Datos del movimiento solicitado
   * @throws {Error} Si el movimiento no existe o el token no es valido
   * 
   * @example
   * const movement = await financesService.getById(5);
   */
  getById: async (id: number): Promise<Movement> => {
    const { data } = await api.get<Movement>(`${BASE}/${id}`);
    return data;
  },

  /**
   * Crea un nuevo movimiento financiero.
   * 
   * @param {CreateMovementDTO} movement - Datos del movimiento a crear (sin id)
   * @returns {Promise<Movement>} Movimiento creado con el id asignado por el backend
   * @throws {Error} Si los datos son invalidos o el token no es valido
   * 
   * @example
   * const nuevo = await financesService.create({
   *   type: 'Ingreso',
   *   title: 'Salario',
   *   category: 'Trabajo',
   *   amount: 1500,
   *   date: '2026-05-14'
   * });
   */
  create: async (movement: CreateMovementDTO): Promise<Movement> => {
    const { data } = await api.post<Movement>(BASE, movement);
    return data;
  },

  /**
   * Actualiza parcialmente un movimiento existente.
   * Solo envia al backend los campos que se desean modificar.
   * 
   * @param {number} id - Identificador del movimiento a actualizar
   * @param {Partial<CreateMovementDTO>} changes - Campos a modificar
   * @returns {Promise<Movement>} Movimiento actualizado con todos sus campos
   * @throws {Error} Si el movimiento no existe o el token no es valido
   * 
   * @example
   * const actualizado = await financesService.update(5, {
   *   amount: 2000,
   *   description: 'Monto ajustado'
   * });
   */
  update: async (id: number, changes: Partial<CreateMovementDTO>): Promise<Movement> => {
    const { data } = await api.patch<Movement>(`${BASE}/${id}`, changes);
    return data;
  },

  /**
   * Elimina un movimiento financiero permanentemente.
   * Esta accion no se puede deshacer.
   * 
   * @param {number} id - Identificador del movimiento a eliminar
   * @returns {Promise<void>}
   * @throws {Error} Si el movimiento no existe o el token no es valido
   * 
   * @example
   * await financesService.delete(5);
   */
  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE}/${id}`);
  }
};

export default financesService;