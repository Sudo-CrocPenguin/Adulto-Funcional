// src/pages/Profile/Profile.tsx
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import styles from './Profile.module.css';
import { User, Mail, Phone, Pencil, Lock } from 'lucide-react';
import { accountService } from '../../services/account.service';

export default function Profile() {
  const { user, isAuthenticated, updateUser } = useAuth();
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [formData, setFormData] = useState({
    names: user?.names || '',
    lastnames: user?.lastnames || '',
    phone: user?.phone || ''
  });

  const stats = [
    { label: 'Compromisos completados', value: 47 },
    { label: 'Racha máxima (días)', value: 14 },
    { label: 'Contraseñas guardadas', value: 2 },
    { label: 'Gastos fijos registrados', value: 8 }
  ];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.accountId) return;

    setSaving(true);
    setMessage('');

    try {
      const account = await accountService.update(user.accountId, {
        names: formData.names,
        lastnames: formData.lastnames,
        phone: formData.phone
      });

      updateUser({
        ...user,
        names: account.names,
        lastnames: account.lastnames,
        phone: account.phone
      });
      setEditing(false);
      setMessage('Perfil actualizado correctamente.');
    } catch (error) {
      console.error(error);
      setMessage('No se pudo actualizar el perfil.');
    } finally {
      setSaving(false);
    }
  };

  if (!isAuthenticated) {
    return <div className={styles.notAuthenticated}>Debes iniciar sesión para ver tu perfil</div>;
  }

  if (!user) {
    return <div className={styles.loading}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.profileHeader}>
        <h2>Perfil</h2>
      </div>
      {message && <p className={styles.message}>{message}</p>}

      <div className={styles.profileGrid}>

        {/* ── Columna izquierda: avatar + info personal ── */}
        <div className={styles.card}>

          {/* Avatar */}
          <div className={styles.avatarWrapper}>
            <div className={styles.avatar}>
              {user.names?.charAt(0).toUpperCase()}
              <span className={styles.avatarEditBtn}><Pencil size={16} /></span>
            </div>
            <p className={styles.avatarName}>{user.names} {user.lastnames}</p>
            <p className={styles.avatarEmail}>{user.email}</p>
            <span className={styles.memberBadge}>Miembro desde 2 de febrero, 2026</span>
          </div>

          {/* Info personal */}
          <p className={styles.infoSectionTitle}>Información Personal</p>

          {!editing ? (
            <>
              <div className={styles.infoRow}>
                <div className={styles.infoIconWrap}><User size={18} /></div>
                <div>
                  <div className={styles.infoLabel}>Nombre completo</div>
                  <div className={styles.infoValue}>{user.names} {user.lastnames}</div>
                </div>
              </div>

              <div className={styles.infoRow}>
                <div className={styles.infoIconWrap}><Mail size={18} /></div>
                <div>
                  <div className={styles.infoLabel}>Correo electrónico</div>
                  <div className={styles.infoValue}>{user.email}</div>
                </div>
              </div>

              <div className={styles.infoRow}>
                <div className={styles.infoIconWrap}><Phone size={18} /></div>
                <div>
                  <div className={styles.infoLabel}>Teléfono</div>
                  <div className={styles.infoValue}>{user.phone || 'No registrado'}</div>
                </div>
              </div>

              <button className={styles.editButton} onClick={() => setEditing(true)}>
                <Pencil size={16} />
                Editar perfil
              </button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className={styles.formGroup}>
                <label>Nombres</label>
                <input name="names" value={formData.names} onChange={handleChange} required />
              </div>
              <div className={styles.formGroup}>
                <label>Apellidos</label>
                <input name="lastnames" value={formData.lastnames} onChange={handleChange} required />
              </div>
              <div className={styles.formGroup}>
                <label>Correo electrónico</label>
                <input type="email" value={user.email} disabled />
              </div>
              <div className={styles.formGroup}>
                <label>Teléfono</label>
                <input name="phone" value={formData.phone} onChange={handleChange} />
              </div>
              <div className={styles.formActions}>
                <button type="submit" className={styles.saveBtn} disabled={saving}>
                  {saving ? 'Guardando...' : 'Guardar'}
                </button>
                <button type="button" className={styles.cancelBtn} onClick={() => setEditing(false)}>Cancelar</button>
              </div>
            </form>
          )}
        </div>

        {/* ── Columna derecha: actividad + cuenta ── */}
        <div className={styles.rightColumn}>

          {/* Actividad */}
          <div className={styles.card}>
            <h3>Mi Actividad</h3>
            <div className={styles.statsGrid}>
              {stats.map((stat, idx) => (
                <div key={idx} className={styles.statItem}>
                  <span className={styles.statNumber}>{stat.value}</span>
                  <span className={styles.statLabel}>{stat.label}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Cuenta */}
          <div className={`${styles.card} ${styles.accountSection}`}>
            <h3>Cuenta</h3>
            <div className={styles.accountItem}>
              <button>
                <div className={styles.accountIconWrap}><Lock size={18} /></div>
                Cambiar contraseña
                <span className={styles.accountChevron}>›</span>
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
