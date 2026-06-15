import { useEffect, useMemo, useState } from "react"
import styles from "./Finances.module.css"
import { Plus, Search, X, ArrowUp, ArrowDown, Trash2 } from "lucide-react"
import financesService, { type Movement } from "../../services/financeService"

const formatCurrency = (amount: number) =>
  "$" + amount.toLocaleString("es-CO", { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const formatDate = (dateStr: string) => {
  if (!dateStr) return ""
  const months = ["Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"]
  const [year, month, day] = dateStr.split("-")
  return `${day}/${months[parseInt(month) - 1]}/${year}`
}

const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === "object" && error !== null && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    if (response?.data?.message) return response.data.message
  }

  return error instanceof Error ? error.message : fallback
}

function Finances() {
  const [showModal, setShowModal]           = useState(false)
  const [deleteTarget, setDeleteTarget]     = useState<Movement | null>(null)
  const [detailTarget, setDetailTarget]     = useState<Movement | null>(null)
  const [search, setSearch]                 = useState("")
  const [filterType, setFilterType]         = useState("")
  const [loading, setLoading]               = useState(true)
  const [saving, setSaving]                 = useState(false)
  const [error, setError]                   = useState("")

  const [movements, setMovements] = useState<Movement[]>([])

  // ─── Formulario 
  const [formData, setFormData] = useState({
    type: "Ingreso",
    title: "",
    category: "",
    amount: "",
    entryDate: "",
    registerDate: "",
    description: "",
  })
  const [formErrors, setFormErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    let mounted = true

    const loadMovements = async () => {
      setLoading(true)
      setError("")

      try {
        const data = await financesService.getAll()
        if (mounted) setMovements(data)
      } catch (err) {
        if (mounted) setError(getErrorMessage(err, "No se pudieron cargar los movimientos."))
      } finally {
        if (mounted) setLoading(false)
      }
    }

    void loadMovements()

    return () => {
      mounted = false
    }
  }, [])

  // ─── Filtrado 
  const filteredMovements = useMemo(() => {
    let list = [...movements]
    if (search)      list = list.filter((m) => m.title.toLowerCase().includes(search.toLowerCase()))
    if (filterType)  list = list.filter((m) => m.type === filterType)
    return list
  }, [movements, search, filterType])

  // ─── Totales 
  const totalIncome   = useMemo(() => movements.filter((m) => m.type === "Ingreso").reduce((a, c) => a + c.amount, 0), [movements])
  const totalExpenses = useMemo(() => movements.filter((m) => m.type === "Egreso").reduce((a, c) => a + c.amount, 0), [movements])
  const balance       = totalIncome - totalExpenses

  // ─── Validación 
  const validateForm = () => {
    const errors: Record<string, string> = {}
    if (!formData.title.trim())   errors.title      = "El título es obligatorio"
    if (!formData.category)       errors.category   = "La clasificación es obligatoria"
    if (!formData.amount || Number(formData.amount) <= 0) errors.amount = "El monto debe ser mayor a 0"
    if (!formData.entryDate)      errors.entryDate  = "La fecha de ingreso es obligatoria"
    if (!formData.registerDate)   errors.registerDate = "La fecha de registro es obligatoria"
    return errors
  }

  // ─── Agregar movimiento 
  const handleAddMovement = async () => {
    const errors = validateForm()
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return }

    setSaving(true)
    setError("")

    try {
      const created = await financesService.create({
        type:         formData.type as "Ingreso" | "Egreso",
        title:        formData.title,
        category:     formData.category,
        amount:       Number(formData.amount),
        entryDate:    formData.entryDate,
        registerDate: formData.registerDate,
        description:  formData.description,
      })
      setMovements((current) => [created, ...current])
      handleCloseModal()
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo crear el movimiento."))
    } finally {
      setSaving(false)
    }
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setFormErrors({})
    setFormData({ type: "Ingreso", title: "", category: "", amount: "", entryDate: "", registerDate: "", description: "" })
  }

  // ─── Eliminar movimiento 
  const handleDelete = async (id: string) => {
    setError("")

    try {
      await financesService.delete(id)
      setMovements((current) => current.filter((m) => m.id !== id))
      setDeleteTarget(null)
    } catch (err) {
      setError(getErrorMessage(err, "No se pudo eliminar el movimiento."))
    }
  }

  // ─── Helpers form 
  const incomeCategories  = ["Trabajo","Freelance","Inversiones","Ventas","Personal","Otros"]
  const expenseCategories = ["Alimentación","Transporte","Servicios","Entretenimiento","Salud","Otros"]

  return (
    <section className={styles.content}>
      <h2 className={styles.title}>Finanzas</h2>
      {error && <p className={styles.empty}>{error}</p>}

      {/* ── RESUMEN ── */}
      <div className={styles.summaryGrid}>
        <div className={styles.summaryCard}>
          <div className={styles.summaryItem}>
            <ArrowUp className={styles.incomeIcon} size={24} />
            <div>
              <p>TOTAL INGRESOS</p>
              <h3 className={styles.incomeText}>{formatCurrency(totalIncome)}</h3>
            </div>
          </div>
          <div className={styles.divider}></div>
          <div className={styles.summaryItem}>
            <ArrowDown className={styles.expenseIcon} size={24} />
            <div>
              <p>TOTAL EGRESOS</p>
              <h3 className={styles.expenseText}>{formatCurrency(totalExpenses)}</h3>
            </div>
          </div>
        </div>
        <div className={styles.balanceCard}>
          <p>SALDO ACTUAL</p>
          <h3>{formatCurrency(balance)}</h3>
        </div>
      </div>

      {/* ── ACCIONES ── */}
      <div className={styles.actionsRow}>
        <div className={styles.searchBox}>
          <Search size={18} />
          <input
            type="text"
            placeholder="Buscar movimiento..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && <X size={16} style={{ cursor: "pointer" }} onClick={() => setSearch("")} />}
        </div>

        <div className={styles.rightActions}>
          <select
            className={styles.filterSelect}
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
          >
            <option value="">Todos los movimientos</option>
            <option value="Ingreso">Solo ingresos</option>
            <option value="Egreso">Solo egresos</option>
          </select>

          <button className={styles.newButton} onClick={() => setShowModal(true)}>
            <Plus size={16} />
            Nuevo Movimiento
          </button>
        </div>
      </div>

      {/* ── LISTA DE MOVIMIENTOS ── */}
      <div className={styles.movements}>
        {loading && <p className={styles.empty}>Cargando movimientos...</p>}

        {!loading && filteredMovements.map((movement) => (
          <div
            key={movement.id}
            className={styles.movementCard}
            onClick={() => setDetailTarget(movement)}
          >
            <div className={styles.leftMovement}>
              <div className={movement.type === "Ingreso" ? styles.incomeBadge : styles.expenseBadge}>
                {movement.type === "Ingreso" ? <ArrowUp size={18} /> : <ArrowDown size={18} />}
              </div>
              <div>
                <h3>{movement.title}</h3>
                <p>{movement.category}</p>
              </div>
            </div>
            <div className={styles.rightMovement}>
              <h3 className={movement.type === "Ingreso" ? styles.positive : styles.negative}>
                {movement.type === "Ingreso" ? "+" : "−"} {formatCurrency(movement.amount)}
              </h3>
              <p>{formatDate(movement.entryDate)}</p>
            </div>
            <button
              className={styles.deleteBtn}
              onClick={(e) => { e.stopPropagation(); setDeleteTarget(movement) }}
              title="Eliminar"
            >
              <Trash2 size={16} />
            </button>
          </div>
        ))}
        {!loading && filteredMovements.length === 0 && (
          <p className={styles.empty}>No se encontraron movimientos.</p>
        )}
      </div>

      {/* Modal - nuevo movimiento*/}
      {showModal && (
        <div className={styles.modalOverlay} onClick={handleCloseModal}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Nuevo Movimiento</h2>
              <button className={styles.closeBtn} onClick={handleCloseModal}>
                <X size={20} />
              </button>
            </div>

            <div className={styles.modalBody}>

              {/* Tipo */}
              <div className={styles.formGroup}>
                <label>Movimiento <span className={styles.required}>*</span></label>
                <select
                  className={styles.input}
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value, category: "" })}
                >
                  <option value="Ingreso">Ingreso</option>
                  <option value="Egreso">Egreso</option>
                </select>
              </div>

              {/* Título */}
              <div className={styles.formGroup}>
                <label>Título <span className={styles.required}>*</span></label>
                <input
                  type="text"
                  className={`${styles.input} ${formErrors.title ? styles.inputError : ""}`}
                  placeholder="Ej. Salario, Mercado..."
                  value={formData.title}
                  onChange={(e) => { setFormData({ ...formData, title: e.target.value }); setFormErrors({ ...formErrors, title: "" }) }}
                />
                {formErrors.title && <span className={styles.errorMsg}>{formErrors.title}</span>}
              </div>

              {/* Fecha ingreso */}
              <div className={styles.formGroup}>
                <label>Fecha ingreso <span className={styles.required}>*</span></label>
                <input
                  type="date"
                  className={`${styles.input} ${formErrors.entryDate ? styles.inputError : ""}`}
                  value={formData.entryDate}
                  onChange={(e) => { setFormData({ ...formData, entryDate: e.target.value }); setFormErrors({ ...formErrors, entryDate: "" }) }}
                />
                {formErrors.entryDate && <span className={styles.errorMsg}>{formErrors.entryDate}</span>}
              </div>

              {/* Fecha registro */}
              <div className={styles.formGroup}>
                <label>Fecha de registro <span className={styles.required}>*</span></label>
                <input
                  type="date"
                  className={`${styles.input} ${formErrors.registerDate ? styles.inputError : ""}`}
                  value={formData.registerDate}
                  onChange={(e) => { setFormData({ ...formData, registerDate: e.target.value }); setFormErrors({ ...formErrors, registerDate: "" }) }}
                />
                {formErrors.registerDate && <span className={styles.errorMsg}>{formErrors.registerDate}</span>}
              </div>

              {/* Clasificación */}
              <div className={styles.formGroup}>
                <label>Clasificación <span className={styles.required}>*</span></label>
                <select
                  className={`${styles.input} ${formErrors.category ? styles.inputError : ""}`}
                  value={formData.category}
                  onChange={(e) => { setFormData({ ...formData, category: e.target.value }); setFormErrors({ ...formErrors, category: "" }) }}
                >
                  <option value="">Seleccionar</option>
                  {(formData.type === "Ingreso" ? incomeCategories : expenseCategories).map((cat) => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
                {formErrors.category && <span className={styles.errorMsg}>{formErrors.category}</span>}
              </div>

              {/* Monto */}
              <div className={styles.formGroup}>
                <label>Monto <span className={styles.required}>*</span></label>
                <input
                  type="number"
                  min="0"
                  className={`${styles.input} ${formErrors.amount ? styles.inputError : ""}`}
                  placeholder="0.00"
                  value={formData.amount}
                  onChange={(e) => { setFormData({ ...formData, amount: e.target.value }); setFormErrors({ ...formErrors, amount: "" }) }}
                />
                {formErrors.amount && <span className={styles.errorMsg}>{formErrors.amount}</span>}
              </div>

              {/* Descripción (opcional) */}
              <div className={styles.formGroup}>
                <label>Descripción <span className={styles.optional}>(opcional)</span></label>
                <textarea
                  className={styles.textarea}
                  placeholder="Agrega una nota o descripción..."
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={handleCloseModal}>Cancelar</button>
              <button className={styles.saveButton} onClick={handleAddMovement} disabled={saving}>
                {saving ? "Guardando..." : "Guardar"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal - detalles del movimiento*/}
      {detailTarget && (
        <div className={styles.modalOverlay} onClick={() => setDetailTarget(null)}>
          <div className={`${styles.modal} ${styles.detailModal}`} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Detalle del Movimiento</h2>
              <button className={styles.closeBtn} onClick={() => setDetailTarget(null)}>
                <X size={20} />
              </button>
            </div>

            <div className={styles.modalBody}>
              {/* Tipo badge */}
              <div className={styles.detailBadgeRow}>
                <span className={detailTarget.type === "Ingreso" ? styles.detailIncomeBadge : styles.detailExpenseBadge}>
                  {detailTarget.type === "Ingreso" ? <ArrowUp size={14} /> : <ArrowDown size={14} />}
                  {detailTarget.type}
                </span>
              </div>

              {/* Monto destacado */}
              <div className={styles.detailAmount}>
                <span className={detailTarget.type === "Ingreso" ? styles.positive : styles.negative}>
                  {detailTarget.type === "Ingreso" ? "+" : "−"} {formatCurrency(detailTarget.amount)}
                </span>
              </div>

              {/* Campos */}
              <div className={styles.detailGrid}>
                <div className={styles.detailField}>
                  <span className={styles.detailLabel}>Título</span>
                  <span className={styles.detailValue}>{detailTarget.title}</span>
                </div>
                <div className={styles.detailField}>
                  <span className={styles.detailLabel}>Clasificación</span>
                  <span className={styles.detailValue}>{detailTarget.category}</span>
                </div>
                <div className={styles.detailField}>
                  <span className={styles.detailLabel}>Fecha ingreso</span>
                  <span className={styles.detailValue}>{formatDate(detailTarget.entryDate)}</span>
                </div>
                <div className={styles.detailField}>
                  <span className={styles.detailLabel}>Fecha registro</span>
                  <span className={styles.detailValue}>{formatDate(detailTarget.registerDate)}</span>
                </div>
                {detailTarget.description && (
                  <div className={`${styles.detailField} ${styles.detailFullWidth}`}>
                    <span className={styles.detailLabel}>Descripción</span>
                    <span className={styles.detailValue}>{detailTarget.description}</span>
                  </div>
                )}
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setDetailTarget(null)}>Cerrar</button>
              <button
                className={styles.deleteConfirmButton}
                onClick={() => { setDeleteTarget(detailTarget); setDetailTarget(null) }}
              >
                <Trash2 size={15} /> Eliminar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal - confirmar eliminación*/}
      {deleteTarget && (
        <div className={styles.modalOverlay} onClick={() => setDeleteTarget(null)}>
          <div className={`${styles.modal} ${styles.confirmModal}`} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Eliminar movimiento</h2>
              <button className={styles.closeBtn} onClick={() => setDeleteTarget(null)}>
                <X size={20} />
              </button>
            </div>

            <div className={styles.confirmBody}>
              <div className={styles.confirmIcon}>
                <Trash2 size={32} />
              </div>
              <p className={styles.confirmText}>
                ¿Estás seguro de que deseas eliminar <strong>"{deleteTarget.title}"</strong>?
              </p>
              <p className={styles.confirmSubtext}>Esta acción no se puede deshacer.</p>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setDeleteTarget(null)}>Cancelar</button>
              <button className={styles.deleteConfirmButton} onClick={() => handleDelete(deleteTarget.id)}>
                Sí, eliminar
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default Finances
