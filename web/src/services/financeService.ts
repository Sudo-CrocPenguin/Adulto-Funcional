import api from './api.config'
import categoriesService, { type Category } from './categories.service'

interface ApiResponse<T> {
  status: number
  message: string
  data: T
}

type BackendMovementType = 'INCOME' | 'EXPENSE'

interface MovementResponse {
  id: string
  movementType: BackendMovementType
  amount: number
  registerDate: string
  description?: string | null
  movementDate: string
  category?: Category | null
}

interface MovementRequest {
  movementType: BackendMovementType
  amount: number
  movementDate: string
  description?: string
  categoryId?: string
}

/**
 * Representa un movimiento financiero en el lenguaje de la interfaz web.
 */
export interface Movement {
  id: string
  type: 'Ingreso' | 'Egreso'
  title: string
  category: string
  categoryId?: string
  amount: number
  entryDate: string
  registerDate: string
  description?: string
}

/**
 * DTO para crear un nuevo movimiento desde el formulario web.
 */
export type CreateMovementDTO = Omit<Movement, 'id' | 'categoryId'>

const BASE = '/finances/movements'

const unwrap = <T>(body: ApiResponse<T>): T => body.data

const toBackendMovementType = (type: Movement['type']): BackendMovementType =>
  type === 'Ingreso' ? 'INCOME' : 'EXPENSE'

const toUiMovementType = (type: BackendMovementType): Movement['type'] =>
  type === 'INCOME' ? 'Ingreso' : 'Egreso'

const toIsoDate = (value?: string | null): string => {
  if (!value) {
    return ''
  }

  return value.split('T')[0]
}

const toUiMovement = (movement: MovementResponse): Movement => {
  const categoryName = movement.category?.name ?? 'Sin categoria'
  const description = movement.description?.trim() ?? ''

  return {
    id: movement.id,
    type: toUiMovementType(movement.movementType),
    title: description || categoryName || 'Movimiento',
    category: categoryName,
    categoryId: movement.category?.id,
    amount: Number(movement.amount),
    entryDate: toIsoDate(movement.movementDate),
    registerDate: toIsoDate(movement.registerDate),
    description,
  }
}

const toBackendMovement = async (movement: Partial<CreateMovementDTO>): Promise<Partial<MovementRequest>> => {
  const payload: Partial<MovementRequest> = {}

  if (movement.type) {
    payload.movementType = toBackendMovementType(movement.type)
  }

  if (movement.amount !== undefined) {
    payload.amount = movement.amount
  }

  if (movement.entryDate) {
    payload.movementDate = movement.entryDate
  }

  if (movement.title || movement.description) {
    payload.description = (movement.description?.trim() || movement.title?.trim() || undefined)
  }

  if (movement.category) {
    payload.categoryId = await categoriesService.ensureFinanceCategoryId(movement.category)
  }

  return payload
}

/**
 * Servicio para gestion de movimientos financieros.
 * Todas las peticiones requieren autenticacion JWT.
 */
export const financesService = {
  /**
   * Obtiene todos los movimientos del usuario autenticado.
   */
  getAll: async (): Promise<Movement[]> => {
    const { data } = await api.get<ApiResponse<MovementResponse[]>>(BASE)
    return unwrap(data).map(toUiMovement)
  },

  /**
   * Obtiene un movimiento especifico por su ID.
   */
  getById: async (id: string): Promise<Movement> => {
    const { data } = await api.get<ApiResponse<MovementResponse>>(`${BASE}/${id}`)
    return toUiMovement(unwrap(data))
  },

  /**
   * Crea un nuevo movimiento financiero y lo devuelve normalizado para la UI.
   */
  create: async (movement: CreateMovementDTO): Promise<Movement> => {
    const payload = await toBackendMovement(movement)
    const { data } = await api.post<ApiResponse<MovementResponse>>(BASE, payload)
    return toUiMovement(unwrap(data))
  },

  /**
   * Actualiza parcialmente un movimiento existente.
   */
  update: async (id: string, changes: Partial<CreateMovementDTO>): Promise<Movement> => {
    const payload = await toBackendMovement(changes)
    const { data } = await api.patch<ApiResponse<MovementResponse>>(`${BASE}/${id}`, payload)
    return toUiMovement(unwrap(data))
  },

  /**
   * Elimina un movimiento financiero permanentemente.
   */
  delete: async (id: string): Promise<void> => {
    await api.delete(`${BASE}/${id}`)
  },
}

export default financesService
