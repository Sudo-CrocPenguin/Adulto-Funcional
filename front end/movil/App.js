import { StatusBar } from 'expo-status-bar';
import { useMemo } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { AppDependenciesProvider } from './src/composition/AppDependenciesContext';
import { createAppDependencies } from './src/composition/createAppDependencies';
import { RootNavigator } from './src/navigation/RootNavigator';
import { MandatoryUpdateGate } from './src/modules/updates/presentation/MandatoryUpdateGate';
import { AppSessionProvider } from './src/session/AppSessionContext';
import { AppThemeProvider } from './src/theme/AppThemeContext';

export default function App() {
  const dependencies = useMemo(() => createAppDependencies(), []);

  return (
    <SafeAreaProvider>
      <StatusBar backgroundColor="#35598D" style="light" />
      <AppDependenciesProvider value={dependencies}>
        <AppThemeProvider preferenceStore={dependencies.themePreferenceStore}>
          <MandatoryUpdateGate ensureLatestUpdate={dependencies.ensureLatestUpdate}>
            <AppSessionProvider restoreSession={dependencies.restoreSession}>
              <RootNavigator />
            </AppSessionProvider>
          </MandatoryUpdateGate>
        </AppThemeProvider>
      </AppDependenciesProvider>
    </SafeAreaProvider>
  );
}
