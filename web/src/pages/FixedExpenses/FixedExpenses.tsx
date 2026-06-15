import { useEffect, useMemo, useState } from "react" 
import styles from "./FixedExpenses.module.css"
import { Plus, X, Trash2, CheckCircle2, Clock } from "lucide-react"
import fixedExpensesService, {
  type FixedExpense,
  type FixedExpenseFrequency,
  type FixedExpenseStatus,
} from "../../services/fixedExpenses.service"

interface FixedExpenseForm {
  name: string
  description: string
  category: string
  frequency: FixedExpenseFrequency
  cutOffDate: string
  amount: string
  status: FixedExpenseStatus
}

const formatCurrency = (amount: number) =>
  "$" +
  amount.toLocaleString("es-CO", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })

const emptyForm: FixedExpenseForm = {
  name: "",
  description: "",
  category: "",
  frequency: "Mensual",
  cutOffDate: "",
  amount: "",
  status: "Pendiente",
}

const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }

  return error instanceof Error ? error.message : fallback
}

function FixedExpenses() {
  const [showModal, setShowModal] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<FixedExpense | null>(null)
  const [expenses, setExpenses] = useState<FixedExpense[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")

  const [selectedTab, setSelectedTab] = useState("Todos")

  const [filters, setFilters] = useState({
    frequency: "",
    status: "",
    category: "",
  })

  const [formData, setFormData] = useState<FixedExpenseForm>(emptyForm)

  const [formErrors, setFormErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    let mounted = true

    const loadExpenses = async () => {
      setLoading(true)
      setError("")

      try {
        const data = await fixedExpensesService.getAll()
        if (mounted) setExpenses(data)
      } catch (err) {
        if (mounted) setError(getErrorMessage(err, "No se pudieron cargar los gastos fijos."))
      } finally {
        if (mounted) setLoading(false)
      }
    }

    void loadExpenses()

    return () => {
      mounted = false
    }
  }, [])

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
    const months = ["ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic"]
    const [year, month, day] = dateStr.split("-")
    return `${day} ${months[parseInt(month) - 1]} ${year}`
  }

  const getBorderClass = (expense: FixedExpense) => {
    if (expense.status === "Pagado") return styles.greenBorder
    const days = getRemainingDays(expense.cutOffDate)
    if (days <= 0) return styles.redBorder
    if (days <= 7) return styles.orangeBorder
    return styles.defaultBorder
  }

  const filteredExpenses = useMemo(() => {
    let filtered = [...expenses]

    if (selectedTab === "Próximos a vencer") {
      filtered = filtered.filter((expense) => {
        const days = getRemainingDays(expense.cutOffDate)
        return days >= 0 && days <= 7 && expense.status !== "Pagado"
      })
    }

    if (filters.frequency)
      filtered = filtered.filter((e) => e.frequency === filters.frequency)
    if (filters.status)
      filtered = filtered.filter((e) => e.status === filters.status)
    if (filters.category)
      filtered = filtered.filter((e) => e.category === filters.category)

    return filtered
  }, [expenses, filters, selectedTab])

  const validateForm = () => {
    const errors: Record<string, string> = {}
    if (!formData.name.trim()) errors.name = "El nombre es obligatorio"
    if (!formData.category) errors.category = "La clasificación es obligatoria"
    if (!formData.cutOffDate) errors.cutOffDate = "La fecha de corte es obligatoria"
    if (!formData.amount || Number(formData.amount) <= 0)
      errors.amount = "El monto debe ser mayor a 0"
    return errors
  }

  const handleAddExpense = async () => {
    const errors = validateForm()
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors)
      return
    }

    setSaving(true)
    setError("")

    try {
      const created = await fixedExpensesService.create({
        name: formData.name,
        description: formData.description,
        category: formData.category,
        frequency: formData.frequency,
        cutOffDate: formData.cutOffDate,
        amount: Number(formData.amount),
        status: formData.status,
      })
      setExpenses((current) => [created, ...current])
      setFormData(emptyForm)
      setFormErrors({})
      setShowModal(false)
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo crear el gasto fijo."))
    } finally {
      setSaving(false)
    }
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setFormErrors({})
    setFormData(emptyForm)
  }

  const handleDelete = async (id: string) => {
    setError("")

    try {
      await fixedExpensesService.delete(id)
      setExpenses((current) => current.filter((e) => e.id !== id))
      setDeleteTarget(null)
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo eliminar el gasto fijo."))
    }
  }

  const handleMarkPaid = async (id: string) => {
    setError("")

    try {
      const updated = await fixedExpensesService.update(id, { status: "Pagado" })
      setExpenses((current) => current.map((e) => e.id === id ? updated : e))
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo actualizar el estado del gasto."))
    }
  }

  const handleMarkPending = async (id: string) => {
    setError("")

    try {
      const updated = await fixedExpensesService.update(id, { status: "Pendiente" })
      setExpenses((current) => current.map((e) => e.id === id ? updated : e))
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo actualizar el estado del gasto."))
    }
  }

  return (
    <section className={styles.content}>
      <h2 className={styles.title}>Gastos Fijos</h2>
      {error && <p className={styles.empty}>{error}</p>}

      {/* FILTROS */}
      <div className={styles.actionsRow}>
        <div className={styles.filters}>
          <select
            className={styles.select}
            value={filters.frequency}
            onChange={(e) => setFilters({ ...filters, frequency: e.target.value })}
          >
            <option value="">Todas las frecuencias</option>
            <option value="Semanal">Semanal</option>
            <option value="Quincenal">Quincenal</option>
            <option value="Mensual">Mensual</option>
            <option value="Trimestral">Trimestral</option>
            <option value="Semestral">Semestral</option>
            <option value="Anual">Anual</option>
          </select>

          <select
            className={styles.select}
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
          >
            <option value="">Todos los estados</option>
            <option value="Pendiente">Pendiente</option>
            <option value="Pagado">Pagado</option>
          </select>

          <select
            className={styles.select}
            value={filters.category}
            onChange={(e) => setFilters({ ...filters, category: e.target.value })}
          >
            <option value="">Por clasificación</option>
            <option value="Salud">Salud</option>
            <option value="Vivienda">Vivienda</option>
            <option value="Suscripción">Suscripción</option>
            <option value="Servicios">Servicios</option>
          </select>
        </div>

        <button className={styles.newButton} onClick={() => setShowModal(true)}>
          <Plus size={18} />
          Nuevo gasto
        </button>
      </div>

      {/* TABS */}
      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${selectedTab === "Todos" ? styles.activeTab : ""}`}
          onClick={() => setSelectedTab("Todos")}
        >
          Todos
        </button>
        <button
          className={`${styles.tab} ${selectedTab === "Próximos a vencer" ? styles.activeTab : ""}`}
          onClick={() => setSelectedTab("Próximos a vencer")}
        >
          Próximos a vencer
        </button>
      </div>

      {/* CARDS */}
      <div className={styles.cardsGrid}>
        {loading && <p className={styles.empty}>Cargando gastos fijos...</p>}

        {!loading && filteredExpenses.map((expense) => {
          const remainingDays = getRemainingDays(expense.cutOffDate)

          return (
            <div key={expense.id} className={`${styles.card} ${getBorderClass(expense)}`}>
              {/* Top actions */}
              <div className={styles.cardActions}>
                <button
                  className={styles.deleteBtn}
                  onClick={() => setDeleteTarget(expense)}
                  title="Eliminar"
                >
                  <Trash2 size={15} />
                </button>
              </div>

              <h3 className={styles.cardName}>{expense.name}</h3>
              <span className={styles.category}>{expense.category.toUpperCase()}</span>

              {expense.description && (
                <p className={styles.description}>{expense.description}</p>
              )}

              <div className={styles.paymentInfo}>
                <p>Próximo pago</p>
                <div className={styles.dateRow}>
                  <span>{formatDate(expense.cutOffDate)}</span>
                  {expense.status !== "Pagado" && (
                    <strong
                      className={remainingDays <= 0 ? styles.redDays : styles.orangeDays}
                    >
                      ({remainingDays} días)
                    </strong>
                  )}
                </div>
              </div>

              <h2 className={styles.amount}>{formatCurrency(expense.amount)}</h2>

              <div className={styles.cardBottom}>
                <div className={styles.badges}>
                  <span
                    className={
                      expense.status === "Pagado" ? styles.paidBadge : styles.pendingBadge
                    }
                  >
                    {expense.status}
                  </span>
                  <span className={styles.frequencyBadge}>{expense.frequency}</span>
                </div>

                {/* Mark as paid / pending */}
                {expense.status === "Pendiente" ? (
                  <button
                    className={styles.markPaidBtn}
                    onClick={() => handleMarkPaid(expense.id)}
                    title="Marcar como pagado"
                  >
                    <CheckCircle2 size={14} />
                    Marcar pagado
                  </button>
                ) : (
                  <button
                    className={styles.markPendingBtn}
                    onClick={() => handleMarkPending(expense.id)}
                    title="Marcar como pendiente"
                  >
                    <Clock size={14} />
                    Pendiente
                  </button>
                )}
              </div>
            </div>
          )
        })}
      </div>

      {!loading && filteredExpenses.length === 0 && (
        <p className={styles.empty}>No se encontraron gastos fijos.</p>
      )}

      {/* ── MODAL NUEVO GASTO ── */}
      {showModal && (
        <div className={styles.modalOverlay} onClick={handleCloseModal}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Nuevo Gasto</h2>
              <button className={styles.closeBtn} onClick={handleCloseModal}>
                <X size={20} />
              </button>
            </div>

            {/* Scrollable body */}
            <div className={styles.modalBody}>
              <div className={styles.formGroup}>
                <label>
                  Nombre <span className={styles.required}>*</span>
                </label>
                <input
                  type="text"
                  className={`${styles.input} ${formErrors.name ? styles.inputError : ""}`}
                  placeholder="Ej. Arriendo, Netflix..."
                  value={formData.name}
                  onChange={(e) => {
                    setFormData({ ...formData, name: e.target.value })
                    setFormErrors({ ...formErrors, name: "" })
                  }}
                />
                {formErrors.name && (
                  <span className={styles.errorMsg}>{formErrors.name}</span>
                )}
              </div>

              <div className={styles.formGroup}>
                <label>
                  Descripción <span className={styles.optional}>(opcional)</span>
                </label>
                <textarea
                  className={styles.textarea}
                  placeholder="Agrega una nota o descripción..."
                  rows={3}
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                />
              </div>

              <div className={styles.formGroup}>
                <label>
                  Clasificación <span className={styles.required}>*</span>
                </label>
                <select
                  className={`${styles.input} ${formErrors.category ? styles.inputError : ""}`}
                  value={formData.category}
                  onChange={(e) => {
                    setFormData({ ...formData, category: e.target.value })
                    setFormErrors({ ...formErrors, category: "" })
                  }}
                >
                  <option value="">Seleccionar</option>
                  <option value="Salud">Salud</option>
                  <option value="Vivienda">Vivienda</option>
                  <option value="Suscripción">Suscripción</option>
                  <option value="Servicios">Servicios</option>
                </select>
                {formErrors.category && (
                  <span className={styles.errorMsg}>{formErrors.category}</span>
                )}
              </div>

              <div className={styles.formGroup}>
                <label>
                  Frecuencia <span className={styles.required}>*</span>
                </label>
                <select
                  className={styles.input}
                  value={formData.frequency}
                  onChange={(e) =>
                    setFormData({ ...formData, frequency: e.target.value as FixedExpenseFrequency })
                  }
                >
                  <option value="Semanal">Semanal</option>
                  <option value="Quincenal">Quincenal</option>
                  <option value="Mensual">Mensual</option>
                  <option value="Trimestral">Trimestral</option>
                  <option value="Semestral">Semestral</option>
                  <option value="Anual">Anual</option>
                </select>
              </div>

              <div className={styles.formGroup}>
                <label>
                  Fecha de corte <span className={styles.required}>*</span>
                </label>
                <input
                  type="date"
                  className={`${styles.input} ${formErrors.cutOffDate ? styles.inputError : ""}`}
                  value={formData.cutOffDate}
                  onChange={(e) => {
                    setFormData({ ...formData, cutOffDate: e.target.value })
                    setFormErrors({ ...formErrors, cutOffDate: "" })
                  }}
                />
                {formErrors.cutOffDate && (
                  <span className={styles.errorMsg}>{formErrors.cutOffDate}</span>
                )}
              </div>

              <div className={styles.formGroup}>
                <label>
                  Monto <span className={styles.required}>*</span>
                </label>
                <input
                  type="number"
                  min="0"
                  className={`${styles.input} ${formErrors.amount ? styles.inputError : ""}`}
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={(e) => {
                    setFormData({ ...formData, amount: e.target.value })
                    setFormErrors({ ...formErrors, amount: "" })
                  }}
                />
                {formErrors.amount && (
                  <span className={styles.errorMsg}>{formErrors.amount}</span>
                )}
              </div>

              <div className={styles.formGroup}>
                <label>
                  Estado <span className={styles.required}>*</span>
                </label>
                <select
                  className={styles.input}
                  value={formData.status}
                  onChange={(e) =>
                    setFormData({ ...formData, status: e.target.value as FixedExpenseStatus })
                  }
                >
                  <option value="Pendiente">Pendiente</option>
                  <option value="Pagado">Pagado</option>
                </select>
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={handleCloseModal}>
                Cancelar
              </button>
              <button className={styles.saveButton} onClick={handleAddExpense} disabled={saving}>
                {saving ? "Guardando..." : "Guardar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL CONFIRMAR ELIMINACIÓN ── */}
      {deleteTarget && (
        <div
          className={styles.modalOverlay}
          onClick={() => setDeleteTarget(null)}
        >
          <div
            className={`${styles.modal} ${styles.confirmModal}`}
            onClick={(e) => e.stopPropagation()}
          >
            <div className={styles.modalHeader}>
              <h2>Eliminar gasto</h2>
              <button
                className={styles.closeBtn}
                onClick={() => setDeleteTarget(null)}
              >
                <X size={20} />
              </button>
            </div>

            <div className={styles.confirmBody}>
              <div className={styles.confirmIcon}>
                <Trash2 size={32} />
              </div>
              <p className={styles.confirmText}>
                ¿Estás seguro de que deseas eliminar{" "}
                <strong>"{deleteTarget.name}"</strong>?
              </p>
              <p className={styles.confirmSubtext}>
                Esta acción no se puede deshacer.
              </p>
            </div>

            <div className={styles.modalButtons}>
              <button
                className={styles.cancelButton}
                onClick={() => setDeleteTarget(null)}
              >
                Cancelar
              </button>
              <button
                className={styles.deleteConfirmButton}
                onClick={() => handleDelete(deleteTarget.id)}
              >
                Sí, eliminar
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default FixedExpenses
