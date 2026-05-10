/**
 * @file Register.tsx
 * @description Modal de registro de usuario.
 * Se renderiza sobre la landing page con un overlay oscuro y borroso.
 * Permite crear una nueva cuenta con validaciones en tiempo real.
 * Los campos se organizan en grid de dos columnas para mejor aprovechamiento
 * del espacio en el modal.
 * Campos: nombres, apellidos, teléfono, correo, contraseña, confirmar contraseña.
 */

import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import styles from './Register.module.css'
import { useAuth } from '../../context/AuthContext'
import { register } from '../../services/auth.service'
import { useNavigate } from 'react-router-dom'

/** Estructura de los campos del formulario de registro */
interface RegisterForm {
  firstName: string
  lastName: string
  phone: string
  email: string
  password: string
  confirmPassword: string
  acceptTerms: boolean
}

/**
 * Errores de validación del formulario de registro.
 * Cada campo es opcional - solo se define si hay error en ese campo.
 */

interface RegisterErrors {
  firstName?: string
  lastName?: string
  phone?: string
  email?: string
  password?: string
  confirmPassword?: string
  acceptTerms?: string
}

/**
 * Props del componente Register.
 * Al ser un modal, necesita callbacks para cerrarse y para
 * navegar al login sin cambiar de página.
 */

interface RegisterProps {
  /** Cierra el modal de registro */
  onClose: () => void
  /** Cierra el modal de registro y abre el de login */
  onGoToLogin: () => void
}

/**
 * Componente modal de registro de usuario.
 *
 * @param {RegisterProps} props - Callbacks para cerrar el modal y cambiar al login
 * @returns Modal con formulario de registro en grid de dos columnas
 *
 * @example
 * <Register
 *   onClose={() => setModal(null)}
 *   onGoToLogin={() => setModal('login')}
 * />
 */

