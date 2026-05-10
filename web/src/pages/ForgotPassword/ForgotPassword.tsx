/**
 * Página de recuperación de contraseña.
 * El usuario ingresa su correo y recibe instrucciones por email.
 * Por seguridad, el mensaje de éxito es genérico - no revela si el correo está
 * o no registrado en el sistema.
 */

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { LockKeyhole } from 'lucide-react'
import styles from './ForgotPassword.module.css'

interface ForgotPasswordErrors {
  email?: string
}

function ForgotPassword() {

  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [errors, setErrors] = useState<ForgotPasswordErrors>({})
  const [successMsg, setSuccessMsg] = useState('')

  const validate = (): ForgotPasswordErrors => {
    const newErrors: ForgotPasswordErrors = {}
    if (!email.trim())
      newErrors.email = 'El correo es obligatorio.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      newErrors.email = 'Ingresa un correo válido.'
    return newErrors
  }

  const handleSubmit = () => {
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    setErrors({})
    // TODO: conectar con el backend - POST /api/auth/forgot-password
    setSuccessMsg('Si el correo está registrado, recibirás instrucciones en tu bandeja de entrada.')
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>

        <div className={styles.iconWrapper}>
          <LockKeyhole size={32} strokeWidth={1.5} />
        </div>

        <h2 className={styles.title}>Recuperar contraseña</h2>

        <p className={styles.subtitle}>
          Ingresa tu correo y te enviaremos instrucciones
          para restablecer tu contraseña
        </p>

        {successMsg && (
          <p className={styles.successMsg}>{successMsg}</p>
        )}

        {!successMsg && (
          <>
            <div className={styles.formGroup}>
              <label htmlFor="email">Correo electrónico</label>
              <input
                type="text"
                id="email"
                name="email"
                placeholder="tucorreo@ejemplo.com"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value)
                  if (errors.email) setErrors({})
                }}
                className={errors.email ? styles.inputError : ''}
                onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit() }}
              />
              {errors.email && (
                <span className={styles.errorMsg}>{errors.email}</span>
              )}
            </div>

            <button className={styles.btnPrimary} onClick={handleSubmit}>
              Enviar
            </button>
          </>
        )}

        <p className={styles.backToLogin} onClick={() => navigate('/', { state: { openLogin: true } })}>
          Volver al inicio de sesión
        </p>

      </div>
    </div>
  )
}

export default ForgotPassword