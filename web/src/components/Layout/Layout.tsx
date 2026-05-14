import { useState, useRef, useEffect } from 'react';
import { Outlet, NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  HomeIcon,
  ListChecks,
  HandCoins,
  Wallet,
  Lock,
  BellRing,
  Settings,
  X,
  UserCircle2,
  ChevronRight,
  AlignJustify,
} from 'lucide-react';
import logo from '../../assets/logo.png';
import styles from './Layout.module.css';
import { accountService, type Account } from '../../services/account.service';
import { logout as authLogout } from '../../services/auth.service';

/**
 * Interfaz que define la estructura de una notificación.
 * 
 * @interface Notification
 * @property {number} id - Identificador único de la notificación
 * @property {string} category - Categoría de la notificación (Gastos Fijos, Compromisos, Finanzas)
 * @property {string} message - Mensaje descriptivo de la notificación
 */
interface Notification {
  id: number;
  category: string;
  message: string;
}

/**
 * Interfaz que define el estado de configuración del usuario.
 * 
 * @interface SettingsState
 * @property {string} username - Nombre de usuario
 * @property {string} language - Idioma seleccionado (Español, English)
 * @property {Object} notifications - Preferencias de notificaciones por categoría
 * @property {boolean} notifications.commitments - Notificaciones de compromisos
 * @property {boolean} notifications.finances - Notificaciones de finanzas
 * @property {boolean} notifications.fixedExpenses - Notificaciones de gastos fijos
 * @property {'light' | 'dark'} mode - Modo de tema (claro/oscuro)
 * @property {boolean} twoStep - Estado de verificación en dos pasos
 * @property {string} backup - Frecuencia de respaldo automático (Diario, Semanal, Mensual)
 */
interface SettingsState {
  username: string;
  language: string;
  notifications: { commitments: boolean; finances: boolean; fixedExpenses: boolean };
  mode: 'light' | 'dark';
  twoStep: boolean;
  backup: string;
}

/**
 * Datos de ejemplo para notificaciones.
 * @constant {Notification[]}
 */

/*TODO: BACKEND - Reemplazar con llamada a API REST para obtener notificaciones del usuario.
 * Endpoint sugerido: GET /api/notifications
 * Respuesta esperada: Array<{ id: number, category: string, message: string }> */

const MOCK_NOTIFICATIONS: Notification[] = [
  { id: 1, category: 'Gastos Fijos',  message: 'Internet vence el 14 de feb' },
  { id: 2, category: 'Compromisos',   message: 'Reunión con equipo hoy 12:00 p.m.' },
  { id: 3, category: 'Finanzas',      message: 'Transferencia exitosa - $150.00' },
];

/**
 * Configuración de los items de navegación del sidebar.
 * Cada item define la ruta, etiqueta e icono correspondiente.
 * 
 * @constant {Array<{ to: string, label: string, icon: React.ComponentType }>}
 */

const NAV_ITEMS = [
  { to: '/dashboard',        label: 'Inicio',                 icon: HomeIcon },
  { to: '/commitments',      label: 'Compromisos',            icon: ListChecks },
  { to: '/finances',         label: 'Finanzas',               icon: HandCoins },
  { to: '/fixed-expenses',   label: 'Gastos Fijos',           icon: Wallet },
  { to: '/password-manager', label: 'Gestor Contraseñas',  icon: Lock },
];

/**
 * Componente principal de Layout.
 * 
 * Proporciona la estructura base de la aplicación que incluye:
 * - Sidebar colapsable con navegación
 * - Topbar con logo, notificaciones y configuración
 * - Área de contenido principal mediante Outlet de React Router
 * - Modal de confirmación para cierre de sesión
 * - Popups de notificaciones y configuración
 * 
 * Gestiona el estado local de:
 * - Apertura/cierre del sidebar
 * - Visualización de popups de notificaciones y configuración
 * - Preferencias de usuario (idioma, tema, notificaciones)
 * - Modal de cierre de sesión
 * 
 * @component
 * @returns {JSX.Element} Estructura completa del layout de la aplicación
 */

