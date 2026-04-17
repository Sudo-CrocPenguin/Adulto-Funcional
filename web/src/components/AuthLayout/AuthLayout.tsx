/* Componente de layout compartido para paginas de autenticación:
contiene el banner superior y la tarjeta que envuelve los formularios.
Es usado por las pages: Register, Login, ForgotPassword */

import React from "react";
import { Link } from 'react-router-dom'
import logo from '../../assets/logo.png'
import styles from './AuthLayout.module.css'

interface AuthLayoutProps {
    /* contenido de formulario que se renderiza dentro de la tarjeta  */
    children: React.ReactNode
}

function AuthLayout ( { children }: AuthLayoutProps) {

    return (
        <div className={styles.authPage}>

            {/* banner superior con logo y titulo de la aplicacion */}
            <div className={styles.banner}>
                <div className={styles.bannerContent}>

                    {/* logo circular */}
                    <Link to="/">
                        <div className={styles.logoWrapper}>
                            <img src={logo} alt="Logo Adulto Funcional" className={styles.logoImg} />
                        </div>
                    </Link>

                    {/* titulo y subtitulo */}
                    <div className={styles.bannerText}>
                        <h1>Adulto Funcional</h1>
                        <p>Organiza tu vida con control y seguridad</p>
                    </div>
                </div>
            </div>

            {/* tarjeta contenedor del formulario */}
            <div className={styles.card}>
                {children}
            </div>
        </div>
    )
}

export default AuthLayout