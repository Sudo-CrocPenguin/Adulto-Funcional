import api from './api.config';

/**
 * Representa un compromiso o tarea del usuario.
 * Los compromisos pueden ser de una sola vez o recurrentes.
 */
export interface Commitment {
  id: number;
  name: string;
  category: 'Trabajo' | 'Hogar' | 'Personal' | 'Salud' | 'Finanzas' | 'Otros';
  frequency: 'Una vez' | 'Diario' | 'Semanal' | 'Mensual';
  priority: 'Alta' | 'Media' | 'Baja';
  date: string;
  reminder: string;
  description: string;
  completed: boolean;
  ceased: boolean;
}

/**
 * DTO para crear un nuevo compromiso.
 * Excluye id, completed y ceased que son gestionados por el backend.
 */
export type CreateCommitmentDTO = Omit<Commitment, 'id' | 'completed' | 'ceased'>;

/**
 * DTO para actualizar parcialmente un compromiso.
 * Permite modificar cualquier campo excepto el id,
 * incluyendo los estados completed y ceased.
 */
export type UpdateCommitmentDTO = Partial<CreateCommitmentDTO> & {
  completed?: boolean;
  ceased?: boolean;
};

const BASE = '/agenda/events';

/**
 * Servicio para gestion de compromisos y tareas.
 * Utiliza el endpoint de agenda del backend filtrando por tipo "commitment".
 * Todas las peticiones requieren autenticacion JWT.
 */

export const commitmentsService = {
    /**
   * Obtiene todos los compromisos del usuario autenticado.
   * 
   * @returns {Promise<Commitment[]>} Lista de compromisos ordenados por fecha
   * @throws {Error} Si el token ha expirado o no es valido
   * 
   * @example
   * const commitments = await commitmentsService.getAll();
   */
  getAll: async (): Promise<Commitment[]> => {
    const { data } = await api.get<Commitment[]>(BASE, {
      params: { type: 'commitment' }
    });
    return data;
  },

  /**
   * Obtiene un compromiso especifico por su ID.
   * 
   * @param {number} id - Identificador del compromiso
   * @returns {Promise<Commitment>} Datos completos del compromiso
   * @throws {Error} Si el compromiso no existe o el token no es valido
   * 
   * @example
   * const commitment = await commitmentsService.getById(10);
   */
  getById: async (id: number): Promise<Commitment> => {
    const { data } = await api.get<Commitment>(`${BASE}/${id}`);
    return data;
  },

  /**
   * Crea un nuevo compromiso.
   * Los campos completed y ceased se inicializan automaticamente en false.
   * 
   * @param {CreateCommitmentDTO} commitment - Datos del compromiso a crear
   * @returns {Promise<Commitment>} Compromiso creado con el id asignado
   * @throws {Error} Si los datos son invalidos o el token no es valido
   * 
   * */
  create: async (commitment: CreateCommitmentDTO): Promise<Commitment> => {
    const { data } = await api.post<Commitment>(BASE, {
      ...commitment,
      type: 'commitment'
    });
    return data;
  },

  /**
   * Actualiza parcialmente un compromiso existente.
   * Solo envia al backend los campos que se desean modificar.
   * 
   * @param {number} id - Identificador del compromiso a actualizar
   * @param {UpdateCommitmentDTO} changes - Campos a modificar
   * @returns {Promise<Commitment>} Compromiso actualizado
   * @throws {Error} Si el compromiso no existe o el token no es valido
   * 
    */
  update: async (id: number, changes: UpdateCommitmentDTO): Promise<Commitment> => {
    const { data } = await api.patch<Commitment>(`${BASE}/${id}`, changes);
    return data;
  },

  /**
   * Alterna el estado de completado de un compromiso.
   * Metodo de conveniencia que internamente usa update().
   * 
   * @param {number} id - Identificador del compromiso
   * @param {boolean} completed - Nuevo estado de completado
   * @returns {Promise<Commitment>} Compromiso actualizado
   * */

  toggleComplete: async (id: number, completed: boolean): Promise<Commitment> => {
    return commitmentsService.update(id, { completed });
  },

  /**
   * Cesa un compromiso recurrente.
   * Lo marca como cesado y no completado, moviendolo a la seccion de completados.
   * Solo aplica para compromisos con frecuencia diferente a "Una vez".
   * 
   * @param {number} id - Identificador del compromiso a cesar
   * @returns {Promise<Commitment>} Compromiso actualizado con ceased = true
   * */
  cease: async (id: number): Promise<Commitment> => {
    return commitmentsService.update(id, { ceased: true, completed: false });
  },

  /**
   * Reactiva un compromiso que fue completado o cesado.
   * Restablece completed y ceased a false, devolviendolo a la lista activa.
   * 
   * @param {number} id - Identificador del compromiso a reactivar
   * @returns {Promise<Commitment>} Compromiso reactivado
   * */

  reactivate: async (id: number): Promise<Commitment> => {
    return commitmentsService.update(id, { completed: false, ceased: false });
  },

  /**
   * Elimina un compromiso permanentemente.
   * Esta accion no se puede deshacer.
   * 
   * @param {number} id - Identificador del compromiso a eliminar
   * @returns {Promise<void>}
   * @throws {Error} Si el compromiso no existe o el token no es valido
   * */
  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE}/${id}`);
  }
};

export default commitmentsService;