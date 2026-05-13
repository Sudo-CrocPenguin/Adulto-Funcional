import { useMemo, useState } from "react"
import styles from "./FixedExpenses.module.css"
import { Plus, X, Trash2 } from "lucide-react"

interface FixedExpense {
  id: number
  name: string
  category: string
  frequency: string
  cutOffDate: string
  amount: number
  status: "Pendiente" | "Pagado"
}

const formatCurrency = (amount: number) =>
  "$" + amount.toLocaleString("es-CO", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })

function FixedExpenses() {
  const [showModal, setShowModal] = useState(false)

  const [expenses, setExpenses] = useState<FixedExpense[]>([
    {
      id: 1,
      name: "Gimnasio",
      category: "Salud",
      frequency: "Mensual",
      cutOffDate: "2026-02-27",
      amount: 45,
      status: "Pendiente",
    },
    {
      id: 2,
      name: "Alquiler",
      category: "Vivienda",
      frequency: "Mensual",
      cutOffDate: "2026-03-05",
      amount: 45,
      status: "Pendiente",
    },
    {
      id: 3,
      name: "Netflix",
      category: "Suscripción",
      frequency: "Mensual",
      cutOffDate: "2026-02-15",
      amount: 45,
      status: "Pagado",
    },
  ])

  const [selectedTab, setSelectedTab] = useState("Todos")

  const [filters, setFilters] = useState({
    frequency: "",
    status: "",
    category: "",
  })

  const [formData, setFormData] = useState({
    name: "",
    category: "",
    frequency: "Diario",
    cutOffDate: "",
    amount: "",
    status: "Pendiente",
  })

  const getRemainingDays = (date: string) => {
    const today = new Date()
    const targetDate = new Date(date)

    today.setHours(0, 0, 0, 0)
    targetDate.setHours(0, 0, 0, 0)

    const difference = targetDate.getTime() - today.getTime()

    return Math.ceil(difference / (1000 * 60 * 60 * 24))
  }

  const formatDate = (dateStr: string) => {
    if (!dateStr) return ""

    const months = [
      "ene",
      "feb",
      "mar",
      "abr",
      "may",
      "jun",
      "jul",
      "ago",
      "sep",
      "oct",
      "nov",
      "dic",
    ]

    const [year, month, day] = dateStr.split("-")

    return `${day} ${months[parseInt(month) - 1]} ${year}`
  }

  const filteredExpenses = useMemo(() => {
    let filtered = [...expenses]

    if (selectedTab === "Próximos a vencer") {
      filtered = filtered.filter((expense) => {
        const days = getRemainingDays(expense.cutOffDate)
        return days >= 0 && days <= 7
      })
    }

    if (filters.frequency) {
      filtered = filtered.filter(
        (expense) => expense.frequency === filters.frequency
      )
    }

    if (filters.status) {
      filtered = filtered.filter(
        (expense) => expense.status === filters.status
      )
    }

    if (filters.category) {
      filtered = filtered.filter(
        (expense) => expense.category === filters.category
      )
    }

    return filtered
  }, [expenses, filters, selectedTab])

  const handleAddExpense = () => {
    if (
      !formData.name ||
      !formData.category ||
      !formData.cutOffDate ||
      !formData.amount
    ) {
      alert("Completa todos los campos")
      return
    }

    const newExpense: FixedExpense = {
      id: Date.now(),
      name: formData.name,
      category: formData.category,
      frequency: formData.frequency,
      cutOffDate: formData.cutOffDate,
      amount: Number(formData.amount),
      status: formData.status as "Pendiente" | "Pagado",
    }

    setExpenses([newExpense, ...expenses])

    setFormData({
      name: "",
      category: "",
      frequency: "Diario",
      cutOffDate: "",
      amount: "",
      status: "Pendiente",
    })

    setShowModal(false)
  }

  const handleDelete = (id: number) => {
    setExpenses(expenses.filter((expense) => expense.id !== id))
  }

  return (
    <section className={styles.content}>
      <h2 className={styles.title}>Gastos Fijos</h2>

      {/* FILTROS */}
      <div className={styles.actionsRow}>
        <div className={styles.filters}>
          <select
            className={styles.select}
            value={filters.frequency}
            onChange={(e) =>
              setFilters({ ...filters, frequency: e.target.value })
            }
          >
            <option value="">Todas las frecuencias</option>
            <option value="Diario">Diario</option>
            <option value="Semanal">Semanal</option>
            <option value="Mensual">Mensual</option>
            <option value="Anual">Anual</option>
          </select>

          <select
            className={styles.select}
            value={filters.status}
            onChange={(e) =>
              setFilters({ ...filters, status: e.target.value })
            }
          >
            <option value="">Todos los estados</option>
            <option value="Pendiente">Pendiente</option>
            <option value="Pagado">Pagado</option>
          </select>

          <select
            className={styles.select}
            value={filters.category}
            onChange={(e) =>
              setFilters({ ...filters, category: e.target.value })
            }
          >
            <option value="">Por clasificación</option>
            <option value="Salud">Salud</option>
            <option value="Vivienda">Vivienda</option>
            <option value="Suscripción">Suscripción</option>
            <option value="Servicios">Servicios</option>
          </select>
        </div>

        <button
          className={styles.newButton}
          onClick={() => setShowModal(true)}
        >
          <Plus size={18} />
          Nuevo gasto
        </button>
      </div>

      {/* TABS */}
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${
            selectedTab === "Todos" ? styles.activeTab : ""
          }`}
          onClick={() => setSelectedTab("Todos")}
        >
          Todos
        </button>

        <button
          className={`${styles.tab} ${
            selectedTab === "Próximos a vencer" ? styles.activeTab : ""
          }`}
          onClick={() => setSelectedTab("Próximos a vencer")}
        >
          Próximos a vencer
        </button>
      </div>

      {/* CARDS */}
      <div className={styles.cardsGrid}>
        {filteredExpenses.map((expense) => {
          const remainingDays = getRemainingDays(expense.cutOffDate)

          return (
            <div
              key={expense.id}
              className={`${styles.card} ${
                expense.status === "Pagado"
                  ? styles.greenBorder
                  : remainingDays <= 0
                  ? styles.redBorder
                  : styles.orangeBorder
              }`}
            >
              <button
                className={styles.deleteBtn}
                onClick={() => handleDelete(expense.id)}
              >
                <Trash2 size={16} />
              </button>

              <h3>{expense.name}</h3>

              <span className={styles.category}>
                {expense.category.toUpperCase()}
              </span>

              <div className={styles.paymentInfo}>
                <p>Próximo pago</p>

                <div className={styles.dateRow}>
                  <span>{formatDate(expense.cutOffDate)}</span>

                  {expense.status !== "Pagado" && (
                    <strong
                      className={
                        remainingDays <= 0
                          ? styles.redDays
                          : styles.orangeDays
                      }
                    >
                      ({remainingDays} días)
                    </strong>
                  )}
                </div>
              </div>

              <h2>{formatCurrency(expense.amount)}</h2>

              <div className={styles.badges}>
                <span
                  className={
                    expense.status === "Pagado"
                      ? styles.paidBadge
                      : styles.pendingBadge
                  }
                >
                  {expense.status}
                </span>

                <span className={styles.frequencyBadge}>
                  {expense.frequency}
                </span>
              </div>
            </div>
          )
        })}
      </div>

      {/* EMPTY */}
      {filteredExpenses.length === 0 && (
        <p className={styles.empty}>No se encontraron gastos fijos.</p>
      )}

      {/* MODAL */}
      {showModal && (
        <div
          className={styles.modalOverlay}
          onClick={() => setShowModal(false)}
        >
          <div
            className={styles.modal}
            onClick={(e) => e.stopPropagation()}
          >
            <div className={styles.modalHeader}>
              <h2>Nuevo Gasto</h2>

              <button
                className={styles.closeBtn}
                onClick={() => setShowModal(false)}
              >
                <X size={20} />
              </button>
            </div>

            <div className={styles.modalBody}>
              <div className={styles.formGroup}>
                <label>Nombre</label>

                <input
                  type="text"
                  className={styles.input}
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                />
              </div>

              <div className={styles.formGroup}>
                <label>Clasificación</label>

                <select
                  className={styles.input}
                  value={formData.category}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      category: e.target.value,
                    })
                  }
                >
                  <option value="">Seleccionar</option>
                  <option value="Salud">Salud</option>
                  <option value="Vivienda">Vivienda</option>
                  <option value="Suscripción">Suscripción</option>
                  <option value="Servicios">Servicios</option>
                </select>
              </div>

              <div className={styles.formGroup}>
                <label>Frecuencia</label>

                <select
                  className={styles.input}
                  value={formData.frequency}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      frequency: e.target.value,
                    })
                  }
                >
                  <option value="Diario">Diario</option>
                  <option value="Semanal">Semanal</option>
                  <option value="Mensual">Mensual</option>
                  <option value="Anual">Anual</option>
                </select>
              </div>

              <div className={styles.formGroup}>
                <label>Fecha de corte</label>

                <input
                  type="date"
                  className={styles.input}
                  value={formData.cutOffDate}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      cutOffDate: e.target.value,
                    })
                  }
                />
              </div>

              <div className={styles.formGroup}>
                <label>Monto</label>

                <input
                  type="number"
                  className={styles.input}
                  value={formData.amount}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      amount: e.target.value,
                    })
                  }
                />
              </div>

              <div className={styles.formGroup}>
                <label>Estado</label>

                <select
                  className={styles.input}
                  value={formData.status}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      status: e.target.value,
                    })
                  }
                >
                  <option value="Pendiente">Pendiente</option>
                  <option value="Pagado">Pagado</option>
                </select>
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button
                className={styles.cancelButton}
                onClick={() => setShowModal(false)}
              >
                Cancelar
              </button>

              <button
                className={styles.saveButton}
                onClick={handleAddExpense}
              >
                Guardar
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default FixedExpenses