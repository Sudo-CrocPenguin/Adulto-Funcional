import { useEffect, useState } from 'react';
import * as Notifications from 'expo-notifications';
import { requestPermissions, setupNotificationChannel, scheduleReminders, cancelReminders } from '../services/notificationService';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

export const useNotifications = () => {
  const [permissionGranted, setPermissionGranted] = useState(false);

  useEffect(() => {
    const init = async () => {
      await setupNotificationChannel();
      const granted = await requestPermissions();
      setPermissionGranted(granted);
    };
    init();
  }, []);

  const scheduleForItem = async (
    id: string,
    type: 'fixedExpense' | 'event',
    name: string,
    dateTime: Date,
    currentNotificationIds?: string[]
  ) => {
    if (!permissionGranted) return [];
    if (currentNotificationIds?.length) {
      await cancelReminders(currentNotificationIds);
    }
    return await scheduleReminders(id, type, name, dateTime);
  };

  const cancelForItem = async (notificationIds: string[]) => {
    if (notificationIds?.length) {
      await cancelReminders(notificationIds);
    }
  };

  return { scheduleForItem, cancelForItem, permissionGranted };
};
