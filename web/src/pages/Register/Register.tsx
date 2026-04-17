/*Pagina de registro de usuario, permite crear una nueva cuenta con validaciones 
en tiempo real
campos: nombres, apellidos, telefono, correo, contraseñas, confirmar contraseña */

import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff } from 'lucide-react'
import AuthLayout from '../../components/AuthLayout/AuthLayout'
import styles from './Register.module.css'

/* interfaz que define la estructura del formulario de registro*/
interface RegisterForm {
    firstName: string
    lastName: string
    phone: string
    email: string
    password: string
    confirmPassword: string
}

/* interfaz para los errores de validacion de cada campo */
interface RegisterErrors {
    firstName?: string
    lastName?: string
    phone?: string
    email?: string
    password?: string
    confirmPassword?: string
}

function Register() {

    //hook para redireccion programatica despues del resgistro
    const navigate = useNavigate()

    //estado del formulario: almacena los valores del usuario
    const [form, setForm] = useState<RegisterForm>({
        firstName: '',
        lastName: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: '',
    })

    //estado de errores de validacion por campo
    const [errors, setErrors] = useState<RegisterErrors>({})

    //mensaje de exito mostrado antes de redirigir al login
    const [successMsg, setSuccessMsg] = useState<string>('')

    //constrola la visibilidad de la contraseña - true: visible, false: oculta
    const [showPassword, setShowPassword] = useState<boolean>(false)
    const [showConfirmPassword, setShowConfirmPassword] = useState<boolean>(false)

    //maneja los cambios en los campos del formulario
    /*aplica validaciones en tiempo real;
    nombres y apellidos no permite numeros
    telefono solo permite numeros maximo 10 digitos
    limpia el error del campo al escribir */

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target

        //no permite numeros en nombres ni apellidos
        if (name === 'firstName' && /\d/.test(value)) return
        if (name === 'lastName' && /\d/.test(value)) return

        //en telefono solo permite numeros y maximo  10 digitos
        if (name === 'phone') {
            if (/\D/.test(value)) return
            if (value.length > 10) return
        }

        //actualiza el estado del formulario
        setForm({ ...form, [name]: value})
    }

    /*valida todos los campos del formulario
    retorna un objeto conlos errores encontrados
    validaciones:
    campos obligatorios, formato de correo valido, telefono exactamente 10 digitos,
    contraseña de minimo 8 caracteres, mayuscula, minuscula y numero,
    confirmacion de contraseña coincida */

    const validate = (): RegisterErrors => {
        const newErrors: RegisterErrors = {}

        //validaciones
        if (!form.firstName.trim())
            newErrors.firstName = "El nombre es obligatorio."

        if (!form.lastName.trim())
            newErrors.lastName = "El apellido es obligatorio."

        if (!form.phone.trim())
            newErrors.phone = "El teléfono es obligatorio."
        else if (form.phone.length !== 10)
            newErrors.phone = "El teléfono debe tener exactamente 10 dígitos."

        if (!form.email.trim())
            newErrors.email = "El correo es obligatorio."
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
            newErrors.email = "Ingresa un correo válido."

        if (!form.password) {
            newErrors.password = "La contraseña es obligatoria."
        } else if (form.password.length < 8) {
            newErrors.password = "Mínimo 8 caracteres."
        }else if (!/[A-Z]/.test(form.password)){
            newErrors.password = "Debe tener al menos una mayúscula."
        }else if (!/[a-z]/.test(form.password)) {
            newErrors.password = "Debe tener al menos una minúscula."
        }else if (!/[0-9]/.test(form.password)) {
            newErrors.password = "Debe tener al menos un número."
        }

        if (!form.confirmPassword)
            newErrors.confirmPassword = "Confirma tu contraseña."
        else if (form.password !== form.confirmPassword)
            newErrors.confirmPassword = "Las contraseñas no coinciden."

        return newErrors
    }

    /* maneja el envio del formulario
    si hay errores los muestra, si no muestra éxito y redirige al login */

    const handleSubmit = () => {
        const newErrors = validate()

        //si hay errores los muestra y detiene el proceso
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors)
            return
        }

        //aqui conexion con back para registrar el usuario

        //muestra mensaje de éxito y redirige al login despues de 2 segundos
        setSuccessMsg('¡Cuenta creada exitosamente! Redirigiendo al login...')
        setTimeout(() => navigate('/login'), 2000)
    }

    return (
        <AuthLayout>

            {/*titulo del formulario */}
            <h2 className={styles.title}>Registrarse</h2>

            {/*mensaje de texto temporal */}
            {successMsg && (
                <p className={styles.successMsg}>{successMsg}</p>
            )}

            {/*campo nombres */}
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
                {errors.firstName && (
                    <span className={styles.errorMsg}>{errors.firstName}</span>
                )}
            </div>

            {/*campo apellidos */}
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
                {errors.lastName && (
                    <span className={styles.errorMsg}>{errors.lastName}</span>
                )}
            </div>

            {/* campo de telefono con prefijo +57 fijo */}
            <div className={styles.formGroup}>
                <label htmlFor="phone">Teléfono</label>
                <div className={styles.phoneWrapper}>
                    {/*prefijo de colombia, no editable */}
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
                {errors.phone && (
                    <span className={styles.errorMsg}>{errors.phone}</span>
                )}
            </div>

                {/* campo correo electronico */}
            <div className={styles.formGroup}>
                <label htmlFor="email">Correo Electónico</label>
                <input 
                    type="text"
                    id="email"
                    name="email"
                    placeholder="tucorreo@ejemplo.co"
                    value={form.email}
                    onChange={handleChange}
                    className={errors.email ? styles.inputError : ''}

                />
                {errors.email && (
                    <span className={styles.errorMsg}>{errors.email}</span>
                )}
            </div>

            {/*campo contraseña con boton mostrar/ocultar*/}
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
                    <span
                        className={styles.eyeIcon}
                        onClick={() => setShowPassword(p => !p)}    
                    >
                        {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                    </span>
                </div>
                {errors.password && (
                    <span className={styles.errorMsg}>{errors.password}</span>
                )}
            </div>

            {/*campo confirmar contraseña con boton mostrar/ocultar */}
            <div className={styles.formGroup}>
                <label htmlFor="confirmPassword">Confirmar Contraseña</label>
                <div className={styles.passwordWrapper}>
                    <input 
                        type={showConfirmPassword ? 'text' : 'password'}
                        id="confirmPassword"
                        name="confirmPassword"
                        placeholder="********"
                        value={form.confirmPassword}
                        onChange={handleChange}
                        className={errors.confirmPassword ? styles.inputError : ''}
                        onKeyDown={(e) => {if (e.key === 'Enter') handleSubmit() }} 
                    />
                    <span
                        className={styles.eyeIcon}
                        onClick={() => setShowConfirmPassword(p => !p)}
                    >
                        {showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                    </span>
                </div>
                {errors.confirmPassword && (
                    <span className={styles.errorMsg}>{errors.confirmPassword}</span>                   
                )}     
            </div>

            {/* boton para crear la cuenta */}
            <button className={styles.btnPrimary} onClick={handleSubmit}>
                Crear Cuenta
            </button>

            {/*enlace si ya tiene cuenta */}
            <p className={styles.loginLink}>
                ¿Ya tienes cuenta? {' '}
                <Link to="/login">Iniciar Sesión</Link>
            </p>

        </AuthLayout>
    )
}

export default Register