/**
 * Dashboard.tsx - Pagina de inicio
 *
 *  - Resumen financiero: saldo, ingresos, egresos, ahorros
 *  - Compromisos pendientes y racha
 *  - Proximos gastos fijos
 *  - Contrasenas guardadas (conteo)
 *  - Datos del grafico estadistico por periodo
 */

import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  Legend, ResponsiveContainer
} from 'recharts'
import {
  DollarSign,
  ClipboardList,
  Clock,
  Lock,
  ChevronDown
} from 'lucide-react'
import styles from './Dashboard.module.css'
import financesService, { type Movement } from '../../services/financeService'
import fixedExpensesService, { type FixedExpense } from '../../services/fixedExpenses.service'
import commitmentsService, { type Commitment } from '../../services/commitments.service'
import { passwordService } from '../../services/password.service'

// ─── Tipos

interface SummaryCard {
  label: string
  value: string | number
  icon: React.ReactNode
  route?: string
}

interface CommitmentItem {
  id: string
  title: string
  date: string
  route: string
}

interface FixedExpenseItem {
  id: string
  title: string
  date: string
  route: string
}

interface ChartDataPoint {
  name: string
  Ingresos: number
  Egresos: number
  Ahorros: number
}

interface ChartBucket {
  name: string
  start: Date
  end: Date
}

const PERIOD_OPTIONS = [
  { value: 'week',     label: 'Ultima semana' },
  { value: 'month',    label: 'Ultimo mes' },
  { value: 'quarter',  label: 'Ultimos 3 meses' },
  { value: 'semester', label: 'Ultimos 6 meses' },
  { value: 'year',     label: 'Ultimo ano' },
]

const MONTHS = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic']
const WEEK_DAYS = ['Dom','Lun','Mar','Mie','Jue','Vie','Sab']

const formatCurrency = (amount: number) =>
  '$' + amount.toLocaleString('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const formatShortDate = (dateStr: string) => {
  if (!dateStr) return ''
  const [year, month, day] = dateStr.split('-')
  return `${day}/${MONTHS[parseInt(month) - 1]}/${year}`
}

const parseDate = (dateStr: string) => new Date(`${dateStr}T00:00:00`)

const startOfDay = (date: Date) => {
  const result = new Date(date)
  result.setHours(0, 0, 0, 0)
  return result
}

const endOfDay = (date: Date) => {
  const result = new Date(date)
  result.setHours(23, 59, 59, 999)
  return result
}

const addDays = (date: Date, days: number) => {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result
}

const addMonths = (date: Date, months: number) => {
  const result = new Date(date)
  result.setMonth(result.getMonth() + months)
  return result
}

const buildChartBuckets = (period: string): ChartBucket[] => {
  const today = startOfDay(new Date())

  if (period === 'week') {
    return Array.from({ length: 7 }, (_, index) => {
      const day = addDays(today, index - 6)
      return {
        name: WEEK_DAYS[day.getDay()],
        start: startOfDay(day),
        end: endOfDay(day),
      }
    })
  }

  if (period === 'month') {
    const start = addDays(today, -27)
    return Array.from({ length: 4 }, (_, index) => {
      const weekStart = addDays(start, index * 7)
      return {
        name: `Sem ${index + 1}`,
        start: startOfDay(weekStart),
        end: endOfDay(addDays(weekStart, 6)),
      }
    })
  }

  const monthCount = period === 'quarter' ? 3 : period === 'semester' ? 6 : 12
  const firstMonth = new Date(today.getFullYear(), today.getMonth() - monthCount + 1, 1)

  return Array.from({ length: monthCount }, (_, index) => {
    const monthStart = addMonths(firstMonth, index)
    const monthEnd = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 0)
    return {
      name: MONTHS[monthStart.getMonth()],
      start: startOfDay(monthStart),
      end: endOfDay(monthEnd),
    }
  })
}

const buildChartData = (movements: Movement[], period: string): ChartDataPoint[] => {
  return buildChartBuckets(period).map((bucket) => {
    const bucketMovements = movements.filter((movement) => {
      const date = parseDate(movement.entryDate)
      return date >= bucket.start && date <= bucket.end
    })

    const ingresos = bucketMovements
      .filter((movement) => movement.type === 'Ingreso')
      .reduce((total, movement) => total + movement.amount, 0)

    const egresos = bucketMovements
      .filter((movement) => movement.type === 'Egreso')
      .reduce((total, movement) => total + movement.amount, 0)

    return {
      name: bucket.name,
      Ingresos: ingresos,
      Egresos: egresos,
      Ahorros: Math.max(ingresos - egresos, 0),
    }
  })
}

const sortByDate = <T extends { date: string }>(items: T[]) =>
  [...items].sort((a, b) => parseDate(a.date).getTime() - parseDate(b.date).getTime())

