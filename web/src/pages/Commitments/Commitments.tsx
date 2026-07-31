// src/pages/Commitments/Commitments.tsx
import { useState, useMemo, useEffect } from 'react';
import { Plus, X, Trash2, RotateCcw, Flame, StopCircle, CheckCircle2 } from 'lucide-react';
import styles from './Commitments.module.css';
import commitmentsService, {
  type Category,
  type Commitment,
  type Frequency,
  type Priority,
} from '../../services/commitments.service';

type Filter    = 'Todas' | 'Pendientes' | 'Completadas';

const CATEGORIES: Category[] = ['Trabajo', 'Hogar', 'Personal', 'Salud', 'Finanzas', 'Otros'];
const FREQUENCIES: Frequency[] = ['Una vez', 'Diario', 'Semanal', 'Mensual', 'Anual'];
const REMINDERS   = ['7:00 A.M.', '8:00 A.M.', '9:00 A.M.', '12:00 P.M.', '6:00 P.M.', '9:00 P.M.'];

const priorityClass: Record<Priority, string> = {
  Alta:  styles.priorityAlta,
  Media: styles.priorityMedia,
  Baja:  styles.priorityBaja,
};

/* ── fecha helpers ───────────────────────────────────────────────── */
const today = () => new Date().toISOString().split('T')[0];

function addDays(dateStr: string, days: number): string {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}
function addMonths(dateStr: string, months: number): string {
  const d = new Date(dateStr + 'T00:00:00');
  d.setMonth(d.getMonth() + months);
  return d.toISOString().split('T')[0];
}
function nextDateFor(frequency: Frequency, fromDate: string): string {
  if (frequency === 'Diario')   return addDays(fromDate, 1);
  if (frequency === 'Semanal')  return addDays(fromDate, 7);
  if (frequency === 'Mensual')  return addMonths(fromDate, 1);
  if (frequency === 'Anual')    return addMonths(fromDate, 12);
  return fromDate;
}
function formatDate(d: string) {
  if (!d) return '';
  const [y, m, day] = d.split('-');
  const months = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
  return `${day}/${months[parseInt(m) - 1]}/${y}`;
}

/* ── secciones ──────────────────────────────────────────────────── */
interface Sections {
  thisWeek:  Commitment[];
  nextWeek:  Commitment[];
  nextMonth: Commitment[];
  later:     Commitment[];
  done:      Commitment[];   // "Una vez" completadas + recurrentes cesadas
}

function groupBySection(list: Commitment[]): Sections {
  const todayStr = today();
  const todayD   = new Date(todayStr + 'T00:00:00');
  const s: Sections = { thisWeek: [], nextWeek: [], nextMonth: [], later: [], done: [] };

  list.forEach(c => {
    // completados / cesados → sección "Completado"
    if (c.completed || c.ceased) { s.done.push(c); return; }

    const d        = new Date(c.date + 'T00:00:00');
    const diffDays = Math.floor((d.getTime() - todayD.getTime()) / 86400000);

    if (diffDays <= 7)       s.thisWeek.push(c);
    else if (diffDays <= 14) s.nextWeek.push(c);
    else if (diffDays <= 35) s.nextMonth.push(c);
    else                     s.later.push(c);
  });

  return s;
}

/* ── streak helpers ─────────────────────────────────────────────── */
const STREAK_KEY = 'af_streak_count';
const LAST_KEY   = 'af_streak_last_date';

function loadStreak() {
  const count    = parseInt(localStorage.getItem(STREAK_KEY) || '0', 10);
  const lastDate = localStorage.getItem(LAST_KEY) || '';
  return { count, lastDate };
}
function saveStreak(count: number, date: string) {
  localStorage.setItem(STREAK_KEY, String(count));
  localStorage.setItem(LAST_KEY, date);
}

const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) return response.data.message;
  }

  return error instanceof Error ? error.message : fallback;
};

