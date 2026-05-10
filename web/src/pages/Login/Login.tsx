/**
 * Página de inicio de sesión.
 * Permite al usuario autenticarse con email y contraseña. 
 * Incluye opción "Recuerdame", toggle de visibilidad de contraseña,
 * y manejo de error de credenciales incorrectas desde el backend.
 */


import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {Eye, EyeOff } from 'lucide-react'
import styles from './Login.module.css'
import { useAuth } from '../../context/AuthContext'
import { login as loginService } from '../../services/auth.service'

interface LoginForm {
  email: string
  password: string
  rememberMe: boolean
}

interface LoginErrors {
  email?: string
  password?: string
}

interface LoginProps {
  onClose: () => void
  onGoToRegister: () => void
}

function Login({ onClose, onGoToRegister }: LoginProps) {

  const navigate = useNavigate()
  const { login } = useAuth()

  const [form, setForm] = useState<LoginForm>({
    email: '',
    password: '',
    rememberMe: false,
  })

  const [errors, setErrors] = useState<LoginErrors>({})
  const [showPassword, setShowPassword] = useState(false)
  const [authError, setAuthError] = useState('')

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
    if (errors[name as keyof LoginErrors]) {
      setErrors({ ...errors, [name]: undefined })
    }
  }

  const validate = (): LoginErrors => {
    const newErrors: LoginErrors = {}
    if (!form.email.trim())
      newErrors.email = 'El correo es obligatorio.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      newErrors.email = 'Ingresa un correo válido.'
    if (!form.password)
      newErrors.password = 'La contraseña es obligatoria.'
    else if (form.password.length < 8)
      newErrors.password = 'Mínimo 8 caracteres.'
    return newErrors
  }

  const handleSubmit = async () => {
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }
    setAuthError('')
    try {
      const response = await loginService({
        email: form.email,
        password: form.password,
      })

      sessionStorage.setItem('token', response.token)
      sessionStorage.setItem('user', JSON.stringify({
        accountId: response.accountId,
        names: response.names,
        lastnames: response.lastnames,
        email: response.email,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      }))

      login(response.token, {
        accountId: response.accountId,
        names: response.names,
        lastnames: response.lastnames,
        email: response.email,
        phone: response.phone,
        hasMasterKey: response.hasMasterKey,
      })

      onClose()
      navigate('/dashboard')

    } catch (err: unknown) {
      if (err instanceof Error) setAuthError(err.message)
    }
  }

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose() }}>
      <div className={styles.modal}>

        <button className={styles.closeBtn} onClick={onClose} aria-label="Cerrar">✕</button>

        <h2 className={styles.title}>Bienvenido de nuevo</h2>
        <p className={styles.subtitle}>Inicia sesión en tu cuenta</p>

        {authError && <p className={styles.authError}>{authError}</p>}

        <div className={styles.formGroup}>
          <label htmlFor="email">Correo electrónico</label>
          <input
            type="text"
            id="email"
            name="email"
            placeholder="tucorreo@ejemplo.com"
            value={form.email}
            onChange={handleChange}
            className={errors.email ? styles.inputError : ''}
          />
          {errors.email && <span className={styles.errorMsg}>{errors.email}</span>}
        </div>

        <div className={styles.formGroup}>
          <div className={styles.labelRow}>
            <label htmlFor="password">Contraseña</label>
            <span
              className={styles.forgotLink}
              onClick={() => { onClose(); navigate('/forgot-password') }}
            >
              ¿Olvidaste tu contraseña?
            </span>
          </div>
          <div className={styles.passwordWrapper}>
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              name="password"
              placeholder="********"
              value={form.password}
              onChange={handleChange}
              className={errors.password ? styles.inputError : ''}
              onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit() }}
            />
            <span className={styles.eyeIcon} onClick={() => setShowPassword(p => !p)}>
              {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
            </span>
          </div>
          {errors.password && <span className={styles.errorMsg}>{errors.password}</span>}
        </div>

        <div className={styles.rememberMe}>
          <input
            type="checkbox"
            id="rememberMe"
            name="rememberMe"
            checked={form.rememberMe}
            onChange={handleChange}
          />
          <label htmlFor="rememberMe">Recuérdame</label>
        </div>

        <button className={styles.btnPrimary} onClick={handleSubmit}>
          Iniciar Sesión
        </button>

        <p className={styles.switchLink}>
          ¿No tienes cuenta?{' '}
          <span className={styles.linkBtn} onClick={onGoToRegister}>
            Registrar
          </span>
        </p>

      </div>
    </div>
  )

}

export default Login