const getStoredStreak = () => {
  const diasActivos = Number(localStorage.getItem('af_streak_count') || 0)
  return { diasActivos, meta: 30 }
}

const resolveResource = async <T,>(request: Promise<T>, fallback: T) => {
  try {
    return { data: await request, failed: false }
  } catch {
    return { data: fallback, failed: true }
  }
}

// ─── Componente principal

export default function Dashboard() {
  const navigate = useNavigate()

  const [movements, setMovements]             = useState<Movement[]>([])
  const [commitments, setCommitments]         = useState<Commitment[]>([])
  const [fixedExpenses, setFixedExpenses]     = useState<FixedExpense[]>([])
  const [passwordCount, setPasswordCount]     = useState(0)
  const [streak, setStreak]                   = useState(getStoredStreak)
  const [chartPeriod, setChartPeriod]         = useState<string>('quarter')
  const [showPeriodMenu, setShowPeriodMenu]   = useState(false)
  const [loading, setLoading]                 = useState(true)
  const [error, setError]                     = useState('')

  useEffect(() => {
    let mounted = true

    const loadDashboard = async () => {
      setLoading(true)
      setError('')

      const [movementResult, commitmentResult, fixedExpenseResult, passwordResult] = await Promise.all([
        resolveResource(financesService.getAll(), [] as Movement[]),
        resolveResource(commitmentsService.getAll(), [] as Commitment[]),
        resolveResource(fixedExpensesService.getAll(), [] as FixedExpense[]),
        resolveResource(passwordService.getAll(), []),
      ])

      if (!mounted) return

      setMovements(movementResult.data)
      setCommitments(commitmentResult.data)
      setFixedExpenses(fixedExpenseResult.data)
      setPasswordCount(passwordResult.data.length)
      setStreak(getStoredStreak())

      if (
        movementResult.failed ||
        commitmentResult.failed ||
        fixedExpenseResult.failed ||
        passwordResult.failed
      ) {
        setError('Algunos datos del inicio no se pudieron sincronizar.')
      }

      setLoading(false)
    }

    void loadDashboard()

    return () => {
      mounted = false
    }
  }, [])

  const totalIncome = useMemo(
    () => movements.filter((movement) => movement.type === 'Ingreso').reduce((total, movement) => total + movement.amount, 0),
    [movements],
  )

  const totalExpenses = useMemo(
    () => movements.filter((movement) => movement.type === 'Egreso').reduce((total, movement) => total + movement.amount, 0),
    [movements],
  )

  const balance = totalIncome - totalExpenses

  const pendingCommitments = useMemo(
    () => commitments.filter((commitment) => !commitment.completed && !commitment.ceased),
    [commitments],
  )

  const upcomingFixedExpenses = useMemo(
    () => sortByDate(
      fixedExpenses
        .filter((expense) => expense.status === 'Pendiente')
        .map((expense) => ({ ...expense, date: expense.cutOffDate })),
    ),
    [fixedExpenses],
  )

  const summary: SummaryCard[] = useMemo(() => [
    { label: 'SALDO ACTUAL',           value: formatCurrency(balance),              icon: <DollarSign size={24} />,    route: '/finances' },
    { label: 'COMPROMISOS PENDIENTES', value: pendingCommitments.length,            icon: <ClipboardList size={24} />, route: '/commitments' },
    { label: 'PROXIMOS GASTOS',        value: upcomingFixedExpenses.length,         icon: <Clock size={24} />,         route: '/fixed-expenses' },
    { label: 'CONTRASENAS',            value: passwordCount,                        icon: <Lock size={24} />,          route: '/password-manager/home' },
  ], [balance, passwordCount, pendingCommitments.length, upcomingFixedExpenses.length])

  const fixedExpenseItems: FixedExpenseItem[] = useMemo(
    () => upcomingFixedExpenses.slice(0, 2).map((expense) => ({
      id: expense.id,
      title: expense.name,
      date: formatShortDate(expense.cutOffDate),
      route: '/fixed-expenses',
    })),
    [upcomingFixedExpenses],
  )

  const commitmentItems: CommitmentItem[] = useMemo(
    () => sortByDate(pendingCommitments)
      .slice(0, 2)
      .map((commitment) => ({
        id: commitment.id,
        title: commitment.name,
        date: formatShortDate(commitment.date),
        route: '/commitments',
      })),
    [pendingCommitments],
  )

  const chartData = useMemo(() => buildChartData(movements, chartPeriod), [movements, chartPeriod])
  const selectedPeriodLabel = PERIOD_OPTIONS.find(p => p.value === chartPeriod)?.label ?? ''
  const streakPercent       = Math.min((streak.diasActivos / streak.meta) * 100, 100)

  return (
    <div className={styles.dashboard}>
      <h2 className={styles.pageTitle}>Inicio</h2>
      {error && <p className={styles.notice}>{error}</p>}

      {/* ── Tarjetas resumen ── */}
      <div className={styles.summaryGrid}>
        {summary.map((card) => (
          <button
            key={card.label}
            className={styles.summaryCard}
            onClick={() => card.route && navigate(card.route)}
            aria-label={`Ir a ${card.label}`}
          >
            <span className={styles.summaryIcon}>{card.icon}</span>
            <span className={styles.summaryLabel}>{card.label}</span>
            <span className={styles.summaryValue}>{loading ? '...' : card.value}</span>
          </button>
        ))}
      </div>

      {/* ── Fila central: racha + grafico ── */}
      <div className={styles.midRow}>

        {/* Racha de compromisos */}
        <div className={styles.streakCard}>
          <div className={styles.streakHeader}>
            <span className={styles.streakTitle}>Racha de Compromisos</span>
            <div className={styles.streakBadge}>
              <span className={styles.streakBadgeNumber}>{streak.diasActivos}</span>
              <span className={styles.streakBadgeSub}>Dias</span>
            </div>
          </div>

          <div className={styles.streakDaysRow}>
            <span className={styles.streakBigNumber}>{streak.diasActivos}</span>
            <span className={styles.streakDaysLabel}>Dias Activos</span>
          </div>

          <div className={styles.streakBarWrapper}>
            <div
              className={styles.streakBarFill}
              style={{ width: `${streakPercent}%` }}
            />
          </div>
          <div className={styles.streakMilestones}>
            {[7, 15, 23, 30].map(m => (
              <span
                key={m}
                className={`${styles.milestone} ${streak.diasActivos >= m ? styles.milestoneReached : ''}`}
              >
                {m}
              </span>
            ))}
          </div>
        </div>

        {/* Grafico estadistico */}
        <div className={styles.chartCard}>
          <div className={styles.chartHeader}>
            <span className={styles.chartTitle}>Reporte estadistico</span>

            <div className={styles.periodSelector}>
              <button
                className={styles.periodButton}
                onClick={() => setShowPeriodMenu(v => !v)}
              >
                {selectedPeriodLabel}
                <ChevronDown size={14} />
              </button>

              {showPeriodMenu && (
                <div className={styles.periodMenu}>
                  {PERIOD_OPTIONS.map(opt => (
                    <button
                      key={opt.value}
                      className={`${styles.periodOption} ${opt.value === chartPeriod ? styles.periodOptionActive : ''}`}
                      onClick={() => { setChartPeriod(opt.value); setShowPeriodMenu(false) }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {loading ? (
            <div className={styles.chartLoading}>Cargando...</div>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={chartData} barCategoryGap="30%" barGap={2}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fontSize: 11, fill: 'var(--color-text-secondary)' }} axisLine={false} tickLine={false} />
                <Tooltip
                  contentStyle={{ background: 'var(--color-white)', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)', fontSize: 12 }}
                  cursor={{ fill: 'rgba(62,99,160,0.06)' }}
                  formatter={(value: unknown) => [`$${Number(value).toLocaleString()}`, '']}
                />
                <Legend wrapperStyle={{ fontSize: 12, paddingTop: 8 }} />
                <Bar dataKey="Ingresos" fill="var(--color-celeste-2)"  radius={[4,4,0,0]} />
                <Bar dataKey="Egresos"  fill="var(--color-primary-4)"  radius={[4,4,0,0]} />
                <Bar dataKey="Ahorros"  fill="var(--color-primary)"     radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* ── Fila inferior: gastos fijos + compromisos ── */}
      <div className={styles.bottomRow}>

        {/* Gastos fijos */}
        <div className={styles.listCard}>
          <span className={styles.listCardTitle}>GASTOS FIJOS</span>
          <div className={styles.listItems}>
            {fixedExpenseItems.length === 0 ? (
              <p className={styles.listEmpty}>Sin gastos proximos.</p>
            ) : fixedExpenseItems.map(exp => (
              <div key={exp.id} className={styles.listItem}>
                <div>
                  <p className={styles.listItemTitle}>{exp.title}</p>
                  <p className={styles.listItemDate}>{exp.date}</p>
                </div>
                <button className={styles.verBtn} onClick={() => navigate(exp.route)}>
                  Ver
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Compromisos */}
        <div className={styles.listCard}>
          <span className={styles.listCardTitle}>COMPROMISOS</span>
          <div className={styles.listItems}>
            {commitmentItems.length === 0 ? (
              <p className={styles.listEmpty}>Sin compromisos pendientes.</p>
            ) : commitmentItems.map(com => (
              <div key={com.id} className={styles.listItem}>
                <div>
                  <p className={styles.listItemTitle}>{com.title}</p>
                  <p className={styles.listItemDate}>{com.date}</p>
                </div>
                <button className={styles.verBtn} onClick={() => navigate(com.route)}>
                  Ver
                </button>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  )
}
