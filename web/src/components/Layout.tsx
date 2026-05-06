/** @jsxRuntime classic */
/** @jsx React.createElement */
import React from 'react'
import styles from './Layout.module.css'

interface LayoutProps {
  children: React.ReactNode
}

function Layout({ children }: LayoutProps) {
  return (
    <div className={styles.container}>

      {/* Sidebar */}
      <aside className={styles.sidebar}>
        <h2 className={styles.logo}>Adulto Funcional</h2>

        <nav className={styles.menu}>
          <a href="#">Dashboard</a>
          <a href="#">Finanzas</a>
          <a href="#">Agenda</a>
          <a href="#">Contraseñas</a>
        </nav>
      </aside>

      {/* Main Content */}
      <div className={styles.main}>

        {/* Header */}
        <header className={styles.header}>
          <div className={styles.search}>
            <input type="text" placeholder="Buscar..." />
          </div>

          <div className={styles.actions}>
            <span className={styles.icon}>🔔</span>
            <span className={styles.icon}>⚙️</span>

            <div className={styles.profile}>
              <span>Usuario</span>
              <img
                src="/logo.png"
                alt="profile"
                className={styles.avatar}
              />
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className={styles.content}>
          {children}
        </main>

      </div>
    </div>
  )
}

export default Layout