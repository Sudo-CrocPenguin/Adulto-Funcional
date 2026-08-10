import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';

const AppSessionContext = createContext(null);

export const SESSION_STATUS = Object.freeze({
  anonymous: 'anonymous',
  authenticated: 'authenticated',
  restoring: 'restoring',
});

export function AppSessionProvider({ children, restoreSession }) {
  const [session, setSession] = useState(null);
  const [status, setStatus] = useState(SESSION_STATUS.restoring);

  useEffect(() => {
    let mounted = true;

    async function restore() {
      try {
        const restoredSession = await restoreSession.execute();
        if (!mounted) {
          return;
        }
        setSession(restoredSession);
        setStatus(
          restoredSession
            ? SESSION_STATUS.authenticated
            : SESSION_STATUS.anonymous,
        );
      } catch {
        if (mounted) {
          setSession(null);
          setStatus(SESSION_STATUS.anonymous);
        }
      }
    }

    restore();
    return () => {
      mounted = false;
    };
  }, [restoreSession]);

  const openSession = useCallback((nextSession) => {
    setSession(nextSession);
    setStatus(SESSION_STATUS.authenticated);
  }, []);

  const closeSession = useCallback(() => {
    setSession(null);
    setStatus(SESSION_STATUS.anonymous);
  }, []);

  const value = useMemo(() => ({
    closeSession,
    openSession,
    session,
    status,
  }), [closeSession, openSession, session, status]);

  return (
    <AppSessionContext.Provider value={value}>
      {children}
    </AppSessionContext.Provider>
  );
}

export function useAppSession() {
  const context = useContext(AppSessionContext);

  if (!context) {
    throw new Error('La sesión de la aplicación no está disponible.');
  }

  return context;
}
