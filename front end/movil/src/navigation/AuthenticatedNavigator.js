import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { CommitmentsScreen } from '../modules/commitments/presentation/screens/CommitmentsScreen';
import { HomeScreen } from '../modules/dashboard/presentation/screens/HomeScreen';
import { useAppTheme } from '../theme/AppThemeContext';
import { APP_ROUTES } from './routes';

const Stack = createNativeStackNavigator();

export function AuthenticatedNavigator() {
  const { palette } = useAppTheme();

  return (
    <Stack.Navigator
      screenOptions={{
        animation: 'fade',
        contentStyle: { backgroundColor: palette.background },
        headerShown: false,
      }}
    >
      <Stack.Screen component={HomeScreen} name={APP_ROUTES.home} />
      <Stack.Screen
        component={CommitmentsScreen}
        name={APP_ROUTES.commitments}
      />
    </Stack.Navigator>
  );
}
