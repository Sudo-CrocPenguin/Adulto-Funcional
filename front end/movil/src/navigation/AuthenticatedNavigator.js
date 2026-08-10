import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { CommitmentsScreen } from '../modules/commitments/presentation/screens/CommitmentsScreen';
import { HomeScreen } from '../modules/dashboard/presentation/screens/HomeScreen';
import { FinanceScreen } from '../modules/finances/presentation/screens/FinanceScreen';
import { FixedExpensesScreen } from '../modules/finances/presentation/screens/FixedExpensesScreen';
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
      <Stack.Screen component={FinanceScreen} name={APP_ROUTES.finances} />
      <Stack.Screen
        component={FixedExpensesScreen}
        name={APP_ROUTES.fixedExpenses}
      />
    </Stack.Navigator>
  );
}
