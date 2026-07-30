import api from './api.config'
import categoriesService, { type Category as BackendCategory } from './categories.service'

interface ApiResponse<T> {
  status: number
  message: string
  data: T
}

type BackendStatus = 'Pendiente' | 'Completado' | 'Cancelado' | 'Pospuesto'

interface EventResponse {
  id: string
  title: string
  priority?: string | null
  eventDate: string
  frequency: number
  reminder: string
  startHour: string
  endHour: string
  description?: string | null
  status?: BackendStatus | string | null
  category?: BackendCategory | null
}

interface EventRequest {
  title: string
  priority: Priority
  eventDate: string
  frequency: number
  reminder: string
  startHour: string
  endHour: string
  description?: string
  status?: BackendStatus
  categoryId?: string
}

export type Priority = 'Alta' | 'Media' | 'Baja'
export type Frequency = 'Una vez' | 'Diario' | 'Semanal' | 'Mensual' | 'Anual'
export type Category = 'Trabajo' | 'Hogar' | 'Personal' | 'Salud' | 'Finanzas' | 'Otros'

/**
 * Representa un compromiso en el lenguaje de la interfaz web.
 */
export interface Commitment {
  id: string
  name: string
  category: Category
  categoryId?: string
  frequency: Frequency
  priority: Priority
  date: string
  reminder: string
  description: string
  completed: boolean
  ceased: boolean
}

export type CreateCommitmentDTO = Omit<Commitment, 'id' | 'categoryId' | 'completed' | 'ceased'>

export type UpdateCommitmentDTO = Partial<CreateCommitmentDTO> & {
  completed?: boolean
  ceased?: boolean
}

const BASE = '/agenda/events'

const frequencyToBackend: Record<Frequency, number> = {
  'Una vez': 0,
  Diario: 1,
  Semanal: 7,
  Mensual: 30,
  Anual: 365,
}

const frequencyToUi = (frequency: number): Frequency => {
  if (frequency === 1) return 'Diario'
  if (frequency === 7) return 'Semanal'
  if (frequency === 30) return 'Mensual'
  if (frequency === 365) return 'Anual'
  return 'Una vez'
}

const normalizePriority = (priority?: string | null): Priority => {
  if (priority === 'Alta' || priority === 'Baja') return priority
  return 'Media'
}

const normalizeCategory = (category?: BackendCategory | null): Category => {
  const name = category?.name
  if (
    name === 'Trabajo' ||
    name === 'Hogar' ||
    name === 'Personal' ||
    name === 'Salud' ||
    name === 'Finanzas'
  ) {
    return name
  }

  return 'Otros'
}

const unwrap = <T>(body: ApiResponse<T>): T => body.data

const toIsoDate = (value?: string | null): string => {
  if (!value) return ''
  return value.split('T')[0]
}

