import { useNavigate } from 'react-router-dom';
import { Lock } from 'lucide-react';
import styles from './PasswordManagerReset.module.css';

export default function PasswordManagerReset() {
  const navigate = useNavigate();

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Gestor de Contrasenas</h1>

      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <Lock size={28} className={styles.lockIcon} />
          <h2 className={styles.cardTitle}>Recuperacion de clave maestra</h2>
          <p className={styles.cardSubtitle}>
            La recuperacion por correo no esta disponible en esta version.
          </p>
        </div>

        <p className={styles.hint}>
          Si la cuenta todavia no tiene clave maestra, puedes crearla al entrar al gestor.
          Si ya existe una clave maestra, por seguridad debes verificarla antes de cambiarla.
        </p>

        <button className={styles.btn} onClick={() => navigate('/password-manager/access')}>
          Volver al acceso
        </button>
      </div>
    </div>
  );
}