function Register({ onClose, onGoToLogin }: RegisterProps) {

  const navigate = useNavigate()
  const { login } = useAuth()

  const [form, setForm] = useState<RegisterForm>({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false,
  })

  const [errors, setErrors] = useState<RegisterErrors>({})
  const [successMsg, setSuccessMsg] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  /**
   * Maneja los cambios en los campos del formulario.
   * - Bloquea números en nombre y apellidos.
   * - Restringe el teléfono a números con máximo 10 dígitos.
   * - Limpia el error del campo en cuanto el usuario empieza a corregirlo.
   *
   * @param {React.ChangeEvent<HTMLInputElement>} e - Evento de cambio del input
   */

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target

    if (name === 'firstName' && /\d/.test(value)) return
    if (name === 'lastName' && /\d/.test(value)) return
    if (name === 'phone') {
      if (/\D/.test(value)) return
      if (value.length > 10) return
    }

    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
    if (errors[name as keyof RegisterErrors]) {
      setErrors({ ...errors, [name]: undefined })
    }
  }

  /**
   * Valida todos los campos del formulario antes de enviar.
   *
   * @returns {RegisterErrors} Objeto con los mensajes de error encontrados por campo
   */

  const validate = (): RegisterErrors => {
    const newErrors: RegisterErrors = {}

    if (!form.firstName.trim())
      newErrors.firstName = 'El nombre es obligatorio.'
    if (!form.lastName.trim())
      newErrors.lastName = 'El apellido es obligatorio.'
    if (!form.phone.trim())
      newErrors.phone = 'El teléfono es obligatorio.'
    else if (form.phone.length !== 10)
      newErrors.phone = 'El teléfono debe tener exactamente 10 dígitos.'
    if (!form.email.trim())
      newErrors.email = 'El correo es obligatorio.'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      newErrors.email = 'Ingresa un correo válido.'
    if (!form.password)
      newErrors.password = 'La contraseña es obligatoria.'
    else if (form.password.length < 8)
      newErrors.password = 'Mínimo 8 caracteres.'
    else if (!/[A-Z]/.test(form.password))
      newErrors.password = 'Debe tener al menos una mayúscula.'
    else if (!/[a-z]/.test(form.password))
      newErrors.password = 'Debe tener al menos una minúscula.'
    else if (!/[0-9]/.test(form.password))
      newErrors.password = 'Debe tener al menos un número.'
    if (!form.confirmPassword)
      newErrors.confirmPassword = 'Confirma tu contraseña.'
    else if (form.password !== form.confirmPassword)
      newErrors.confirmPassword = 'Las contraseñas no coinciden.'
    if (!form.acceptTerms)
      newErrors.acceptTerms = 'Debes aceptar los términos y condiciones.'

    return newErrors
  }

  /**
   * Maneja el envío del formulario de registro.
   * Si hay errores de validación los muestra y detiene el proceso.
   * Si el registro es exitoso, guarda la sesión, muestra mensaje de éxito
   * y redirige al dashboard después de 2 segundos.
   */
  
  const handleSubmit = async () => {
    const newErrors = validate()
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    try {
      const response = await register({
        names: form.firstName,
        lastnames: form.lastName,
        phone: `+57${form.phone}`,
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

      setSuccessMsg('¡Cuenta creada exitosamente! Redirigiendo...')
      setTimeout(() => {
        onClose()
        navigate('/dashboard')
      }, 2000)

    } catch (error: unknown) {
      if (error instanceof Error) {
        setSuccessMsg('')
        setErrors({ email: error.message })
      }
    }
  }

  return (
    <div
      className={styles.overlay}
      onClick={(e) => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className={styles.modal}>

        <button className={styles.closeBtn} onClick={onClose} aria-label="Cerrar">✕</button>

        <h2 className={styles.title}>Crea tu cuenta</h2>
        <p className={styles.subtitle}>Empieza a organizar tu vida hoy</p>

        {successMsg && <p className={styles.successMsg}>{successMsg}</p>}

        {/* Fila: Nombres y Apellidos */}
        <div className={styles.twoCol}>
          <div className={styles.formGroup}>
            <label htmlFor="firstName">Nombres</label>
            <input
              type="text"
              id="firstName"
              name="firstName"
              placeholder="Ingresa tu nombre"
              value={form.firstName}
              onChange={handleChange}
              className={errors.firstName ? styles.inputError : ''}
            />
            {errors.firstName && <span className={styles.errorMsg}>{errors.firstName}</span>}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="lastName">Apellidos</label>
            <input
              type="text"
              id="lastName"
              name="lastName"
              placeholder="Ingresa tu apellido"
              value={form.lastName}
              onChange={handleChange}
              className={errors.lastName ? styles.inputError : ''}
            />
            {errors.lastName && <span className={styles.errorMsg}>{errors.lastName}</span>}
          </div>
        </div>

        {/* Fila: Teléfono y Correo */}
        <div className={styles.twoCol}>
          <div className={styles.formGroup}>
            <label htmlFor="phone">Teléfono</label>
            <div className={styles.phoneWrapper}>
              <span className={styles.phonePrefix}>+57</span>
              <input
                type="text"
                id="phone"
                name="phone"
                placeholder="3001234567"
                value={form.phone}
                onChange={handleChange}
                className={errors.phone ? styles.inputError : ''}
                maxLength={10}
              />
            </div>
            {errors.phone && <span className={styles.errorMsg}>{errors.phone}</span>}
          </div>

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
        </div>

        {/* Fila: Contraseña y Confirmar */}
        <div className={styles.twoCol}>
          <div className={styles.formGroup}>
            <label htmlFor="password">Contraseña</label>
            <div className={styles.passwordWrapper}>
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                placeholder="********"
                value={form.password}
                onChange={handleChange}
                className={errors.password ? styles.inputError : ''}
              />
              <span className={styles.eyeIcon} onClick={() => setShowPassword(p => !p)}>
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </span>
            </div>
            {errors.password && <span className={styles.errorMsg}>{errors.password}</span>}
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="confirmPassword">Confirmar contraseña</label>
            <div className={styles.passwordWrapper}>
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                id="confirmPassword"
                name="confirmPassword"
                placeholder="********"
                value={form.confirmPassword}
                onChange={handleChange}
                className={errors.confirmPassword ? styles.inputError : ''}
                onKeyDown={(e) => { if (e.key === 'Enter') handleSubmit() }}
              />
              <span className={styles.eyeIcon} onClick={() => setShowConfirmPassword(p => !p)}>
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </span>
            </div>
            {errors.confirmPassword && <span className={styles.errorMsg}>{errors.confirmPassword}</span>}
          </div>
        </div>

        {/* Términos */}
        <div className={styles.termsGroup}>
          <input
            type="checkbox"
            id="acceptTerms"
            name="acceptTerms"
            checked={form.acceptTerms}
            onChange={handleChange}
          />
          <label htmlFor="acceptTerms">
            Acepto los{' '}
            <span className={styles.termsLink}>Términos y Condiciones</span>
            {' '}y la{' '}
            <span className={styles.termsLink}>Política de Privacidad</span>
          </label>
        </div>
        {errors.acceptTerms && <span className={styles.errorMsg}>{errors.acceptTerms}</span>}

        <button className={styles.btnPrimary} onClick={handleSubmit}>
          Crear Cuenta
        </button>

        <p className={styles.switchLink}>
          ¿Ya tienes cuenta?{' '}
          <span className={styles.linkBtn} onClick={onGoToLogin}>
            Iniciar Sesión
          </span>
        </p>

      </div>
    </div>
  )
}

export default Register