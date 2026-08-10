import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { LoginScreen } from '../modules/auth/presentation/screens/LoginScreen';
import { PasswordRecoveryScreen } from '../modules/auth/presentation/screens/PasswordRecoveryScreen';
import { RegisterScreen } from '../modules/auth/presentation/screens/RegisterScreen';
import { AUTH_ROUTES } from './routes';

const Stack = createNativeStackNavigator();

export function AuthNavigator() {
  return (
    <Stack.Navigator
      initialRouteName={AUTH_ROUTES.register}
      screenOptions={{
        animation: 'slide_from_right',
        contentStyle: { backgroundColor: '#F4F6F9' },
        headerShown: false,
      }}
    >
      <Stack.Screen component={RegisterScreen} name={AUTH_ROUTES.register} />
      <Stack.Screen component={LoginScreen} name={AUTH_ROUTES.login} />
      <Stack.Screen
        component={PasswordRecoveryScreen}
        name={AUTH_ROUTES.passwordRecovery}
      />
    </Stack.Navigator>
  );
}
