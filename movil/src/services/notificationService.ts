import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

export const setupNotificationChannel = async () => {
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('reminders', {
      name: 'Recordatorios',
      importance: Notifications.AndroidImportance.HIGH,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#2D4F83',
    });
  }
};

export const requestPermissions = async () => {
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;
  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }
  return finalStatus === 'granted';
};

export const scheduleReminders = async (
  id: string,
  type: 'fixedExpense' | 'event',
  title: string,
  dateTime: Date,
  offsets: number[] = [24, 12, 6, 3, 1, 0.25]
): Promise<string[]> => {
  const notificationIds: string[] = [];
  for (const offsetHours of offsets) {
    const triggerDate = new Date(dateTime.getTime() - offsetHours * 60 * 60 * 1000);
    if (triggerDate > new Date()) {
      const identifier = await Notifications.scheduleNotificationAsync({
        content: {
          title: type === 'fixedExpense' ? '💰 Gasto fijo próximo' : '📅 Compromiso próximo',
          body: `"${title}" está programado para dentro de ${Math.round(offsetHours)} hora(s)`,
          data: { id, type, reminderOffset: offsetHours },
          sound: true,
          priority: Notifications.AndroidNotificationPriority.HIGH,
        },
        trigger: {
          date: triggerDate,
          channelId: 'reminders',
        },
      });
      notificationIds.push(identifier);
    }
  }
  return notificationIds;
};

export const cancelReminders = async (notificationIds: string[]) => {
  for (const id of notificationIds) {
    await Notifications.cancelScheduledNotificationAsync(id);
  }
};

export const cancelAllReminders = async () => {
  await Notifications.cancelAllScheduledNotificationsAsync();
};
