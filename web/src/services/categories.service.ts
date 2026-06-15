import api from './api.config'

interface ApiResponse<T> {
  status: number
  message: string
  data: T
}

export type CategoryType = 'FINANCES' | 'AGENDA'

export interface Category {
  id: string
  name: string
  type: CategoryType
}

const BASE = '/finances/categories'

const normalizeName = (name: string) => name.trim().toLocaleLowerCase('es-CO')

const unwrap = <T>(body: ApiResponse<T>): T => body.data

export const categoriesService = {
  /**
   * Obtiene categorias del backend.
   * Las categorias FINANCES se usan para movimientos y gastos fijos.
   */
  getAll: async (type?: CategoryType): Promise<Category[]> => {
    const { data } = await api.get<ApiResponse<Category[]>>(BASE, {
      params: type ? { type } : undefined,
    })
    return unwrap(data)
  },

  /**
   * Crea una categoria nueva con el contrato real del backend.
   */
  create: async (name: string, type: CategoryType = 'FINANCES'): Promise<Category> => {
    const { data } = await api.post<ApiResponse<Category>>(BASE, {
      name: name.trim(),
      type,
    })
    return unwrap(data)
  },

  /**
   * Retorna el id de una categoria financiera existente o la crea.
   * Esto permite que los formularios actuales sigan usando nombres legibles.
   */
  ensureFinanceCategoryId: async (name: string): Promise<string> => {
    const cleanName = name.trim()
    const categories = await categoriesService.getAll('FINANCES')
    const existing = categories.find((category) => normalizeName(category.name) === normalizeName(cleanName))

    if (existing) {
      return existing.id
    }

    const created = await categoriesService.create(cleanName, 'FINANCES')
    return created.id
  },
}

export default categoriesService
