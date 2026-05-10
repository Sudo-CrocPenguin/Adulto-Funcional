import { useEffect, useState } from 'react'
import { TrendingUp, ClipboardCheck, LockKeyhole } from 'lucide-react'
import logo from '../../assets/logo.png'
import styles from './LandingPage.module.css'
import Login from '../Login/Login'
import Register from '../Register/Register'
import { useLocation } from 'react-router-dom'

type ModalType = 'login' | 'register' | null

function LandingPage() {

    const [modal, setModal] = useState<ModalType>(null)

    const location = useLocation()

    useEffect(() => {
        
    if (location.state?.openLogin) {
        setModal('login')
    }

    }, [location.state])

    return (
 
        <div className={styles.page}>

            {/* Modales */}
            {modal === 'login' && (
                <Login
                    onClose={() => setModal(null)}
                    onGoToRegister={() => setModal('register')}
                />
            )}
            {modal === 'register' && (
                <Register
                    onClose={() => setModal(null)}
                    onGoToLogin={() => setModal('login')}
                />
            )}
        
            {/* ── Fondo decorativo ─── */}
            <div className={styles.bgDecor} aria-hidden="true">
                <span className={styles.orb1}></span>
                <span className={styles.orb2}></span>
                <span className={styles.ring1}></span>
                <span className={styles.ring2}></span>
                <span className={styles.ring3}></span>
                <span className={styles.ring4}></span>
            </div>
 
            {/* ── Navbar ─── */}
            <nav className={styles.navbar}>
                <div className={styles.navBrand}>
                    <div className={styles.logoWrapper}>
                        <img src={logo} alt="Logo Adulto Funcional" className={styles.logoImg} />
                    </div>
                    <span className={styles.brandName}>Adulto Funcional</span>
                </div>
                <div className={styles.navActions}>
                    <button className={styles.btnOutline} onClick={() => setModal('login')}>
                        Iniciar sesión
                    </button>
                    <button className={styles.btnTeal} onClick={() => setModal('register')}>
                        Registrarse
                    </button>
                </div>
            </nav>
 
            {/* ── Hero ─── */}
            <section className={styles.hero}>
                <div className={styles.badge}>
                    <span className={styles.badgeDot} />
                    Tu organizador personal todo en uno
                </div>
                <h1 className={styles.heroTitle}>
                    Organiza tu vida con
                    <span className={styles.heroAccent}>control y seguridad</span>
                </h1>
                <p className={styles.heroSubtitle}>
                    Gestiona tus finanzas, tareas, gastos fijos y contraseñas en <br />
                    un solo lugar&nbsp;
                    <span className={styles.heroHighlight}>100% privado.</span>
                </p>
                <button className={styles.btnHero} onClick={() => setModal('register')}>
                    Crear Cuenta
                </button>
            </section>
 
            {/* ── Features ─── */}
            <section className={styles.features}>
                <div className={styles.featureItem}>
                    <div className={styles.featureIcon}>
                        <TrendingUp size={26} strokeWidth={1.8} />
                    </div>
                    <div className={styles.featureText}>
                        <h3>Finanzas</h3>
                        <p>Ingresos, gastos <br /> y presupuesto</p>
                    </div>
                </div>
                <div className={styles.featureItem}>
                    <div className={styles.featureIcon}>
                        <ClipboardCheck size={26} strokeWidth={1.8} />
                    </div>
                    <div className={styles.featureText}>
                        <h3>Tareas</h3>
                        <p>Compromisos <br /> y pendientes</p>
                    </div>
                </div>
                <div className={styles.featureItem}>
                    <div className={styles.featureIcon}>
                        <LockKeyhole size={26} strokeWidth={1.8} />
                    </div>
                    <div className={styles.featureText}>
                        <h3>Contraseñas</h3>
                        <p>Gestor cifrado <br /> y seguro</p>
                    </div>
                </div>
            </section>
 
        </div>
 
    )
}

export default LandingPage