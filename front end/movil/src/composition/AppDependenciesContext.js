import { createContext, useContext } from 'react';

const AppDependenciesContext = createContext(null);

export const AppDependenciesProvider = AppDependenciesContext.Provider;

export function useAppDependencies() {
  const dependencies = useContext(AppDependenciesContext);

  if (!dependencies) {
    throw new Error('Las dependencias de la aplicación no están disponibles.');
  }

  return dependencies;
}

