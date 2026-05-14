// src/pages/Profile/Profile.tsx
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import styles from './Profile.module.css';

export default function Profile() {
  const { user, isAuthenticated } = useAuth();
  const [editing, setEditing] = useState(false);
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

    try {
      const res = await fetch(`/api/account/${user.accountId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          names: formData.names,
          lastnames: formData.lastnames,
          phone: formData.phone
        })
      });

      if (res.ok) {
        const updatedUser = {
          ...user,
          names: formData.names,
          lastnames: formData.lastnames,
          phone: formData.phone
        };
        sessionStorage.setItem('user', JSON.stringify(updatedUser));
        alert('Perfil actualizado correctamente');
        window.location.reload();
      } else {
        alert('Error al actualizar');
      }
    } catch (error) {
      console.error(error);
      alert('Error de red');
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

      <div className={styles.profileGrid}>

        {/* ── Columna izquierda: avatar + info personal ── */}
        <div className={styles.card}>

          {/* Avatar */}
          <div className={styles.avatarWrapper}>
            <div className={styles.avatar}>
              {user.names?.charAt(0).toUpperCase()}
              <span className={styles.avatarEditBtn}>✏</span>
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
                <div className={styles.infoIconWrap}>👤</div>
                <div>
                  <div className={styles.infoLabel}>Nombre completo</div>
                  <div className={styles.infoValue}>{user.names} {user.lastnames}</div>
                </div>
              </div>

              <div className={styles.infoRow}>
                <div className={styles.infoIconWrap}>✉️</div>
                <div>
                  <div className={styles.infoLabel}>Correo electrónico</div>
                  <div className={styles.infoValue}>{user.email}</div>
                </div>
              </div>

              <div className={styles.infoRow}>
                <div className={styles.infoIconWrap}>📞</div>
                <div>
                  <div className={styles.infoLabel}>Teléfono</div>
                  <div className={styles.infoValue}>{user.phone || 'No registrado'}</div>
                </div>
              </div>

              <button className={styles.editButton} onClick={() => setEditing(true)}>
                ✏ Editar perfil
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
                <button type="submit" className={styles.saveBtn}>Guardar</button>
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
                <div className={styles.accountIconWrap}>🔒</div>
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