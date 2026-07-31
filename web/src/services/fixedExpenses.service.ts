import api from './api.config'
import categoriesService, { type Category } from './categories.service'

interface ApiResponse<T> {
  status: number
  message: string
  data: T
}

type BackendFrequency = 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'SEMIANNUAL' | 'ANNUAL'
type BackendStatus = 'ACTIVE' | 'INACTIVE'

interface FixedExpenseResponse {
  id: string
  name: string
  frequency: BackendFrequency
  amount: number
  status: BackendStatus
  nextDueDate: string
  category?: Category | null
}

interface FixedExpenseRequest {
  name: string
  frequency: BackendFrequency
  amount: number
  status: BackendStatus
  nextDueDate: string
  categoryId: string
}

export type FixedExpenseFrequency = 'Semanal' | 'Quincenal' | 'Mensual' | 'Trimestral' | 'Semestral' | 'Anual'
export type FixedExpenseStatus = 'Pendiente' | 'Pagado'

export interface FixedExpense {
  id: string
  name: string
  description: string
  category: string
  categoryId?: string
  frequency: FixedExpenseFrequency
  cutOffDate: string
  amount: number
  status: FixedExpenseStatus
}

export type CreateFixedExpenseDTO = Omit<FixedExpense, 'id' | 'categoryId'>
export type UpdateFixedExpenseDTO = Partial<CreateFixedExpenseDTO>

const BASE = '/finances/fixed-expenses'

const frequencyToBackend: Record<FixedExpenseFrequency, BackendFrequency> = {
  Semanal: 'WEEKLY',
  Quincenal: 'BIWEEKLY',
  Mensual: 'MONTHLY',
  Trimestral: 'QUARTERLY',
  Semestral: 'SEMIANNUAL',
  Anual: 'ANNUAL',
}

const frequencyToUi: Record<BackendFrequency, FixedExpenseFrequency> = {
  WEEKLY: 'Semanal',
  BIWEEKLY: 'Quincenal',
  MONTHLY: 'Mensual',
  QUARTERLY: 'Trimestral',
  SEMIANNUAL: 'Semestral',
  ANNUAL: 'Anual',
}

const unwrap = <T>(body: ApiResponse<T>): T => body.data

const toBackendStatus = (status: FixedExpenseStatus): BackendStatus =>
  status === 'Pendiente' ? 'ACTIVE' : 'INACTIVE'

const toUiStatus = (status: BackendStatus): FixedExpenseStatus =>
  status === 'ACTIVE' ? 'Pendiente' : 'Pagado'

const toUiFixedExpense = (expense: FixedExpenseResponse): FixedExpense => ({
  id: expense.id,
  name: expense.name,
  description: '',
  category: expense.category?.name ?? 'Sin categoria',
  categoryId: expense.category?.id,
  frequency: frequencyToUi[expense.frequency],
  cutOffDate: expense.nextDueDate,
  amount: Number(expense.amount),
  status: toUiStatus(expense.status),
})

const toBackendFixedExpense = async (
  expense: Partial<CreateFixedExpenseDTO>,
): Promise<Partial<FixedExpenseRequest>> => {
  const payload: Partial<FixedExpenseRequest> = {}

  if (expense.name !== undefined) {
    payload.name = expense.name.trim()
  }

  if (expense.frequency) {
    payload.frequency = frequencyToBackend[expense.frequency]
  }

  if (expense.amount !== undefined) {
    payload.amount = expense.amount
  }

  if (expense.status) {
    payload.status = toBackendStatus(expense.status)
  }

  if (expense.cutOffDate) {
    payload.nextDueDate = expense.cutOffDate
  }

  if (expense.category) {
    payload.categoryId = await categoriesService.ensureFinanceCategoryId(expense.category)
  }

  return payload
}

const fixedExpensesService = {
  /**
   * Obtiene todos los gastos fijos del usuario autenticado.
   */
  getAll: async (): Promise<FixedExpense[]> => {
    const { data } = await api.get<ApiResponse<FixedExpenseResponse[]>>(BASE)
    return unwrap(data).map(toUiFixedExpense)
  },

  /**
   * Obtiene un gasto fijo por su ID.
   */
  getById: async (id: string): Promise<FixedExpense> => {
    const { data } = await api.get<ApiResponse<FixedExpenseResponse>>(`${BASE}/${id}`)
    return toUiFixedExpense(unwrap(data))
  },

  /**
   * Crea un nuevo gasto fijo con categoria financiera real.
   */
  create: async (expense: CreateFixedExpenseDTO): Promise<FixedExpense> => {
    const payload = await toBackendFixedExpense(expense) as FixedExpenseRequest
    const { data } = await api.post<ApiResponse<FixedExpenseResponse>>(BASE, payload)
    return toUiFixedExpense(unwrap(data))
  },

  /**
   * Actualiza campos especificos de un gasto fijo.
   */
  update: async (id: string, changes: UpdateFixedExpenseDTO): Promise<FixedExpense> => {
    const payload = await toBackendFixedExpense(changes)
    const { data } = await api.patch<ApiResponse<FixedExpenseResponse>>(`${BASE}/${id}`, payload)
    return toUiFixedExpense(unwrap(data))
  },

  /**
   * Cambia el estado mostrado por la UI contra el estado operativo real.
   */
  toggleStatus: async (id: string, currentStatus: FixedExpenseStatus): Promise<FixedExpense> => {
    const newStatus = currentStatus === 'Pendiente' ? 'Pagado' : 'Pendiente'
    return fixedExpensesService.update(id, { status: newStatus })
  },

  /**
   * Elimina un gasto fijo por su ID.
   */
  delete: async (id: string): Promise<void> => {
    await api.delete(`${BASE}/${id}`)
  },
}

export default fixedExpensesService
