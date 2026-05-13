
import api from "./api.config"

// ─── Tipos ────────────────────────────────────────────────────────────────────

export interface FixedExpense {
  id: number
  name: string
  category: string
  frequency: string
  cutOffDate: string
  amount: number
  status: 'Pendiente' | 'Pagado'
}

export type CreateFixedExpenseDTO = Omit<FixedExpense, 'id'>

export type UpdateFixedExpenseDTO = Partial<CreateFixedExpenseDTO>

// ─── Servicio ─────────────────────────────────────────────────────────────────

const BASE = '/fixed-expenses'

const fixedExpensesService = {

  /**
   * Obtiene todos los gastos fijos del usuario autenticado.
   */
  getAll: async (): Promise<FixedExpense[]> => {
    const { data } = await api.get<FixedExpense[]>(BASE)
    return data
  },

  /**
   * Obtiene un gasto fijo por su ID.
   */
  getById: async (id: number): Promise<FixedExpense> => {
    const { data } = await api.get<FixedExpense>(`${BASE}/${id}`)
    return data
  },

  /**
   * Crea un nuevo gasto fijo.
   */
  create: async (expense: CreateFixedExpenseDTO): Promise<FixedExpense> => {
    const { data } = await api.post<FixedExpense>(BASE, expense)
    return data
  },

  /**
   * Actualiza campos específicos de un gasto fijo (ej: solo el estado).
   */
  update: async (id: number, changes: UpdateFixedExpenseDTO): Promise<FixedExpense> => {
    const { data } = await api.put<FixedExpense>(`${BASE}/${id}`, changes)
    return data
  },

  /**
   * Cambia el estado de un gasto entre 'Pendiente' y 'Pagado'.
   * Wrapper semántico sobre update() para usar desde el toggle de la card.
   */
  toggleStatus: async (id: number, currentStatus: 'Pendiente' | 'Pagado'): Promise<FixedExpense> => {
    const newStatus = currentStatus === 'Pendiente' ? 'Pagado' : 'Pendiente'
    return fixedExpensesService.update(id, { status: newStatus })
  },

  /**
   * Elimina un gasto fijo por su ID.
   */
  delete: async (id: number): Promise<void> => {
    await api.delete(`${BASE}/${id}`)
  },
}

export default fixedExpensesService