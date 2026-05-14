/**
 * Dashboard.tsx - Página de inicio 
 *
 *  - Resumen financiero: saldo, ingresos, egresos, ahorros
 *  - Compromisos pendientes y racha
 *  - Próximos gastos fijos
 *  - Contraseñas guardadas (conteo)
 *  - Datos del gráfico estadístico por período
 */

import { useState, useEffect } from 'react'
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

// ─── Tipos 

interface SummaryCard {
  label: string
  value: string | number
  icon: React.ReactNode
  route?: string
}

interface CommitmentItem {
  id: number
  title: string
  date: string
  route: string
}

interface FixedExpenseItem {
  id: number
  title: string
  date: string
  route: string
}

interface ChartDataPoint {
  name: string
  Ingresos: number
  Egresos: number
  Osio: number
  Ahorros: number
}

// ─── Datos mock (reemplazar con llamadas al backend) 

// TODO: BACKEND: GET /api/dashboard/summary → { saldo, compromisosPendientes, proximosGastos, contrasenas }
const MOCK_SUMMARY: SummaryCard[] = [
  { label: 'SALDO ACTUAL',           value: '$2,500.00', icon: <DollarSign size={24} />,    route: '/finances' },
  { label: 'COMPROMISOS PENDIENTES', value: 8,           icon: <ClipboardList size={24} />, route: '/commitments' },
  { label: 'PRÓXIMOS GASTOS',        value: 3,           icon: <Clock size={24} />,         route: '/fixed-expenses' },
  { label: 'CONTRASEÑAS',            value: 12,          icon: <Lock size={24} />,          route: '/password-manager/home' },
]

//  TODO: BACKEND: GET /api/dashboard/streak → { diasActivos, meta }
const MOCK_STREAK = { diasActivos: 7, meta: 30 }

// TODO: BACKEND: GET /api/dashboard/recent-commitments → CommitmentItem[]
const MOCK_COMMITMENTS: CommitmentItem[] = [
  { id: 1, title: 'Reunión con equipo', date: '24/Feb', route: '/commitments' },
  { id: 2, title: 'Pago tarjeta',       date: '28/Feb', route: '/commitments' },
]

// TODO: BACKEND: GET /api/dashboard/recent-fixed-expenses → FixedExpenseItem[]
const MOCK_FIXED_EXPENSES: FixedExpenseItem[] = [
  { id: 1, title: 'Internet', date: '24/Feb', route: '/fixed-expenses' },
  { id: 2, title: 'Netflix',  date: '01/Mar', route: '/fixed-expenses' },
]

// TODO: BACKEND: GET /api/dashboard/chart?period=week|month|quarter|semester|year → ChartDataPoint[]
const MOCK_CHART_DATA: Record<string, ChartDataPoint[]> = {
  week: [
    { name: 'Lun', Ingresos: 500,  Egresos: 200, Osio: 80,  Ahorros: 120 },
    { name: 'Mar', Ingresos: 300,  Egresos: 150, Osio: 50,  Ahorros: 80  },
    { name: 'Mié', Ingresos: 700,  Egresos: 300, Osio: 100, Ahorros: 200 },
    { name: 'Jue', Ingresos: 200,  Egresos: 180, Osio: 60,  Ahorros: 40  },
    { name: 'Vie', Ingresos: 900,  Egresos: 400, Osio: 120, Ahorros: 300 },
    { name: 'Sáb', Ingresos: 100,  Egresos: 90,  Osio: 200, Ahorros: 20  },
    { name: 'Dom', Ingresos: 50,   Egresos: 60,  Osio: 300, Ahorros: 10  },
  ],
  month: [
    { name: 'Sem 1', Ingresos: 2000, Egresos: 900,  Osio: 300, Ahorros: 500 },
    { name: 'Sem 2', Ingresos: 1800, Egresos: 700,  Osio: 250, Ahorros: 450 },
    { name: 'Sem 3', Ingresos: 2200, Egresos: 1000, Osio: 350, Ahorros: 600 },
    { name: 'Sem 4', Ingresos: 1500, Egresos: 800,  Osio: 280, Ahorros: 400 },
  ],
  quarter: [
    { name: 'Ene', Ingresos: 3500, Egresos: 900,  Osio: 600, Ahorros: 400 },
    { name: 'Feb', Ingresos: 2800, Egresos: 1200, Osio: 500, Ahorros: 700 },
    { name: 'Mar', Ingresos: 3200, Egresos: 1100, Osio: 800, Ahorros: 900 },
  ],
  semester: [
    { name: 'Ene', Ingresos: 3500, Egresos: 900,  Osio: 600, Ahorros: 400  },
    { name: 'Feb', Ingresos: 2800, Egresos: 1200, Osio: 500, Ahorros: 700  },
    { name: 'Mar', Ingresos: 3200, Egresos: 1100, Osio: 800, Ahorros: 900  },
    { name: 'Abr', Ingresos: 4000, Egresos: 1400, Osio: 700, Ahorros: 1200 },
    { name: 'May', Ingresos: 3600, Egresos: 1300, Osio: 650, Ahorros: 1000 },
    { name: 'Jun', Ingresos: 3900, Egresos: 1500, Osio: 900, Ahorros: 1100 },
  ],
  year: [
    { name: 'Ene', Ingresos: 3500, Egresos: 900,  Osio: 600,  Ahorros: 400  },
    { name: 'Feb', Ingresos: 2800, Egresos: 1200, Osio: 500,  Ahorros: 700  },
    { name: 'Mar', Ingresos: 3200, Egresos: 1100, Osio: 800,  Ahorros: 900  },
    { name: 'Abr', Ingresos: 4000, Egresos: 1400, Osio: 700,  Ahorros: 1200 },
    { name: 'May', Ingresos: 3600, Egresos: 1300, Osio: 650,  Ahorros: 1000 },
    { name: 'Jun', Ingresos: 3900, Egresos: 1500, Osio: 900,  Ahorros: 1100 },
    { name: 'Jul', Ingresos: 4200, Egresos: 1600, Osio: 750,  Ahorros: 1300 },
    { name: 'Ago', Ingresos: 3100, Egresos: 1200, Osio: 680,  Ahorros: 900  },
    { name: 'Sep', Ingresos: 3800, Egresos: 1350, Osio: 720,  Ahorros: 1050 },
    { name: 'Oct', Ingresos: 4100, Egresos: 1450, Osio: 800,  Ahorros: 1200 },
    { name: 'Nov', Ingresos: 3700, Egresos: 1250, Osio: 690,  Ahorros: 1000 },
    { name: 'Dic', Ingresos: 5000, Egresos: 2000, Osio: 1200, Ahorros: 1500 },
  ],
}

