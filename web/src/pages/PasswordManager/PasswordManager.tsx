import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Eye, EyeOff, Trash2, Pencil } from 'lucide-react';
import styles from './PasswordManager.module.css';
import { passwordService, type PasswordCredential } from '../../services/password.service';

/* ── Fuerza de contraseña ── */
type Strength = 'weak' | 'medium' | 'strong';

interface StrengthResult {
  label: string;
  level: Strength;
  score: number; /* 0-4 para los 4 segmentos */
}

function getStrength(pwd: string): StrengthResult {
  let score = 0;
  if (pwd.length >= 6)             score++;
  if (/[A-Z]/.test(pwd))           score++;
  if (/[a-z]/.test(pwd))           score++;
  if (/[0-9]/.test(pwd))           score++;
  if (/[^A-Za-z0-9]/.test(pwd))    score++;

  /* Normaliza a 4 segmentos */
  const segments = Math.min(Math.ceil((score / 5) * 4), 4);

  if (score <= 1) return { label: 'Débil',  level: 'weak',   score: Math.max(segments, pwd.length > 0 ? 1 : 0) };
  if (score <= 3) return { label: 'Media',  level: 'medium', score: segments };
  return              { label: 'Fuerte', level: 'strong', score: 4 };
}

/* ── Medidor de fuerza reutilizable ── */
function StrengthMeter({ pwd }: { pwd: string }) {
  if (!pwd) return null;
  const { label, level, score } = getStrength(pwd);
  return (
    <div className={styles.strengthMeter}>
      <div className={styles.strengthBarRow}>
        {[1, 2, 3, 4].map(i => (
          <div
            key={i}
            className={`${styles.strengthSegment} ${i <= score ? `${styles.filled} ${styles[level]}` : ''}`}
          />
        ))}
      </div>
      <div className={styles.strengthInfo}>
        <span className={styles.strengthLabel}>Fuerza</span>
        <span className={`${styles.strengthValue} ${styles[level]}`}>{label}</span>
      </div>
    </div>
  );
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('es-ES', {
    day: 'numeric', month: 'long', year: 'numeric',
  });
}

