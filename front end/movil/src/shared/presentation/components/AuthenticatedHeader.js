import { useMemo, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import { useAppTheme } from '../../../theme/AppThemeContext';
import { AppHeader } from './AppHeader';
import { NotificationPanel } from './NotificationPanel';
import { ThemeSettingsSheet } from './ThemeSettingsSheet';

export function AuthenticatedHeader({ notifications = [], title }) {
  const { palette } = useAppTheme();
  const [dismissedIds, setDismissedIds] = useState([]);
  const [notificationsVisible, setNotificationsVisible] = useState(false);
  const [settingsVisible, setSettingsVisible] = useState(false);

  const visibleNotifications = useMemo(() => (
    notifications.filter(({ id }) => !dismissedIds.includes(id))
  ), [dismissedIds, notifications]);

  function toggleNotifications() {
    setSettingsVisible(false);
    setNotificationsVisible((visible) => !visible);
  }

  function openSettings() {
    setNotificationsVisible(false);
    setSettingsVisible(true);
  }

  function dismissNotification(notificationId) {
    setDismissedIds((currentIds) => (
      currentIds.includes(notificationId)
        ? currentIds
        : [...currentIds, notificationId]
    ));
  }

  return (
    <View style={styles.layer}>
      <AppHeader
        notificationCount={visibleNotifications.length}
        onNotifications={toggleNotifications}
        onSettings={openSettings}
        palette={palette}
        title={title}
      />
      {notificationsVisible ? (
        <NotificationPanel
          notifications={visibleNotifications}
          onDismiss={dismissNotification}
          palette={palette}
        />
      ) : null}
      <ThemeSettingsSheet
        onClose={() => setSettingsVisible(false)}
        visible={settingsVisible}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  layer: {
    position: 'relative',
    zIndex: 10,
  },
});
