import { useMemo, useState } from "react" 
import styles from "./FixedExpenses.module.css"
import { Plus, X, Trash2, CheckCircle2, Clock } from "lucide-react"

interface FixedExpense {
  id: number
  name: string
  description: string
  category: string
  frequency: string
  cutOffDate: string
  amount: number
  status: "Pendiente" | "Pagado"
}

const formatCurrency = (amount: number) =>
  "$" +
  amount.toLocaleString("es-CO", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })

function FixedExpenses() {
  const [showModal, setShowModal] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<FixedExpense | null>(null)

  // TODO: quitar los datos hardcodeados y dejar el array vacío cuando el backend esté listo
  const [expenses, setExpenses] = useState<FixedExpense[]>([
    {
      id: 1,
      name: "Gimnasio",
      description: "",
      category: "Salud",
      frequency: "Mensual",
      cutOffDate: "2026-02-27",
      amount: 45000,
      status: "Pendiente",
    },
    {
      id: 2,
      name: "Alquiler",
      description: "Arriendo apartamento",
      category: "Vivienda",
      frequency: "Mensual",
      cutOffDate: "2026-05-19",
      amount: 850000,
      status: "Pendiente",
    },
    {
      id: 3,
      name: "Netflix",
      description: "",
      category: "Suscripción",
      frequency: "Mensual",
      cutOffDate: "2026-02-15",
      amount: 22000,
      status: "Pagado",
    },
  ])

  // TODO: GET /api/fixed-expenses — descomentar cuando el backend esté listo
  // useEffect(() => {
  //   fetch("/api/fixed-expenses")
  //     .then((res) => res.json())
  //     .then((data) => setExpenses(data))
  //     .catch((err) => console.error("Error al cargar gastos fijos:", err))
  // }, [])

  const [selectedTab, setSelectedTab] = useState("Todos")

  const [filters, setFilters] = useState({
    frequency: "",
    status: "",
    category: "",
  })

  const [formData, setFormData] = useState({
    name: "",
    description: "",
    category: "",
    frequency: "Mensual",
    cutOffDate: "",
    amount: "",
    status: "Pendiente",
  })

  const [formErrors, setFormErrors] = useState<Record<string, string>>({})

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

  const handleAddExpense = () => {
    const errors = validateForm()
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors)
      return
    }

    const newExpense: FixedExpense = {
      id: Date.now(), // TODO: quitar este id, el backend lo asignará automáticamente
      name: formData.name,
      description: formData.description,
      category: formData.category,
      frequency: formData.frequency,
      cutOffDate: formData.cutOffDate,
      amount: Number(formData.amount),
      status: formData.status as "Pendiente" | "Pagado",
    }

    // TODO: POST /api/fixed-expenses — descomentar cuando el backend esté listo
    // El objeto que devuelve el backend trae el id real generado por la BD
    // fetch("/api/fixed-expenses", {
    //   method: "POST",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify(newExpense),
    // })
    //   .then((res) => res.json())
    //   .then((created: FixedExpense) => setExpenses([created, ...expenses]))
    //   .catch((err) => console.error("Error al crear gasto fijo:", err))

    // TODO: eliminar esta línea y moverla dentro del .then() de arriba
    setExpenses([newExpense, ...expenses])

    setFormData({
      name: "",
      description: "",
      category: "",
      frequency: "Mensual",
      cutOffDate: "",
      amount: "",
      status: "Pendiente",
    })
    setFormErrors({})
    setShowModal(false)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setFormErrors({})
    setFormData({
      name: "",
      description: "",
      category: "",
      frequency: "Mensual",
      cutOffDate: "",
      amount: "",
      status: "Pendiente",
    })
  }

  const handleDelete = (id: number) => {
    // TODO: DELETE /api/fixed-expenses/:id — descomentar cuando el backend esté listo
    // fetch(`/api/fixed-expenses/${id}`, {
    //   method: "DELETE",
    // })
    //   .then(() => setExpenses(expenses.filter((e) => e.id !== id)))
    //   .catch((err) => console.error("Error al eliminar gasto fijo:", err))

    // TODO: eliminar esta línea y moverla dentro del .then() de arriba
    setExpenses(expenses.filter((e) => e.id !== id))
    setDeleteTarget(null)
  }

  const handleMarkPaid = (id: number) => {
    // TODO: PATCH /api/fixed-expenses/:id — descomentar cuando el backend esté listo
    // fetch(`/api/fixed-expenses/${id}`, {
    //   method: "PATCH",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ status: "Pagado" }),
    // })
    //   .then(() =>
    //     setExpenses(expenses.map((e) => e.id === id ? { ...e, status: "Pagado" } : e))
    //   )
    //   .catch((err) => console.error("Error al actualizar estado:", err))

    // TODO: eliminar esta línea y moverla dentro del .then() de arriba
    setExpenses(
      expenses.map((e) => e.id === id ? { ...e, status: "Pagado" } : e)
    )
  }

  const handleMarkPending = (id: number) => {
    // TODO: PATCH /api/fixed-expenses/:id — descomentar cuando el backend esté listo
    // fetch(`/api/fixed-expenses/${id}`, {
    //   method: "PATCH",
    //   headers: { "Content-Type": "application/json" },
    //   body: JSON.stringify({ status: "Pendiente" }),
    // })
    //   .then(() =>
    //     setExpenses(expenses.map((e) => e.id === id ? { ...e, status: "Pendiente" } : e))
    //   )
    //   .catch((err) => console.error("Error al actualizar estado:", err))

    // TODO: eliminar esta línea y moverla dentro del .then() de arriba
    setExpenses(
      expenses.map((e) => e.id === id ? { ...e, status: "Pendiente" } : e)
    )
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
            onChange={(e) => setFilters({ ...filters, frequency: e.target.value })}
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
        {filteredExpenses.map((expense) => {
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

      {filteredExpenses.length === 0 && (
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
                    setFormData({ ...formData, frequency: e.target.value })
                  }
                >
                  <option value="Diario">Diario</option>
                  <option value="Semanal">Semanal</option>
                  <option value="Mensual">Mensual</option>
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
                    setFormData({ ...formData, status: e.target.value })
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
              <button className={styles.saveButton} onClick={handleAddExpense}>
                Guardar
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