export default function Layout() {

  const location = useLocation();
  const navigate = useNavigate();

  /** Estado de apertura/cierre del sidebar */
  const [sidebarOpen,   setSidebarOpen]   = useState(true);
  /** Estado de visibilidad del popup de notificaciones */
  const [notifOpen,     setNotifOpen]     = useState(false);
  /** Estado de visibilidad del popup de configuración */
  const [settingsOpen,  setSettingsOpen]  = useState(false);
  /** Pestaña activa en el popup de configuración */
  const [settingsTab,   setSettingsTab]   = useState<'general' | 'security'>('general');

  const [notifications, setNotifications] = useState<Notification[]>(MOCK_NOTIFICATIONS);
  /** Estado de visibilidad del modal de cierre de sesión */
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [account, setAccount] = useState<Account | null>(null);

  const [settings, setSettings] = useState<SettingsState>({
    username: sessionStorage.getItem('names') || 'Usuario',
    language: 'Español',
    notifications: { commitments: true, finances: true, fixedExpenses: true },
    mode: 'light',
    twoStep: false,
    backup: 'Diario',
  });

  /** Referencia al contenedor del popup de notificaciones */
  const notifRef    = useRef<HTMLDivElement>(null);
  /** Referencia al contenedor del popup de configuración */
  const settingsRef = useRef<HTMLDivElement>(null);

  //cargar cuenta al montar
  useEffect(() => {
    const accountId = sessionStorage.getItem('accountId');
    if (accountId) {
      accountService.getById(accountId)
        .then((acc) => { 
          setAccount(acc);
          setSettings((s) => ({ ...s, username: acc.names}));
        })

        .catch(console.error);
    }
  }, []);

  /**
   * Efecto para cerrar los popups al hacer clic fuera de ellos.
   * Agrega un event listener al documento y lo limpia al desmontar.
   */
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (notifRef.current    && !notifRef.current.contains(e.target as Node))    setNotifOpen(false);
      if (settingsRef.current && !settingsRef.current.contains(e.target as Node)) setSettingsOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const dismissNotif = (id: number) =>
    setNotifications((prev) => prev.filter((n) => n.id !== id));

  //cerrar sesion
  const handleLogout = async () => {
    setShowLogoutModal(false);
    try {
      await authLogout();  
    } catch (error) {
      console.error('Error cerrando sesión:', error);
    }
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('accountId');
    sessionStorage.removeItem('names');
    navigate('/login');
  };

  const handleDeleteAccount = async () => {
    setShowDeleteModal(false);
    const accountId = sessionStorage.getItem('accountId');
    if (accountId) {
      try {
        await accountService.remove(accountId);
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('accountId');
        sessionStorage.removeItem('names');
        navigate('/login');
      } catch (error: any) {
        console.error('Error eliminando cuenta:', error.response?.status, error);
        alert('La eliminación de cuenta aún no está disponible. Contacta al administrador.');
      }
    }
  };

  return (
    <div className={styles.root}>

      {/* ──────────────────────────────────────────────────────────────────
          SIDEBAR
          Contiene la navegación principal y el acceso al perfil.
          Se colapsa/expande mediante los botones de toggle.
      ────────────────────────────────────────────────────────────────── */}
    
      <aside className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarExpanded : styles.sidebarCollapsed}`}>

        {/* Cabecera del sidebar con botón toggle hamburguesa/chevron */}
        <div className={styles.sidebarHeader}>
          {sidebarOpen ? (
            <button className={styles.hamburger} onClick={() => setSidebarOpen(false)}>
              <AlignJustify size={27} />
            </button>
          ) : (
            <button className={styles.collapseToggle} onClick={() => setSidebarOpen(true)}>
              <ChevronRight size={27} />
            </button>
          )}
        </div>

        {/* Navegación principal */}
        <nav className={styles.nav}>
          {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `${styles.navItem} ${isActive ? styles.navItemActive : ''}`
              }
            >
              <Icon size={30} className={styles.navIcon} />
              {sidebarOpen && <span className={styles.navLabel}>{label}</span>}
            </NavLink>
          ))}
        </nav>

          {/* Footer del sidebar con acceso al perfil de usuario */}
        <div className={styles.sidebarFooter}>
          <button
            className={`${styles.userProfileBtn} ${location.pathname === '/profile' ? styles.userProfileBtnActive : ''}`}
            onClick={() => navigate('/profile')}
            title="Ver perfil"
          >
            <UserCircle2 size={30} className={styles.userAvatar} />
            {sidebarOpen && <span className={styles.userName}>
              {account?.names || sessionStorage.getItem('names') || 'Usuario'}
              </span>}
          </button>
        </div>

      </aside>

      <div className={styles.mainWrapper}>

        <header className={styles.topbar}>
          <div className={styles.topbarLeft}>
            <button
              className={styles.mobileMenuBtn}
              onClick={() => setSidebarOpen((v) => !v)}
            >
              <AlignJustify size={24} />
            </button>
            <img src={logo} alt="Logo" className={styles.topbarLogo} />
            <span className={styles.topbarTitle}>ADULTO FUNCIONAL</span>
          </div>

          <div className={styles.topbarActions}>

            <div className={styles.iconWrapper} ref={notifRef}>
              <button
                className={`${styles.iconBtn} ${styles.notificationBtn}`}
                onClick={() => { setNotifOpen((p) => !p); setSettingsOpen(false); }}
              >
                <BellRing size={30} />
                {notifications.length > 0 && (
                  <span className={styles.badge}>{notifications.length}</span>
                )}
              </button>

              {notifOpen && (
                <div className={styles.popup}>
                  <div className={styles.popupHeader}>
                    <BellRing size={14} /> <span>Notificaciones</span>
                  </div>
                  {notifications.length === 0 && (
                    <p className={styles.emptyMsg}>Sin notificaciones</p>
                  )}
                  {notifications.map((n) => (
                    <div key={n.id} className={styles.notifItem}>
                      <div className={styles.notifContent}>
                        <div>
                          <p className={styles.notifCategory}>{n.category}</p>
                          <p className={styles.notifMsg}>{n.message}</p>
                        </div>
                      </div>
                      <button className={styles.notifClose} onClick={() => dismissNotif(n.id)}>
                        <X size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className={styles.iconWrapper} ref={settingsRef}>
              <button
                className={`${styles.iconBtn} ${styles.settingsBtn}`}
                onClick={() => { setSettingsOpen((p) => !p); setNotifOpen(false); }}
              >
                <Settings size={30} />
              </button>

              {settingsOpen && (
                <div className={`${styles.popup} ${styles.popupSettings}`}>
                  <p className={styles.popupHeader2}>Configuración</p>

                  <div className={styles.tabs}>
                    <button
                      className={`${styles.tab} ${settingsTab === 'general' ? styles.tabActive : ''}`}
                      onClick={() => setSettingsTab('general')}
                    >
                      General
                    </button>
                    <button
                      className={`${styles.tab} ${settingsTab === 'security' ? styles.tabActive : ''}`}
                      onClick={() => setSettingsTab('security')}
                    >
                      Seguridad
                    </button>
                  </div>

                  {settingsTab === 'general' && (
                    <div className={styles.settingsBody}>
                      <label className={styles.settingsLabel}>Nombre de Usuario</label>
                      <input
                        className={styles.settingsInput}
                        value={settings.username}
                        onChange={(e) => setSettings((s) => ({ ...s, username: e.target.value }))}
                      />

                      <label className={styles.settingsLabel}>Idioma</label>
                      <select
                        className={styles.settingsSelect}
                        value={settings.language}
                        onChange={(e) => setSettings((s) => ({ ...s, language: e.target.value }))}
                      >
                        <option>Español</option>
                        <option>English</option>
                      </select>

                      <label className={styles.settingsLabel}>Notificaciones</label>
                      {(['commitments', 'finances', 'fixedExpenses'] as const).map((key) => {
                        const labels: Record<string, string> = {
                          commitments:  'Compromisos',
                          finances:     'Finanzas',
                          fixedExpenses: 'Gastos Fijos',
                        };
                        return (
                          <div key={key} className={styles.checkRow}>
                            <span>{labels[key]}</span>
                            <input
                              type="checkbox"
                              checked={settings.notifications[key]}
                              onChange={(e) =>
                                setSettings((s) => ({
                                  ...s,
                                  notifications: { ...s.notifications, [key]: e.target.checked },
                                }))
                              }
                            />
                          </div>
                        );
                      })}

                      <label className={styles.settingsLabel}>Modo</label>
                      <div className={styles.modeRow}>
                        <button
                          className={`${styles.modeBtn} ${settings.mode === 'light' ? styles.modeBtnActive : ''}`}
                          onClick={() => setSettings((s) => ({ ...s, mode: 'light' }))}
                        >
                          Claro
                        </button>
                        <button
                          className={`${styles.modeBtn} ${settings.mode === 'dark' ? styles.modeBtnActive : ''}`}
                          onClick={() => setSettings((s) => ({ ...s, mode: 'dark' }))}
                        >
                          Oscuro
                        </button>
                      </div>
                    </div>
                  )}

                  {settingsTab === 'security' && (
                    <div className={styles.settingsBody}>
                      <div className={styles.secRow}>
                        <span className={styles.settingsLabel}>Verificación en dos pasos</span>
                        <button
                          className={`${styles.activateBtn} ${settings.twoStep ? styles.activateBtnOn : ''}`}
                          onClick={() => setSettings((s) => ({ ...s, twoStep: !s.twoStep }))}
                        >
                          {settings.twoStep ? 'Activado' : 'Activar'}
                        </button>
                      </div>

                      <label className={styles.settingsLabel}>Respaldo automático</label>
                      <select
                        className={styles.settingsSelect}
                        value={settings.backup}
                        onChange={(e) => setSettings((s) => ({ ...s, backup: e.target.value }))}
                      >
                        <option>Diario</option>
                        <option>Semanal</option>
                        <option>Mensual</option>
                      </select>

                      <label className={styles.settingsLabel}>Inicio de sesión</label>
                      <div className={styles.sessionRow}>
                        <button
                          className={styles.logoutBtn}
                          onClick={() => { setSettingsOpen(false); setShowLogoutModal(true); }}
                        >
                          Cerrar Sesión
                        </button>
                        <button className={styles.deleteBtn} onClick={() => { setSettingsOpen(false); setShowDeleteModal(true);}}>
                          Eliminar Cuenta
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

          </div>
        </header>

        {/*
          Área de contenido dinámico.
          Renderiza el componente correspondiente a la ruta actual mediante React Router Outlet.
        */}
        <main className={styles.content}>
          <Outlet />
        </main>

      </div>

      {showLogoutModal && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <p className={styles.modalText}>¿Estás seguro que quieres cerrar sesión?</p>
            <div className={styles.modalActions}>
              <button className={styles.cancelBtn} onClick={() => setShowLogoutModal(false)}>
                Cancelar
              </button>
              <button
                className={styles.confirmLogoutBtn}
                onClick={handleLogout}
              >
                Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL ELIMINAR CUENTA */}
      {showDeleteModal && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <p className={styles.modalText}>¿Estás seguro que quieres eliminar tu cuenta?</p>
            <p className={styles.modalWarning}>
              Esta acción es irreversible. Se eliminarán todos tus datos, 
              finanzas, compromisos, gastos fijos y contraseñas guardadas.
            </p>
            <div className={styles.modalActions}>
              <button className={styles.cancelBtn} onClick={() => setShowDeleteModal(false)}>
                Cancelar
              </button>
              <button className={styles.confirmDeleteBtn} onClick={handleDeleteAccount}>
                Eliminar Cuenta
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}