const PERIOD_OPTIONS = [
  { value: 'week',     label: 'Última semana' },
  { value: 'month',    label: 'Último mes' },
  { value: 'quarter',  label: 'Últimos 3 meses' },
  { value: 'semester', label: 'Últimos 6 meses' },
  { value: 'year',     label: 'Último año' },
]

// ─── Componente principal

export default function Dashboard() {
  const navigate = useNavigate()

  // TODO: BACKEND: reemplazar useState con useEffect + axios
  const [summary]       = useState<SummaryCard[]>(MOCK_SUMMARY)
  const [streak]        = useState(MOCK_STREAK)
  const [commitments]   = useState<CommitmentItem[]>(MOCK_COMMITMENTS)
  const [fixedExpenses] = useState<FixedExpenseItem[]>(MOCK_FIXED_EXPENSES)

  const [chartPeriod, setChartPeriod]       = useState<string>('quarter')
  const [chartData, setChartData]           = useState<ChartDataPoint[]>(MOCK_CHART_DATA['quarter'])
  const [showPeriodMenu, setShowPeriodMenu] = useState(false)
  const [loading, setLoading]               = useState(false)

  // TODO: BACKEND: al cambiar período → axios.get(`/api/dashboard/chart?period=${chartPeriod}`)
  useEffect(() => {
    setLoading(true)
    setTimeout(() => {
      setChartData(MOCK_CHART_DATA[chartPeriod] ?? [])
      setLoading(false)
    }, 300)
  }, [chartPeriod])

  const selectedPeriodLabel = PERIOD_OPTIONS.find(p => p.value === chartPeriod)?.label ?? ''
  const streakPercent       = Math.min((streak.diasActivos / streak.meta) * 100, 100)

  return (
    <div className={styles.dashboard}>
      <h2 className={styles.pageTitle}>Inicio</h2>

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
            <span className={styles.summaryValue}>{card.value}</span>
          </button>
        ))}
      </div>

      {/* ── Fila central: racha + gráfico ── */}
      <div className={styles.midRow}>

        {/* Racha de compromisos */}
        <div className={styles.streakCard}>
          <div className={styles.streakHeader}>
            <span className={styles.streakTitle}>Racha de Compromisos</span>
            <div className={styles.streakBadge}>
              <span className={styles.streakBadgeNumber}>{streak.diasActivos}</span>
              <span className={styles.streakBadgeSub}>Días</span>
            </div>
          </div>

          <div className={styles.streakDaysRow}>
            <span className={styles.streakBigNumber}>{streak.diasActivos}</span>
            <span className={styles.streakDaysLabel}>Días Activos</span>
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

        {/* Gráfico estadístico */}
        <div className={styles.chartCard}>
          <div className={styles.chartHeader}>
            <span className={styles.chartTitle}>Reporte estadístico</span>

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
            <div className={styles.chartLoading}>Cargando…</div>
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
                <Bar dataKey="Osio"     fill="var(--color-cyan)"        radius={[4,4,0,0]} />
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
            {fixedExpenses.map(exp => (
              <div key={exp.id} className={styles.listItem}>
                <div>
                  <p className={styles.listItemTitle}>{exp.title}</p>
                  <p className={styles.listItemDate}>{exp.date}</p>
                </div>
                {/* TODO: BACKEND: navega al módulo de gastos fijos */}
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
            {commitments.map(com => (
              <div key={com.id} className={styles.listItem}>
                <div>
                  <p className={styles.listItemTitle}>{com.title}</p>
                  <p className={styles.listItemDate}>{com.date}</p>
                </div>
                {/* TODO: BACKEND: navega al módulo de compromisos */}
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