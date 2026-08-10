import { StatusBar } from 'expo-status-bar';
import { useMemo } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { AppDependenciesProvider } from './src/composition/AppDependenciesContext';
import { createAppDependencies } from './src/composition/createAppDependencies';
import { AuthNavigator } from './src/navigation/AuthNavigator';

export default function App() {
  const dependencies = useMemo(() => createAppDependencies(), []);

  return (
    <SafeAreaProvider>
      <StatusBar backgroundColor="#35598D" style="light" />
      <AppDependenciesProvider value={dependencies}>
        <AuthNavigator />
      </AppDependenciesProvider>
    </SafeAreaProvider>
  );
}
