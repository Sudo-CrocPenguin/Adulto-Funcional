import { createNativeStackNavigator } from '@react-navigation/native-stack';

import { HomeScreen } from '../modules/dashboard/presentation/screens/HomeScreen';
import { APP_ROUTES } from './routes';

const Stack = createNativeStackNavigator();

export function AuthenticatedNavigator() {
  return (
    <Stack.Navigator
      screenOptions={{
        contentStyle: { backgroundColor: '#F4F6F9' },
        headerShown: false,
      }}
    >
      <Stack.Screen component={HomeScreen} name={APP_ROUTES.home} />
    </Stack.Navigator>
  );
}
