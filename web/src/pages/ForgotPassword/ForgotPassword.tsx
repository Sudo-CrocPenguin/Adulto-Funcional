/* Página para recuperar contraseña
el usuario ingresa su correo  recibe instrucciones por email*/

import { useState } from 'react'
import { Link } from 'react-router-dom'
import AuthLayout from '../../components/AuthLayout/AuthLayout'
import styles from "./ForgotPassword.module.css"

interface ForgotPasswordErrors {
  email?: string
}

function ForgotPassword() {

  const [email, setEmail] = useState<string>('')
  const [errors, setErrors ]= useState<ForgotPasswordErrors>({})
  const [successMsg, setSuccessMsg] = useState<string>('')

  const validate = (): ForgotPasswordErrors => {
    const newErrors: ForgotPasswordErrors = {}

    if (!email.trim())
      newErrors.email = "El correo es obligatorio."
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      newErrors.email = "Ingresa un correo válido."

    return newErrors
  }

  const handleSubmit = () => {
    const newErrors = validate()

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setErrors({})

    //TODO: conectar con el backend - POST /api/auth/forgot-password

    setSuccessMsg("Si el correo está registrado, recibirás instrucciones en tu bandeja de entrada.")
  }

  return (

    <AuthLayout>

      <h2 className={styles.title}>Recuperar Contraseña</h2>

      <p className={styles.subtitle}>
        Ingresa tu correo y te enviaremos instrucciones para restablecer tu contraseña
      </p>

      {/*mensaje de exito */}
      {successMsg && (
        <p className={styles.successMsg}>{successMsg}</p>
      )}

      {/*campo correo electronico */}
      {!successMsg && (
        <>
          <div className={styles.formGroup}>
            <label htmlFor="email">Correo Electrónico</label>
            <input 
              type="text"
              id="email"
              name="email"
              placeholder='tucorreo@ejemplo.com'
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                if (errors.email) setErrors({})
              }} 
              
              className={errors.email ? styles.inputError : ''}
              onKeyDown={(e) => {if (e.key === "Enter") handleSubmit() }}
            />

            {errors.email && (
              <span className={styles.errorMsg}>{errors.email}</span>
            )}

          </div>

            {/*boton enviar */}
            <button className={styles.btnPrimary} onClick={handleSubmit}>
              Enviar
            </button>
        </>
      )}

      {/*link volver al login */}
      <p className={styles.backToLogin}>
        <Link to="/login">Volver al inicio de sesión</Link>
      </p>

    </AuthLayout>

  )
}



export default ForgotPassword