const parseReminderTime = (reminder: string): string => {
  const [time = '07:00', suffix = 'A.M.'] = reminder.trim().split(/\s+/)
  const [rawHour, rawMinute = '00'] = time.split(':')
  let hour = Number(rawHour)
  const minute = Number(rawMinute)
  const normalizedSuffix = suffix.toUpperCase()

  if (normalizedSuffix.startsWith('P') && hour < 12) hour += 12
  if (normalizedSuffix.startsWith('A') && hour === 12) hour = 0

  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`
}

const toReminderLabel = (value?: string | null): string => {
  if (!value) return '7:00 A.M.'

  const [, time = '07:00:00'] = value.split('T')
  const [rawHour, rawMinute = '00'] = time.split(':')
  let hour = Number(rawHour)
  const suffix = hour >= 12 ? 'P.M.' : 'A.M.'

  if (hour === 0) hour = 12
  if (hour > 12) hour -= 12

  return `${hour}:${rawMinute} ${suffix}`
}

const addHours = (dateTime: string, hours: number): string => {
  const [datePart, timePart = '07:00:00'] = dateTime.split('T')
  const [rawHour, rawMinute = '00'] = timePart.split(':')
  const hour = Number(rawHour) + hours

  return `${datePart}T${String(hour).padStart(2, '0')}:${rawMinute}:00`
}

const buildDateTime = (date: string, reminder: string): string => `${date}T${parseReminderTime(reminder)}`

const toBackendStatus = (commitment: Partial<UpdateCommitmentDTO>): BackendStatus | undefined => {
  if (commitment.ceased) return 'Cancelado'
  if (commitment.completed) return 'Completado'
  if (commitment.completed === false || commitment.ceased === false) return 'Pendiente'
  return undefined
}

const toUiCommitment = (event: EventResponse): Commitment => ({
  id: event.id,
  name: event.title,
  category: normalizeCategory(event.category),
  categoryId: event.category?.id,
  frequency: frequencyToUi(event.frequency),
  priority: normalizePriority(event.priority),
  date: toIsoDate(event.eventDate),
  reminder: toReminderLabel(event.reminder),
  description: event.description ?? '',
  completed: event.status === 'Completado',
  ceased: event.status === 'Cancelado',
})

const toBackendEvent = async (
  commitment: Partial<CreateCommitmentDTO | UpdateCommitmentDTO>,
): Promise<Partial<EventRequest>> => {
  const payload: Partial<EventRequest> = {}

  if (commitment.name !== undefined) payload.title = commitment.name.trim()
  if (commitment.priority !== undefined) payload.priority = commitment.priority
  if (commitment.date !== undefined) payload.eventDate = commitment.date
  if (commitment.frequency !== undefined) payload.frequency = frequencyToBackend[commitment.frequency]
  if (commitment.description !== undefined) payload.description = commitment.description.trim()

  const status = toBackendStatus(commitment)
  if (status) payload.status = status

  if (commitment.date && commitment.reminder) {
    const startHour = buildDateTime(commitment.date, commitment.reminder)
    payload.reminder = startHour
    payload.startHour = startHour
    payload.endHour = addHours(startHour, 1)
  }

  if (commitment.category) {
    payload.categoryId = await categoriesService.ensureAgendaCategoryId(commitment.category)
  }

  return payload
}

export const commitmentsService = {
  /**
   * Obtiene todos los compromisos del usuario autenticado.
   */
  getAll: async (): Promise<Commitment[]> => {
    const { data } = await api.get<ApiResponse<EventResponse[]>>(BASE)
    return unwrap(data).map(toUiCommitment)
  },

  /**
   * Obtiene un compromiso especifico por su ID.
   */
  getById: async (id: string): Promise<Commitment> => {
    const { data } = await api.get<ApiResponse<EventResponse>>(`${BASE}/${id}`)
    return toUiCommitment(unwrap(data))
  },

  /**
   * Crea un nuevo compromiso como evento de agenda.
   */
  create: async (commitment: CreateCommitmentDTO): Promise<Commitment> => {
    const payload = await toBackendEvent({
      ...commitment,
      completed: false,
      ceased: false,
    }) as EventRequest
    const { data } = await api.post<ApiResponse<EventResponse>>(BASE, payload)
    return toUiCommitment(unwrap(data))
  },

  /**
   * Actualiza parcialmente un compromiso existente.
   */
  update: async (id: string, changes: UpdateCommitmentDTO): Promise<Commitment> => {
    const payload = await toBackendEvent(changes)
    const { data } = await api.patch<ApiResponse<EventResponse>>(`${BASE}/${id}`, payload)
    return toUiCommitment(unwrap(data))
  },

  toggleComplete: async (id: string, completed: boolean): Promise<Commitment> => {
    return commitmentsService.update(id, { completed, ceased: false })
  },

  cease: async (id: string): Promise<Commitment> => {
    return commitmentsService.update(id, { ceased: true, completed: false })
  },

  reactivate: async (id: string): Promise<Commitment> => {
    return commitmentsService.update(id, { completed: false, ceased: false })
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`${BASE}/${id}`)
  },
}

export default commitmentsService
