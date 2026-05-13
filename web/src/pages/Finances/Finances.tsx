import { useMemo, useState } from "react"
import styles from "./Finances.module.css"
import { Plus, Search, X, ArrowUp, ArrowDown, Trash2 } from "lucide-react"

interface Movement {
  id: number
  type: "Ingreso" | "Egreso"
  title: string
  category: string
  amount: number
  date: string
}

const formatCurrency = (amount: number) =>
  "$" + amount.toLocaleString("es-CO", { minimumFractionDigits: 2, maximumFractionDigits: 2 })

function Finances() {
  const [showModal, setShowModal] = useState(false)
  const [search, setSearch] = useState("")
  const [movements, setMovements] = useState<Movement[]>([
    { id: 1, type: "Ingreso", title: "Salario", category: "Trabajo", amount: 1500, date: "2026-02-20" },
    { id: 2, type: "Egreso", title: "Supermercado", category: "Alimentación", amount: 180, date: "2026-02-21" },
  ])
  const [formData, setFormData] = useState({
    type: "Ingreso", title: "", category: "", amount: "", date: "", description: "",
  })

  const filteredMovements = movements.filter((m) =>
    m.title.toLowerCase().includes(search.toLowerCase())
  )

  const totalIncome = useMemo(() =>
    movements.filter((m) => m.type === "Ingreso").reduce((acc, curr) => acc + curr.amount, 0),
    [movements]
  )

  const totalExpenses = useMemo(() =>
    movements.filter((m) => m.type === "Egreso").reduce((acc, curr) => acc + curr.amount, 0),
    [movements]
  )

  const balance = totalIncome - totalExpenses

  const formatDate = (dateStr: string) => {
    if (!dateStr) return ""
    const [year, month, day] = dateStr.split("-")
    const months = ["Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"]
    return `${day}/${months[parseInt(month) - 1]}/${year}`
  }

  const handleAddMovement = () => {
    if (!formData.title || !formData.category || !formData.amount || !formData.date) {
      alert("Completa todos los campos")
      return
    }
    const newMovement: Movement = {
      id: Date.now(),
      type: formData.type as "Ingreso" | "Egreso",
      title: formData.title,
      category: formData.category,
      amount: Number(formData.amount),
      date: formData.date,
    }
    setMovements([newMovement, ...movements])
    setFormData({ type: "Ingreso", title: "", category: "", amount: "", date: "", description: "" })
    setShowModal(false)
  }

  const handleDelete = (id: number) => {
    setMovements(movements.filter((m) => m.id !== id))
  }

  return (
    <section className={styles.content}>
      <h2 className={styles.title}>Finanzas</h2>

      {/* RESUMEN */}
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

      {/* SEARCH */}
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
        <button className={styles.newButton} onClick={() => setShowModal(true)}>
          <Plus size={16} />
          Nuevo Movimiento
        </button>
      </div>

      {/* MOVIMIENTOS */}
      <div className={styles.movements}>
        {filteredMovements.map((movement) => (
          <div key={movement.id} className={styles.movementCard}>
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
              <p>{formatDate(movement.date)}</p>
            </div>
            <button className={styles.deleteBtn} onClick={() => handleDelete(movement.id)}>
              <Trash2 size={16} />
            </button>
          </div>
        ))}
        {filteredMovements.length === 0 && (
          <p className={styles.empty}>No se encontraron movimientos.</p>
        )}
      </div>

      {/* MODAL */}
      {showModal && (
        <div className={styles.modalOverlay} onClick={() => setShowModal(false)}>
          <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Nuevo Movimiento</h2>
              <button className={styles.closeBtn} onClick={() => setShowModal(false)}>
                <X size={20} />
              </button>
            </div>

            <div className={styles.modalBody}>
              <div className={styles.formGroup}>
                <label>Tipo</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value, category: "" })}
                  className={styles.input}
                >
                  <option>Ingreso</option>
                  <option>Egreso</option>
                </select>
              </div>

              <div className={styles.formGroup}>
                <label>Título</label>
                <input
                  type="text"
                  className={styles.input}
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </div>

              <div className={styles.formGroup}>
                <label>Clasificación</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  className={styles.input}
                >
                  <option value="">Seleccionar</option>
                  {formData.type === "Ingreso" ? (
                    <>
                      <option value="Trabajo">Trabajo</option>
                      <option value="Freelance">Freelance</option>
                      <option value="Inversiones">Inversiones</option>
                      <option value="Ventas">Ventas</option>
                      <option value="Otros">Otros</option>
                    </>
                  ) : (
                    <>
                      <option value="Alimentación">Alimentación</option>
                      <option value="Transporte">Transporte</option>
                      <option value="Servicios">Servicios</option>
                      <option value="Entretenimiento">Entretenimiento</option>
                      <option value="Salud">Salud</option>
                      <option value="Otros">Otros</option>
                    </>
                  )}
                </select>
              </div>

              <div className={styles.formGroup}>
                <label>Monto</label>
                <input
                  type="number"
                  className={styles.input}
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                />
              </div>

              <div className={styles.formGroup}>
                <label>Fecha</label>
                <input
                  type="date"
                  className={styles.input}
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                />
              </div>

              <div className={styles.formGroup}>
                <label>Descripción</label>
                <textarea
                  className={styles.textarea}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setShowModal(false)}>Cancelar</button>
              <button className={styles.saveButton} onClick={handleAddMovement}>Guardar</button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default Finances