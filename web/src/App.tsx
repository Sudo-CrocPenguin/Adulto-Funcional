/* Componente raíz de la aplicación
   Define todas las rutas públicas y protegidas */

import { BrowserRouter, Routes, Route } from 'react-router-dom'

/* Páginas de autenticación */
import ForgotPassword from './pages/ForgotPassword/ForgotPassword'

/* Layout compartido para páginas protegidas */
import Layout from './components/Layout/Layout';

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
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute' 

import LandingPage from './pages/LandingPage/LandingPage'


function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Rutas públicas - accesibles sin autenticación */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/login" element={<LandingPage />} /> {/* Ruta de login que usas en ProtectedRoute */}


        {/*  RUTAS PROTEGIDAS - CORREGIDO PARA TU ProtectedRoute */}
        <Route element={
          <ProtectedRoute>
            <Layout />  {/* El Layout va aquí, como base para todas */}
          </ProtectedRoute>
        }>
          {/* TODAS estas páginas se muestran DENTRO del Layout */}
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/commitments" element={<Commitments />} />
          <Route path="/finances" element={<Finances />} />
          <Route path="/fixed-expenses" element={<FixedExpenses />} />
          <Route path="/profile" element={<Profile />} />

          <Route path="/password-manager" element={<PasswordManagerAccess />} />
          <Route path="/password-manager/reset" element={<PasswordManagerReset />} />
          <Route path="/password-manager/home" element={<PasswordManager />} />
        </Route>

      </Routes>
    </BrowserRouter>
  )
}

export default App