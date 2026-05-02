/**
 * Página de inicio de sesión.
 * Permite al usuario autenticarse con email y contraseña. 
 * Incluye opción "Recuerdame", toggle de visibilidad de contraseña,
 * y manejo de error de credenciales incorrectas desde el backend.
 */


import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {Eye, EyeOff } from 'lucide-react'
import AuthLayout from '../../components/AuthLayout/AuthLayout'
import styles from './Login.module.css'

interface LoginForm {
  email: string
  password: string
  rememberMe: boolean
}

interface LoginErrors {
  email?: string
  password?: string
}


function Login() {

  const navigate = useNavigate()

  const [form, setForm] = useState<LoginForm>({
    email: '',
    password: '',
    rememberMe: false,
  })

  const [errors, setErrors] = useState<LoginErrors>({})
  const [showPassword, setShowPassword] = useState<boolean>(false)

  //error general de autenticacion (credenciales incorrectas desde el backend )
  const [authError, setAuthError] = useState<string>('')

  /**
   * Maneja los cambios en los campos del formulario.
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type,checked } = e.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })

    //limpia el error del campo al escribir
    if (errors[name as keyof LoginErrors]) {
      setErrors({ ...errors, [name]: undefined })
    }
  }

  /**
   * Valida los campos del formulario antes de enviar.
   * @returns Objeto con los mensajes de error encontrados por campo.
   */
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

  /**
   * Maneja el envío del formulario de login.
   * Si hay errores de validación los muestra y detiene el proceso.
   * Si las credenciales son incorrectas, muestra authError desde el backend.
   */
  const handleSubmit = () => {
    const newErrors = validate()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)

      return
    }

    setAuthError('') //limpia error previo

    //TODO: conectar con el backend - POST /api/auth/login
    //Si el back responde 401:
    //setAuthError('Correo o contraseña incorrectos.')
    //return

    navigate('/dashboard')
  }


  return (
  
    <AuthLayout>

      <h2 className={styles.title}>Iniciar Sesión</h2>

      {/**error de autenticacion del backend */}
      {authError && (
        <p className={styles.authError}>{authError}</p>
      )}

      {/**campo correo electronico */}
      <div className={styles.formGroup}>
        <label htmlFor="email">Correo Electrónico</label>
        <input 
          type="text"
          id="email"
          name="email"
          placeholder='tucorreo@ejemplo.com'
          value={form.email}
          onChange={handleChange}
          className={errors.email ? styles.inputError : ''} 
        
        />

        {errors.email && (
          <span className={styles.errorMsg}>{errors.email}</span>
        )}

      </div>

      {/**campo contrase con boton mostrar/ocultar */}
      <div className={styles.formGroup}>
        <label htmlFor="password">Contraseña</label>
        <div className={styles.passwordWrapper}>
          <input 
            type={showPassword ? 'text' : 'password'}
            id='password'
            name='password'
            placeholder='*********'
            value={form.password}
            onChange={handleChange}
            className={errors.password ? styles.inputError : ''}
            onKeyDown={(e) => {if (e.key === 'Enter') handleSubmit()}}          
          />

          <span className={styles.eyeIcon} onClick={() => setShowPassword(p => !p)}>
            {showPassword ? <EyeOff size={20}/> : <Eye size={20}/>}
          </span>

        </div>
        
        {errors.password && (
          <span className={styles.errorMsg}>{errors.password}</span>
        )}

      </div>

      {/**checkbox recuerdame */}
      <div className={styles.rememberMe}>
        <input 
          type="checkbox"
          id="rememberMe"
          name="rememberMe"
          checked={form.rememberMe}
          onChange={handleChange} 
        />

        <label htmlFor="remenberMe">Recuérdame</label>

      </div>

      {/**boton iniciar sesion */}
      <button className={styles.btnPrimary} onClick={handleSubmit}>
        Iniciar Sesión
      </button>

      {/**link olvidé mi contraseña */}
      <p className={styles.forgotPassword}>
        <Link to="/forgot-password">¿Olvidaste tu contraseña?</Link>
      </p>

      {/**link ir a registro */}
      <p className={styles.registerLink}>
        ¿No tienes cuenta? <Link to="/register">Registrarse</Link>
      </p>

    </AuthLayout>

  )

}

export default Login