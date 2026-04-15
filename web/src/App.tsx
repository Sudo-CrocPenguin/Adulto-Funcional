/* Componente raíz de la aplicación
   Define todas las rutas públicas y protegidas */

import { BrowserRouter, Routes, Route } from 'react-router-dom'

/* Páginas de autenticación */
import Register from './pages/Register/Register'
import Login from './pages/Login/Login'
import ForgotPassword from './pages/ForgotPassword/ForgotPassword'

/* Layout compartido para páginas protegidas */
import Layout from './components/Layout'

/* Páginas protegidas */
import Dashboard from './pages/Dashboard/Dashboard'
import Commitments from './pages/Commitments/Commitments'
import Finances from './pages/Finances/Finances'
import FixedExpenses from './pages/FixedExpenses/FixedExpenses'
import Profile from './pages/Profile/Profile'

/* Gestor de contraseñas */
import PasswordManagerAccess from './pages/PasswordManagerAccess/PasswordManagerAccess'
import PasswordManagerReset from './pages/PasswordManagerReset/PasswordManagerReset'
import PasswordManager from './pages/PasswordManager/PasswordManager'

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Rutas públicas - accesibles sin autenticación */}
        <Route path="/" element={<Register />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />

        {/* Rutas protegidas - requieren autenticación, envueltas en Layout */}
        <Route path="/dashboard" element={<Layout><Dashboard /></Layout>} />
        <Route path="/commitments" element={<Layout><Commitments /></Layout>} />
        <Route path="/finances" element={<Layout><Finances /></Layout>} />
        <Route path="/fixed-expenses" element={<Layout><FixedExpenses /></Layout>} />
        <Route path="/profile" element={<Layout><Profile /></Layout>} />

        {/* Rutas del gestor de contraseñas */}
        <Route path="/password-manager" element={<Layout><PasswordManagerAccess /></Layout>} />
        <Route path="/password-manager/reset" element={<Layout><PasswordManagerReset /></Layout>} />
        <Route path="/password-manager/home" element={<Layout><PasswordManager /></Layout>} />

      </Routes>
    </BrowserRouter>
  )
}

export default App