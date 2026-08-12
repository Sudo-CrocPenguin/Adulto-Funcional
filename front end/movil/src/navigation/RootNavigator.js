import { NavigationContainer } from '@react-navigation/native';
import { ActivityIndicator, StyleSheet, View } from 'react-native';

import { useAppSession, SESSION_STATUS } from '../session/AppSessionContext';
import { useAppTheme } from '../theme/AppThemeContext';
import { AuthenticatedNavigator } from './AuthenticatedNavigator';
import { AuthNavigator } from './AuthNavigator';
import { AUTH_ROUTES } from './routes';

export function RootNavigator() {
  const { session, status } = useAppSession();
  const { palette } = useAppTheme();

  if (status === SESSION_STATUS.restoring) {
    return (
      <View
        accessibilityLabel="Restaurando sesión"
        style={[styles.loading, { backgroundColor: palette.background }]}
      >
        <ActivityIndicator color={palette.brand} size="large" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      {session ? (
        <AuthenticatedNavigator />
      ) : (
        <AuthNavigator
          initialRouteName={status === SESSION_STATUS.signedOut
            ? AUTH_ROUTES.login
            : AUTH_ROUTES.register}
        />
      )}
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  loading: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
  },
});