/* ══════════════════════════════════════════════════════════════════
   COMPONENT
══════════════════════════════════════════════════════════════════ */
export default function Commitments() {
  const [commitments, setCommitments] = useState<Commitment[]>([]);
  const [filter,      setFilter]      = useState<Filter>('Todas');
  const [showModal,   setShowModal]   = useState(false);
  const [loading,     setLoading]     = useState(true);
  const [saving,      setSaving]      = useState(false);
  const [error,       setError]       = useState('');
  const [streak,      setStreak]      = useState<number>(() => {
    const { count, lastDate } = loadStreak();
    if (lastDate && lastDate < addDays(today(), -1)) return 0;
    return count;
  });

  const [form, setForm] = useState({
    name: '', category: '' as Category | '', frequency: '' as Frequency | '',
    priority: 'Media' as Priority, date: '', reminder: '7:00 A.M.', description: '',
  });

  useEffect(() => {
    let mounted = true;

    const loadCommitments = async () => {
      setLoading(true);
      setError('');

      try {
        const data = await commitmentsService.getAll();
        if (mounted) setCommitments(data);
      } catch (err) {
        if (mounted) setError(getErrorMessage(err, 'No se pudieron cargar los compromisos.'));
      } finally {
        if (mounted) setLoading(false);
      }
    };

    void loadCommitments();

    return () => {
      mounted = false;
    };
  }, []);

  const replaceCommitment = (updated: Commitment) => {
    setCommitments(prev => prev.map(c => c.id === updated.id ? updated : c));
  };

  /* ── completar ────────────────────────────────────────────────── */
  const toggleComplete = async (id: string) => {
    const todayStr = today();
    const target   = commitments.find(c => c.id === id);
    if (!target) return;

    const isRecurring = target.frequency !== 'Una vez';
    setError('');

    try {
      let updated: Commitment;
      if (isRecurring) {
        updated = await commitmentsService.update(id, {
          date: nextDateFor(target.frequency, target.date),
          completed: false,
          ceased: false,
        });
      } else {
        updated = await commitmentsService.toggleComplete(id, !target.completed);
      }
      replaceCommitment(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'No se pudo actualizar el compromiso.'));
      return;
    }

    // racha: sumar solo 1 vez por día
    const completing = isRecurring || !target.completed;
    if (!completing) return;
    const { count, lastDate } = loadStreak();
    if (lastDate === todayStr) return;
    const newCount = lastDate === addDays(todayStr, -1) ? count + 1 : 1;
    setStreak(newCount);
    saveStreak(newCount, todayStr);
  };

  /* ── cesar (solo recurrentes) ─────────────────────────────────── */
  const ceaseCommitment = async (id: string) => {
    setError('');

    try {
      const updated = await commitmentsService.cease(id);
      replaceCommitment(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'No se pudo cesar el compromiso.'));
    }
  };

  /* ── reactivar (desde "Completado") ──────────────────────────── */
  const reactivate = async (id: string) => {
    setError('');

    try {
      const updated = await commitmentsService.reactivate(id);
      replaceCommitment(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'No se pudo reactivar el compromiso.'));
    }
  };

  /* ── eliminar ─────────────────────────────────────────────────── */
  const deleteCommitment = async (id: string) => {
    setError('');

    try {
      await commitmentsService.delete(id);
      setCommitments(prev => prev.filter(c => c.id !== id));
    } catch (err) {
      setError(getErrorMessage(err, 'No se pudo eliminar el compromiso.'));
    }
  };

  /* ── guardar nuevo ────────────────────────────────────────────── */
  const handleSave = async () => {
    if (!form.name || !form.category || !form.frequency || !form.date) {
      setError('Completa todos los campos obligatorios.');
      return;
    }

    setSaving(true);
    setError('');

    try {
      const created = await commitmentsService.create({
        name: form.name,
        category: form.category as Category,
        frequency: form.frequency as Frequency,
        priority: form.priority,
        date: form.date,
        reminder: form.reminder,
        description: form.description,
      });
      setCommitments(prev => [created, ...prev]);
      setForm({ name: '', category: '', frequency: '', priority: 'Media', date: '', reminder: '7:00 A.M.', description: '' });
      setShowModal(false);
    } catch (err) {
      setError(getErrorMessage(err, 'No se pudo crear el compromiso.'));
    } finally {
      setSaving(false);
    }
  };

  /* ── filtrar y agrupar ────────────────────────────────────────── */
  const filtered = useMemo(() => {
    return commitments.filter(c => {
      const isDone = c.completed || c.ceased;
      if (filter === 'Pendientes')  return !isDone;
      if (filter === 'Completadas') return isDone;
      return true;
    });
  }, [commitments, filter]);

  const sections   = useMemo(() => groupBySection(filtered), [filtered]);
  const streakMax  = Math.max(commitments.filter(c => !c.completed && !c.ceased).length, 1);
  const streakPct  = Math.min(Math.round((streak / streakMax) * 100), 100);

  /* ── card activa ──────────────────────────────────────────────── */
  const renderCard = (c: Commitment) => (
    <div key={c.id} className={styles.commitmentCard}>
      <input
        type="checkbox"
        className={styles.checkbox}
        checked={c.completed}
        onChange={() => toggleComplete(c.id)}
      />
      <div className={styles.cardBody}>
        <h3 className={styles.cardName}>{c.name}</h3>
        <div className={styles.cardMeta}>
          <span className={styles.metaTag}>{c.category}</span>
          <RotateCcw size={12} className={styles.metaIcon} />
          <span className={styles.metaTag}>{c.frequency}</span>
          <span className={priorityClass[c.priority]}>{c.priority}</span>
        </div>
      </div>
      <div className={styles.cardRight}>
        <span className={styles.cardDate}>{formatDate(c.date)}</span>
        <div className={styles.cardActions}>
          {/* botón cesar — solo recurrentes */}
          {c.frequency !== 'Una vez' && (
            <button
              className={styles.ceaseBtn}
              title="Cesar este compromiso"
              onClick={() => ceaseCommitment(c.id)}
            >
              <StopCircle size={15} />
            </button>
          )}
          <button className={styles.deleteBtn} onClick={() => deleteCommitment(c.id)}>
            <Trash2 size={15} />
          </button>
        </div>
      </div>
    </div>
  );

  /* ── card completada / cesada ─────────────────────────────────── */
  const renderDoneCard = (c: Commitment) => (
    <div key={c.id} className={`${styles.commitmentCard} ${styles.doneCard}`}>
      <CheckCircle2 size={18} className={c.ceased ? styles.ceasedIcon : styles.checkIcon} />
      <div className={styles.cardBody}>
        <h3 className={styles.cardName}>{c.name}</h3>
        <div className={styles.cardMeta}>
          <span className={styles.metaTag}>{c.category}</span>
          <RotateCcw size={12} className={styles.metaIcon} />
          <span className={styles.metaTag}>{c.frequency}</span>
          <span className={styles.doneTag}>
            {c.ceased ? 'Cesado' : 'Completado'}
          </span>
        </div>
      </div>
      <div className={styles.cardRight}>
        <button className={styles.reactivateBtn} onClick={() => reactivate(c.id)}>
          Reactivar
        </button>
        <button className={styles.deleteBtn} onClick={() => deleteCommitment(c.id)}>
          <Trash2 size={15} />
        </button>
      </div>
    </div>
  );

  /* ── render sección ───────────────────────────────────────────── */
  const renderSection = (title: string, list: Commitment[], accentClass?: string, done = false) => {
    if (list.length === 0) return null;
    return (
      <div className={styles.section}>
        <h4 className={`${styles.sectionTitle} ${accentClass || ''}`}>{title}</h4>
        <div className={styles.list}>
          {list.map(c => done ? renderDoneCard(c) : renderCard(c))}
        </div>
      </div>
    );
  };

  return (
    <section className={styles.container}>
      <h2 className={styles.title}>Compromisos</h2>
      {error && <p className={styles.empty}>{error}</p>}

      {/* ── Racha ── */}
      <div className={styles.streakCard}>
        <div className={styles.streakLeft}>
          <Flame size={20} className={styles.flameIcon} />
          <div>
            <p className={styles.streakLabel}>Mi Racha</p>
            <p className={styles.streakValue}>
              <span className={styles.streakNumber}>{streak}</span> Días Activos
            </p>
          </div>
        </div>
        <div className={styles.streakRight}>
          <div className={styles.progressBar}>
            <div className={styles.progressFill} style={{ width: `${streakPct}%` }} />
          </div>
          <span className={styles.streakCount}>{streak}</span>
        </div>
      </div>

      {/* ── Filtros + Botón ── */}
      <div className={styles.actionsRow}>
        <div className={styles.filters}>
          {(['Todas', 'Pendientes', 'Completadas'] as Filter[]).map(f => (
            <button
              key={f}
              className={`${styles.filterBtn} ${filter === f ? styles.filterActive : ''}`}
              onClick={() => setFilter(f)}
            >{f}</button>
          ))}
        </div>
        <button className={styles.newButton} onClick={() => setShowModal(true)}>
          <Plus size={16} /> Nuevo Compromiso
        </button>
      </div>

      {/* ── Secciones ── */}
      {loading
        ? <p className={styles.empty}>Cargando compromisos...</p>
        : filtered.length === 0
        ? <p className={styles.empty}>No hay compromisos en esta categoría.</p>
        : <>
            {renderSection('Esta semana',    sections.thisWeek)}
            {renderSection('Próxima semana', sections.nextWeek,  styles.accentNextWeek)}
            {renderSection('Próximo mes',    sections.nextMonth, styles.accentNextMonth)}
            {renderSection('Más adelante',   sections.later,     styles.accentLater)}
            {renderSection('Completado',     sections.done,      styles.accentDone, true)}
          </>
      }

      {/* ── Modal ── */}
      {showModal && (
        <div className={styles.modalOverlay} onClick={() => setShowModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>Nuevo Compromiso</h2>
              <button className={styles.closeBtn} onClick={() => setShowModal(false)}>
                <X size={20} />
              </button>
            </div>

            <div className={styles.modalBody}>
              <div className={styles.formGroup}>
                <label>Nombre</label>
                <input className={styles.input} value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Ej: Pagar renta" />
              </div>
              <div className={styles.formGroup}>
                <label>Categoría</label>
                <select className={styles.input} value={form.category}
                  onChange={e => setForm({ ...form, category: e.target.value as Category })}>
                  <option value="">Seleccionar</option>
                  {CATEGORIES.map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label>Frecuencia</label>
                <select className={styles.input} value={form.frequency}
                  onChange={e => setForm({ ...form, frequency: e.target.value as Frequency })}>
                  <option value="">Seleccionar</option>
                  {FREQUENCIES.map(f => <option key={f}>{f}</option>)}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label>Prioridad</label>
                <div className={styles.radioGroup}>
                  {(['Alta', 'Media', 'Baja'] as Priority[]).map(p => (
                    <label key={p} className={styles.radioLabel}>
                      <input type="radio" name="priority" value={p}
                        checked={form.priority === p}
                        onChange={() => setForm({ ...form, priority: p })} />
                      {p}
                    </label>
                  ))}
                </div>
              </div>
              <div className={styles.formGroup}>
                <label>Fecha</label>
                <input type="date" className={styles.input} value={form.date}
                  onChange={e => setForm({ ...form, date: e.target.value })} />
              </div>
              <div className={styles.formGroupRow}>
                <label>Recordatorio</label>
                <select className={styles.inputSmall} value={form.reminder}
                  onChange={e => setForm({ ...form, reminder: e.target.value })}>
                  {REMINDERS.map(r => <option key={r}>{r}</option>)}
                </select>
              </div>
              <div className={styles.formGroup}>
                <label>Descripción</label>
                <textarea className={styles.textarea} value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                  placeholder="Opcional..." />
              </div>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setShowModal(false)}>Cancelar</button>
              <button className={styles.saveButton} onClick={handleSave} disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
