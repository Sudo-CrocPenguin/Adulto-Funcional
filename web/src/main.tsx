/* Punto de entrada principal de la aplicación
   Monta el componente raíz App en el elemento #root del DOM */

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './assets/styles/global.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext.tsx'

/* StrictMode activa advertencias adicionales en desarrollo
   para detectar problemas potenciales en el código */
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
)