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

// ── tipos ──────────────────────────────────────────────────────────────────────
interface Notification {
  id: number;
  category: string;
  message: string;
  icon: string;
}

interface SettingsState {
  username: string;
  language: string;
  notifications: { commitments: boolean; finances: boolean; fixedExpenses: boolean };
  mode: 'light' | 'dark';
  twoStep: boolean;
  backup: string;
}

// ── datos de ejemplo ───────────────────────────────────────────────────────────
const MOCK_NOTIFICATIONS: Notification[] = [
  { id: 1, category: 'Gastos Fijos',  message: 'Internet vence el 14 de feb',      icon: 'ℹ️' },
  { id: 2, category: 'Compromisos',   message: 'Reunión con equipo hoy 12:00 p.m.', icon: 'ℹ️' },
  { id: 3, category: 'Finanzas',      message: 'Transferencia exitosa - $150.00',   icon: 'ℹ️' },
];

const NAV_ITEMS = [
  { to: '/dashboard',        label: 'Inicio',                 icon: HomeIcon },
  { to: '/commitments',      label: 'Compromisos',            icon: ListChecks },
  { to: '/finances',         label: 'Finanzas',               icon: HandCoins },
  { to: '/fixed-expenses',   label: 'Gastos Fijos',           icon: Wallet },
  { to: '/password-manager', label: 'Gestor de Contraseñas',  icon: Lock },
];

// ── componente principal ───────────────────────────────────────────────────────
export default function Layout() {
  const location = useLocation();
  const navigate = useNavigate();

  const [sidebarOpen,   setSidebarOpen]   = useState(true);
  const [notifOpen,     setNotifOpen]     = useState(false);
  const [settingsOpen,  setSettingsOpen]  = useState(false);
  const [settingsTab,   setSettingsTab]   = useState<'general' | 'security'>('general');
  const [notifications, setNotifications] = useState<Notification[]>(MOCK_NOTIFICATIONS);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [settings, setSettings] = useState<SettingsState>({
    username: 'Usuario',
    language: 'Español',
    notifications: { commitments: true, finances: true, fixedExpenses: true },
    mode: 'light',
    twoStep: false,
    backup: 'Diario',
  });

  const notifRef    = useRef<HTMLDivElement>(null);
  const settingsRef = useRef<HTMLDivElement>(null);

  // cerrar popups al hacer click afuera
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

  return (
    <div className={styles.root}>

      {/* ── SIDEBAR ───────────────────────────────────────────────────────── */}
      <aside className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarExpanded : styles.sidebarCollapsed}`}>

        {/*
          Cabecera del sideba
          - Solo el botón toggle hamburguesa / chevron
        */}
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

        {/* navegación */}
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

        {/*
          Footer del sidebar: perfil de usuario*/}
        <div className={styles.sidebarFooter}>
          <button
            className={`${styles.userProfileBtn} ${location.pathname === '/profile' ? styles.userProfileBtnActive : ''}`}
            onClick={() => navigate('/profile')}
            title="Ver perfil"
          >
            <UserCircle2 size={30} className={styles.userAvatar} />
            {sidebarOpen && <span className={styles.userName}>Usuario</span>}
          </button>
        </div>

      </aside>

      {/* ── CONTENIDO DERECHO ─────────────────────────────────────────────── */}
      <div className={styles.mainWrapper}>

        {/* TOPBAR */}
        <header className={styles.topbar}>
          <div className={styles.topbarLeft}>
            <img src={logo} alt="Logo" className={styles.topbarLogo} />
            <span className={styles.topbarTitle}>ADULTO FUNCIONAL</span>
          </div>

          <div className={styles.topbarActions}>

            {/* notificaciones */}
            <div className={styles.iconWrapper} ref={notifRef}>
              <button
                className={styles.iconBtn}
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
                        <span className={styles.notifIcon}>{n.icon}</span>
                        <div>
                          <p className={styles.notifCategory}>{n.category}</p>
                          <p className={styles.notifMsg}>{n.message}</p>
                        </div>
                      </div>
                      <button className={styles.notifClose} onClick={() => dismissNotif(n.id)}>
                        <X size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* configuración */}
            <div className={styles.iconWrapper} ref={settingsRef}>
              <button
                className={styles.iconBtn}
                onClick={() => { setSettingsOpen((p) => !p); setNotifOpen(false); }}
              >
                <Settings size={30} />
              </button>

              {settingsOpen && (
                <div className={`${styles.popup} ${styles.popupSettings}`}>
                  <p className={styles.popupHeader2}>Configuración</p>

                  {/* tabs */}
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
                        <button className={styles.deleteBtn}>Eliminar Cuenta</button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

          </div>
        </header>

        {/* ÁREA DE CONTENIDO */}
        <main className={styles.content}>
          <Outlet />
        </main>

      </div>

      {/* ── MODAL CERRAR SESIÓN ─────────────────────────────────────────────── */}
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
                onClick={() => {
                  setShowLogoutModal(false);
                  navigate('/login');   // ← reemplaza con logout() del AuthContext si lo tienes
                }}
              >
                Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}