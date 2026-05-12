import { useMemo, useState } from "react"
import styles from "./Finances.module.css"
import {
  Plus,
  Search,
  X,
  ChevronDown,
  ArrowUp,
  ArrowDown,
} from "lucide-react"

interface Movement {
  id: number
  type: "Ingreso" | "Egreso"
  title: string
  category: string
  amount: number
  date: string
}

function Finances() {
  const [showModal, setShowModal] = useState(false)

  const [search, setSearch] = useState("")

  const [movements, setMovements] = useState<Movement[]>([
    {
      id: 1,
      type: "Ingreso",
      title: "Salario",
      category: "Trabajo",
      amount: 1500,
      date: "20/Feb",
    },
    {
      id: 2,
      type: "Egreso",
      title: "Supermercado",
      category: "Alimentación",
      amount: 180,
      date: "21/Feb",
    },
  ])

  const [formData, setFormData] = useState({
    type: "Ingreso",
    title: "",
    category: "",
    amount: "",
    date: "",
    description: "",
  })

  const filteredMovements = movements.filter((movement) =>
    movement.title.toLowerCase().includes(search.toLowerCase())
  )

  const totalIncome = useMemo(() => {
    return movements
      .filter((m) => m.type === "Ingreso")
      .reduce((acc, curr) => acc + curr.amount, 0)
  }, [movements])

  const totalExpenses = useMemo(() => {
    return movements
      .filter((m) => m.type === "Egreso")
      .reduce((acc, curr) => acc + curr.amount, 0)
  }, [movements])

  const balance = totalIncome - totalExpenses

  const handleAddMovement = () => {
    if (
      !formData.title ||
      !formData.category ||
      !formData.amount ||
      !formData.date
    ) {
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

    setFormData({
      type: "Ingreso",
      title: "",
      category: "",
      amount: "",
      date: "",
      description: "",
    })

    setShowModal(false)
  }

  return (
    <section className={styles.content}>
      <h2 className={styles.title}>Finanzas</h2>

      {/* RESUMEN */}
      <div className={styles.summaryGrid}>
        <div className={styles.summaryCard}>
          <div className={styles.summaryItem}>
            <ArrowUp className={styles.incomeIcon} size={42} />

            <div>
              <p>TOTAL INGRESOS</p>
              <h3 className={styles.incomeText}>
                ${totalIncome.toFixed(2)}
              </h3>
            </div>
          </div>

          <div className={styles.divider}></div>

          <div className={styles.summaryItem}>
            <ArrowDown className={styles.expenseIcon} size={42} />

            <div>
              <p>TOTAL EGRESOS</p>
              <h3 className={styles.expenseText}>
                ${totalExpenses.toFixed(2)}
              </h3>
            </div>
          </div>
        </div>

        <div className={styles.balanceCard}>
          <p>SALDO ACTUAL</p>
          <h3>${balance.toFixed(2)}</h3>
        </div>
      </div>

      {/* SEARCH */}
      <div className={styles.actionsRow}>
        <div className={styles.searchBox}>
          <Search size={24} />

          <input
            type="text"
            placeholder="Buscar movimiento..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <X size={20} />
        </div>

        <button
          className={styles.newButton}
          onClick={() => setShowModal(true)}
        >
          <Plus size={20} />
          Nuevo Movimiento
        </button>
      </div>

      {/* MOVIMIENTOS */}
      <div className={styles.movements}>
        {filteredMovements.map((movement) => (
          <div key={movement.id} className={styles.movementCard}>
            <div className={styles.leftMovement}>
              <div
                className={
                  movement.type === "Ingreso"
                    ? styles.incomeBadge
                    : styles.expenseBadge
                }
              >
                {movement.type === "Ingreso" ? (
                  <ArrowUp size={28} />
                ) : (
                  <ArrowDown size={28} />
                )}
              </div>

              <div>
                <h3>{movement.title}</h3>
                <p>{movement.category}</p>
              </div>
            </div>

            <div className={styles.rightMovement}>
              <h3
                className={
                  movement.type === "Ingreso"
                    ? styles.positive
                    : styles.negative
                }
              >
                {movement.type === "Ingreso" ? "+" : "-"} $
                {movement.amount.toFixed(2)}
              </h3>

              <p>{movement.date}</p>
            </div>
          </div>
        ))}
      </div>

      {/* MODAL */}
      {showModal && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2>Nuevo Movimiento</h2>

            <div className={styles.formGroup}>
              <label>Tipo</label>

              <select
                value={formData.type}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    type: e.target.value,
                  })
                }
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
                value={formData.title}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    title: e.target.value,
                  })
                }
              />
            </div>

            <div className={styles.formGroup}>
              <label>Categoría</label>

              <input
                type="text"
                value={formData.category}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    category: e.target.value,
                  })
                }
              />
            </div>

            <div className={styles.formGroup}>
              <label>Monto</label>

              <input
                type="number"
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
              <label>Fecha</label>

              <input
                type="text"
                placeholder="22/Feb"
                value={formData.date}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    date: e.target.value,
                  })
                }
              />
            </div>

            <div className={styles.formGroup}>
              <label>Descripción</label>

              <textarea
                value={formData.description}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    description: e.target.value,
                  })
                }
              />
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
                onClick={handleAddMovement}
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

export default Finances