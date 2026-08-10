import { StatusBar } from 'expo-status-bar';
import { useMemo } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { createAppDependencies } from './src/composition/createAppDependencies';
import { RegisterScreen } from './src/modules/auth/presentation/screens/RegisterScreen';

export default function App() {
  const dependencies = useMemo(() => createAppDependencies(), []);

  return (
    <SafeAreaProvider>
      <StatusBar backgroundColor="#35598D" style="light" />
      <RegisterScreen registerAccount={dependencies.registerAccount} />
    </SafeAreaProvider>
  );
}
