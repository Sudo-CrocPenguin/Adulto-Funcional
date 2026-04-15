/* Componente de layout compartido
   Envuelve las páginas protegidas con navbar y sidebar */

import React from 'react'

interface LayoutProps {
  children: React.ReactNode
}

function Layout ({ children }: LayoutProps) {

  return (
    <div>{children}</div>
  )
}

export default Layout
