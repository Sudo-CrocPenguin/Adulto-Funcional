import { useState, useRef, useEffect, type KeyboardEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, Eye, EyeOff } from 'lucide-react';
import styles from './PasswordManagerReset.module.css';

type Step = 1 | 2 | 3;

interface PassRequirement {
  label: string;
  met: boolean;
}

export default function PasswordManagerReset() {
  const navigate = useNavigate();

  const [step, setStep]               = useState<Step>(1);
  const [email, setEmail]             = useState('');
  const [code, setCode]               = useState<string[]>(Array(6).fill(''));
  const [newKey, setNewKey]           = useState('');
  const [confirmKey, setConfirmKey]   = useState('');
  const [showNew, setShowNew]         = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError]             = useState('');
  const [loading, setLoading]         = useState(false);
  const [resendTimer, setResendTimer] = useState(0);
  const [resetSuccess, setResetSuccess] = useState(false);

  const codeRefs      = useRef<(HTMLInputElement | null)[]>([]);
  const timerInterval = useRef<ReturnType<typeof setInterval> | null>(null);

  /* ── Cuenta regresiva de reenvío ── */
  const startResendTimer = () => {
    setResendTimer(60);
    if (timerInterval.current) clearInterval(timerInterval.current);
    timerInterval.current = setInterval(() => {
      setResendTimer(prev => {
        if (prev <= 1) {
          clearInterval(timerInterval.current!);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  useEffect(() => () => {
    if (timerInterval.current) clearInterval(timerInterval.current);
  }, []);

  /* ── Requisitos de contraseña ── */
  const getPassRequirements = (pass: string): PassRequirement[] => [
    { label: 'Mínimo 8 caracteres',        met: pass.length >= 8 },
    { label: 'Al menos una mayúscula',      met: /[A-Z]/.test(pass) },
    { label: 'Al menos una minúscula',      met: /[a-z]/.test(pass) },
    { label: 'Al menos un carácter especial', met: /[^A-Za-z0-9]/.test(pass) },
  ];

  const passRequirements   = getPassRequirements(newKey);
  const passRequirementsMet = passRequirements.every(r => r.met);
  const unmetRequirements   = passRequirements.filter(r => !r.met);
  const passwordsMismatch   = confirmKey.length > 0 && newKey !== confirmKey;

  /* ── Paso 1: enviar email ── */
  const handleSendEmail = async () => {
    if (!email) { setError('Ingresa tu correo electrónico'); return; }
    setLoading(true);
    setError('');
    try {
      /* TODO: BACKEND - POST /api/auth/forgot-master-key { email } */
      setStep(2);
      startResendTimer();
    } catch {
      setError('Correo no encontrado');
    } finally {
      setLoading(false);
    }
  };

  /* ── Paso 2: verificar código ── */
  const handleVerifyCode = async () => {
    const fullCode = code.join('');
    if (fullCode.length < 6) { setError('Ingresa el código completo'); return; }
    setLoading(true);
    setError('');
    try {
      /* TODO: BACKEND - POST /api/auth/verify-reset-code { email, code: fullCode } */
      setStep(3);
    } catch {
      setError('Código incorrecto o expirado');
    } finally {
      setLoading(false);
    }
  };

  /* ── Paso 3: restablecer contraseña ── */
  const handleReset = async () => {
    if (!passRequirementsMet) return;
    if (newKey !== confirmKey) return;
    setLoading(true);
    try {
      /* TODO: BACKEND - POST /api/auth/reset-master-key { email, code, newMasterKey } */
      setResetSuccess(true);
      setTimeout(() => {
        sessionStorage.setItem('masterKeyVerified', 'true');
        navigate('/password-manager/home');
      }, 3000);
    } catch {
      setError('Error al restablecer la contraseña. Inténtalo de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  /* ── Manejo de inputs del código ── */
  const handleCodeChange = (index: number, value: string) => {
    const digit = value.replace(/\D/g, '').slice(-1);
    const next  = [...code];
    next[index] = digit;
    setCode(next);
    setError('');
    if (digit && index < 5) codeRefs.current[index + 1]?.focus();
  };

  const handleCodeKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !code[index] && index > 0) {
      const next  = [...code];
      next[index - 1] = '';
      setCode(next);
      codeRefs.current[index - 1]?.focus();
    }
  };

  const handleResend = () => {
    /* TODO: BACKEND - reenviar código de verificación */
    setCode(Array(6).fill(''));
    setError('');
    startResendTimer();
    setTimeout(() => codeRefs.current[0]?.focus(), 50);
  };

  /* ── Cabecera compartida ── */
  const CardHeader = ({ large = false, showSubtitle = true }: { large?: boolean; showSubtitle?: boolean }) => (
    <div className={styles.cardHeader}>
      <Lock size={large ? 28 : 24} className={styles.lockIcon} />
      <h2 className={styles.cardTitle}>Restablecer Contraseña Maestra</h2>
      {showSubtitle && <p className={styles.cardSubtitle}>Verifiquemos que eres tú</p>}
    </div>
  );

  return (
    <div className={styles.page}>
      <h1 className={styles.title}>Gestor de Contraseñas</h1>

      {/* ── PASO 1: Correo ── */}
      {step === 1 && (
        <div className={styles.card}>
          <CardHeader />

          <label className={styles.label}>Ingresa tu correo electrónico</label>
          <input
            type="email"
            className={`${styles.input} ${error ? styles.inputError : ''}`}
            value={email}
            onChange={e => { setEmail(e.target.value); setError(''); }}
            onKeyDown={e => e.key === 'Enter' && handleSendEmail()}
            placeholder="correo@ejemplo.com"
          />
          <p className={styles.hint}>Te enviaremos un código de verificación</p>

          {error && <p className={styles.errorText}>{error}</p>}

          <button className={styles.btn} onClick={handleSendEmail} disabled={loading}>
            {loading ? 'Enviando...' : 'Enviar'}
          </button>
        </div>
      )}

      {/* ── PASO 2: Código ── */}
      {step === 2 && (
        <div className={styles.card}>
          <CardHeader large />

          <label className={`${styles.label} ${styles.labelCenter}`}>Ingresa el código</label>
          <div className={styles.codeRow}>
            {code.map((digit, i) => (
              <input
                key={i}
                ref={el => { codeRefs.current[i] = el; }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                className={`${styles.codeBox} ${error ? styles.codeBoxError : ''}`}
                value={digit}
                onChange={e => handleCodeChange(i, e.target.value)}
                onKeyDown={e => handleCodeKeyDown(i, e)}
              />
            ))}
          </div>

          <p className={styles.resendText}>
            ¿No recibiste el código?{' '}
            {resendTimer > 0 ? (
              <span className={styles.resendTimer}>Reenviar en {resendTimer}s</span>
            ) : (
              <button className={styles.link} onClick={handleResend}>Reenviar</button>
            )}
          </p>

          {error && <p className={styles.errorText}>{error}</p>}

          <button className={styles.btn} onClick={handleVerifyCode} disabled={loading}>
            {loading ? 'Verificando...' : 'Verificar'}
          </button>
        </div>
      )}

      {/* ── PASO 3: Nueva contraseña ── */}
      {step === 3 && (
        <div className={styles.card}>
          <CardHeader showSubtitle={false}/>

          <label className={styles.label}>Nueva contraseña maestra</label>
          <div className={styles.inputWrapper}>
            <input
              type={showNew ? 'text' : 'password'}
              className={`${styles.input} ${newKey.length > 0 && !passRequirementsMet ? styles.inputError : ''}`}
              value={newKey}
              onChange={e => setNewKey(e.target.value)}
            />
            <button className={styles.eyeBtn} onClick={() => setShowNew(p => !p)}>
              {showNew ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {/* Requisitos de contraseña dinámicos */}
          {newKey.length > 0 && !passRequirementsMet && (
            <div className={styles.passRequirements}>
              <p className={styles.passRequirementsTitle}>La contraseña debe tener:</p>
              {unmetRequirements.map(req => (
                <p key={req.label} className={styles.passRequirementItem}>
                  · {req.label}
                </p>
              ))}
            </div>
          )}

          <label className={styles.label}>Confirmar contraseña maestra</label>
          <div className={styles.inputWrapper}>
            <input
              type={showConfirm ? 'text' : 'password'}
              className={`${styles.input} ${passwordsMismatch ? styles.inputError : ''}`}
              value={confirmKey}
              onChange={e => setConfirmKey(e.target.value)}
            />
            <button className={styles.eyeBtn} onClick={() => setShowConfirm(p => !p)}>
              {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>

          {passwordsMismatch && (
            <p className={styles.errorText}>Las contraseñas no coinciden</p>
          )}

          {resetSuccess && (
            <p className={styles.successNote}>
              Recibirás una notificación de actualización de contraseña maestra cada dos meses
            </p>
          )}

          <button
            className={styles.btn}
            onClick={handleReset}
            disabled={loading || !passRequirementsMet || passwordsMismatch}
          >
            {loading ? 'Restableciendo...' : 'Restablecer'}
          </button>
        </div>
      )}
    </div>
  );
}