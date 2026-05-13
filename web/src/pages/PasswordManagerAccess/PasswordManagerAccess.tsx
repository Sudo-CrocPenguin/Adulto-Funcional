import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, Eye, EyeOff } from 'lucide-react';
import styles from './PasswordManagerAccess.module.css';
import { accountService } from '../../services/account.service';

type View = 'login' | 'create';

/** Retorna los requisitos que le faltan a la contraseña */
function getMissingRequirements(key: string): string[] {
  const missing: string[] = [];
  if (key.length < 8)              missing.push('Mínimo 8 caracteres');
  if (!/[A-Z]/.test(key))          missing.push('Al menos una mayúscula');
  if (!/[a-z]/.test(key))          missing.push('Al menos una minúscula');
  if (!/[^A-Za-z0-9]/.test(key))   missing.push('Al menos un caracter especial');
  return missing;
}

export default function PasswordManagerAccess() {
  const navigate = useNavigate();

  const [view, setView]               = useState<View>('login');
  const [masterKey, setMasterKey]     = useState('');
  const [newKey, setNewKey]           = useState('');
  const [confirmKey, setConfirmKey]   = useState('');
  const [showMaster, setShowMaster]   = useState(false);
  const [showNew, setShowNew]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loginError, setLoginError]           = useState('');
  const [keyErrors, setKeyErrors]             = useState<string[]>([]);
  const [confirmError, setConfirmError]       = useState('');
  const [loading, setLoading]                 = useState(false);

  useEffect(() => {
    if (sessionStorage.getItem('masterKeyVerified') === 'true') {
      navigate('/password-manager/home', { replace: true });
      return;
    }
    const accountId = sessionStorage.getItem('accountId');
    if (accountId) {
      accountService.getById(accountId)
        .then(acc => { if (!acc.hasMasterKey) setView('create'); })
        .catch(console.error);
    }
  }, [navigate]);

  const handleLogin = async () => {
    if (!masterKey) { setLoginError('Ingresa tu contraseña maestra'); return; }
    setLoading(true);
    setLoginError('');
    try {
      /*TODO: BACKEND - POST /api/security/master-key/verify { masterKey } */
      sessionStorage.setItem('masterKeyVerified', 'true');
      navigate('/password-manager/home');
    } catch {
      setLoginError('Contraseña maestra incorrecta');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setKeyErrors([]);
    setConfirmError('');

    const missing = getMissingRequirements(newKey);
    if (missing.length > 0) { setKeyErrors(missing); return; }
    if (newKey !== confirmKey) { setConfirmError('Las contraseñas no coinciden'); return; }

    setLoading(true);
    try {
      /*TODO: BACKEND - PATCH /api/account/{id} { masterKey: newKey } */
      sessionStorage.setItem('masterKeyVerified', 'true');
      navigate('/password-manager/home');
    } catch {
      setKeyErrors(['Error al crear la contraseña maestra']);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Gestor de Contraseñas</h1>

      {view === 'login' ? (

        <div className={styles.card}>
          <div className={styles.lockWrapper}>
            <Lock size={72} className={styles.lockIcon} />
          </div>

          <p className={styles.subtitle}>Ingresa la contraseña maestra</p>

          <div className={styles.inputWrapper}>
            <input
              type={showMaster ? 'text' : 'password'}
              className={`${styles.input} ${loginError ? styles.inputError : ''}`}
              value={masterKey}
              onChange={e => { setMasterKey(e.target.value); setLoginError(''); }}
              onKeyDown={e => e.key === 'Enter' && handleLogin()}
            />
            <button className={styles.eyeBtn} onClick={() => setShowMaster(p => !p)}>
              {showMaster ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {loginError && <p className={styles.errorText}>{loginError}</p>}

          <button className={styles.btn} onClick={handleLogin} disabled={loading}>
            {loading ? 'Verificando...' : 'Ingresar'}
          </button>

          <button className={styles.link} onClick={() => navigate('/password-manager/reset')}>
            ¿Necesitas ayuda?
          </button>

          <p className={styles.createPrompt}>
            ¿No tienes contraseña maestra?{' '}
            <button className={styles.link} onClick={() => setView('create')}>Crear una</button>
          </p>
        </div>

      ) : (

        <div className={styles.card}>
          <div className={styles.cardTitleRow}>
            <Lock size={28} className={styles.lockIconSmall} />
            <h2 className={styles.cardTitle}>Crear Contraseña Maestra</h2>
          </div>

          <label className={styles.label}>Contraseña Maestra</label>
          <div className={styles.inputWrapper}>
            <input
              type={showNew ? 'text' : 'password'}
              className={`${styles.input} ${keyErrors.length > 0 ? styles.inputError : ''}`}
              value={newKey}
              onChange={e => { setNewKey(e.target.value); setKeyErrors([]); }}
            />
            <button className={styles.eyeBtn} onClick={() => setShowNew(p => !p)}>
              {showNew ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {keyErrors.length > 0 && (
            <div className={styles.errorList}>
              <span>La contraseña debe tener:</span>
              {keyErrors.map(e => <span key={e}>• {e}</span>)}
            </div>
          )}

          <label className={styles.label}>Confirmar contraseña maestra</label>
          <div className={styles.inputWrapper}>
            <input
              type={showConfirm ? 'text' : 'password'}
              className={`${styles.input} ${confirmError ? styles.inputError : ''}`}
              value={confirmKey}
              onChange={e => { setConfirmKey(e.target.value); setConfirmError(''); }}
            />
            <button className={styles.eyeBtn} onClick={() => setShowConfirm(p => !p)}>
              {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {confirmError && <p className={styles.errorText}>{confirmError}</p>}

          <p className={styles.noticeText}>
            Recibirás una notificación de actualización de contraseña maestra cada dos meses.
          </p>

          <button className={styles.btn} onClick={handleCreate} disabled={loading}>
            {loading ? 'Creando...' : 'Crear'}
          </button>
        </div>

      )}
    </div>
  );
}