export default function PasswordManager() {
  const navigate = useNavigate();

  const [passwords, setPasswords]   = useState<PasswordCredential[]>([]);
  const [loading, setLoading]       = useState(true);
  const [showModal, setShowModal]   = useState(false);
  const [visibleIds, setVisibleIds] = useState<Set<string>>(new Set());

  /* Modal nueva / editar contraseña */
  const [editingId, setEditingId]     = useState<string | null>(null);
  const [newName, setNewName]         = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [showNewPwd, setShowNewPwd]   = useState(false);
  const [saving, setSaving]           = useState(false);
  const [modalError, setModalError]   = useState('');

  /* Modal confirmación eliminación */
  const [confirmDelete, setConfirmDelete] = useState<PasswordCredential | null>(null);

  const VAULT_TIMEOUT = 5 * 60 * 1000;
  const inactivityTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const lockVault = useCallback(() => {
    sessionStorage.removeItem('masterKeyVerified');
    navigate('/password-manager', { replace: true});
  }, [navigate]);

  const resetTimer = useCallback(() => {
    if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
    inactivityTimer.current = setTimeout(lockVault, VAULT_TIMEOUT);
  }, [lockVault]);

  /* Verificar sesión de clave maestra */
  useEffect(() => {
    if (sessionStorage.getItem('masterKeyVerified') !== 'true') {
      navigate('/password-manager', { replace: true });
      return;
    }
    resetTimer();
    const events = ['mousemove', 'keydown', 'click', 'scroll'] as const;
    events.forEach(e => window.addEventListener(e, resetTimer));

    loadPasswords();

    return () => {
      sessionStorage.removeItem('masterKeyVerified');

      if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
      events.forEach( e => window.removeEventListener(e, resetTimer));
    };
  }, [navigate, resetTimer]);

  const loadPasswords = async () => {
    try {
      const data = await passwordService.getAll();
      setPasswords(data);
    } catch (err) {
      console.error('Error cargando contraseñas:', err);
    } finally {
      setLoading(false);
    }
  };

  const toggleVisible = (id: string) => {
    setVisibleIds(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  /* ── Abrir modal para nueva contraseña ── */
  const openCreate = () => {
    setEditingId(null);
    setNewName('');
    setNewPassword('');
    setShowNewPwd(false);
    setModalError('');
    setShowModal(true);
  };

  /* ── Abrir modal para editar ── */
  const openEdit = (p: PasswordCredential) => {
    setEditingId(p.id);
    setNewName(p.name);
    setNewPassword(p.password);
    setShowNewPwd(false);
    setModalError('');
    setShowModal(true);
  };

  const handleSave = async () => {
    if (!newName.trim()) { setModalError('El nombre es obligatorio'); return; }
    if (!newPassword)    { setModalError('La contraseña es obligatoria'); return; }
    setSaving(true);
    setModalError('');
    try {
      if (editingId) {
        /* TODO: BACKEND - descomentar cuando el endpoint esté listo */
        // const updated = await passwordService.update(editingId, { name: newName.trim(), password: newPassword });
        // setPasswords(prev => prev.map(p => p.id === editingId ? updated : p));
        setPasswords(prev => prev.map(p =>
          p.id === editingId
            ? { ...p, name: newName.trim(), password: newPassword }
            : p
        ));
      } else {
        /* TODO: BACKEND - descomentar cuando el endpoint esté listo */
        // const created = await passwordService.create({ name: newName.trim(), password: newPassword });
        // setPasswords(prev => [created, ...prev]);
        const mock: PasswordCredential = {
          id: crypto.randomUUID(),
          name: newName.trim(),
          password: newPassword,
          createdAt: new Date().toISOString(),
        };
        setPasswords(prev => [mock, ...prev]);
      }
      closeModal();
    } catch (err) {
      console.error('Error guardando contraseña:', err);
      setModalError('Error al guardar. Inténtalo de nuevo.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!confirmDelete) return;
    try {
      await passwordService.remove(confirmDelete.id);
      setPasswords(prev => prev.filter(p => p.id !== confirmDelete.id));
    } catch (err) {
      console.error('Error eliminando contraseña:', err);
    } finally {
      setConfirmDelete(null);
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingId(null);
    setNewName('');
    setNewPassword('');
    setShowNewPwd(false);
    setModalError('');
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Gestor de Contraseñas</h1>

      <button className={styles.addBtn} onClick={openCreate}>
        <Plus size={16} />
        Añadir contraseña
      </button>

      {/* ── Grid de tarjetas ── */}
      {loading ? (
        <p className={styles.emptyMsg}>Cargando...</p>
      ) : passwords.length === 0 ? (
        <p className={styles.emptyMsg}>No tienes contraseñas guardadas aún.</p>
      ) : (
        <div className={styles.grid}>
          {passwords.map(p => {
            const visible = visibleIds.has(p.id);
            return (
              <div key={p.id} className={styles.card}>
                <div className={styles.cardTop}>
                  <span className={styles.cardName}>{p.name}</span>
                  <div className={styles.cardActions}>
                    <button
                      className={styles.editBtn}
                      onClick={() => openEdit(p)}
                      title="Editar"
                    >
                      <Pencil size={15} />
                    </button>
                    <button
                      className={styles.deleteBtn}
                      onClick={() => setConfirmDelete(p)}
                      title="Eliminar"
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>

                <StrengthMeter pwd={p.password} />

                <div className={styles.passwordRow}>
                  <span className={styles.passwordText}>
                    {visible ? p.password : '•'.repeat(Math.min(p.password.length, 8))}
                  </span>
                  <button className={styles.eyeBtn} onClick={() => toggleVisible(p.id)}>
                    {visible ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>

                <span className={styles.date}>{formatDate(p.createdAt)}</span>
              </div>
            );
          })}
        </div>
      )}

      {/* ── Modal nueva / editar contraseña ── */}
      {showModal && (
        <div className={styles.overlay} onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className={styles.modal}>
            <h3 className={styles.modalTitle}>
              {editingId ? 'Editar Contraseña' : 'Nueva Contraseña'}
            </h3>

            <label className={styles.modalLabel}>Nombre</label>
            <input
              type="text"
              className={`${styles.modalInput} ${modalError === 'El nombre es obligatorio' ? styles.modalInputError : ''}`}
              value={newName}
              onChange={e => { setNewName(e.target.value); setModalError(''); }}
              placeholder="Ej: Netflix"
            />
            {modalError === 'El nombre es obligatorio' && (
              <p className={styles.errorText}>{modalError}</p>
            )}

            <label className={styles.modalLabel}>Contraseña</label>
            <div className={styles.inputWrapper}>
              <input
                type={showNewPwd ? 'text' : 'password'}
                className={`${styles.modalInput} ${modalError === 'La contraseña es obligatoria' ? styles.modalInputError : ''}`}
                value={newPassword}
                onChange={e => { setNewPassword(e.target.value); setModalError(''); }}
                placeholder="Ingresa la contraseña"
              />
              <button className={styles.eyeInline} onClick={() => setShowNewPwd(p => !p)}>
                {showNewPwd ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            {modalError === 'La contraseña es obligatoria' && (
              <p className={styles.errorText}>{modalError}</p>
            )}

            <StrengthMeter pwd={newPassword} />

            {modalError === 'Error al guardar. Inténtalo de nuevo.' && (
              <p className={styles.errorText}>{modalError}</p>
            )}

            <div className={styles.modalActions}>
              <button className={styles.cancelBtn} onClick={closeModal}>Cancelar</button>
              <button className={styles.saveBtn} onClick={handleSave} disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Modal confirmación eliminación ── */}
      {confirmDelete && (
        <div className={styles.overlay} onClick={e => e.target === e.currentTarget && setConfirmDelete(null)}>
          <div className={styles.confirmModal}>
            <h3 className={styles.confirmTitle}>¿Eliminar contraseña?</h3>
            <p className={styles.confirmText}>
              Estás a punto de eliminar la contraseña{' '}
              <span className={styles.confirmName}>"{confirmDelete.name}"</span>.
              Esta acción no se puede deshacer.
            </p>
            <div className={styles.confirmActions}>
              <button className={styles.cancelBtn} onClick={() => setConfirmDelete(null)}>
                Cancelar
              </button>
              <button className={styles.confirmDeleteBtn} onClick={handleDelete}>
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}