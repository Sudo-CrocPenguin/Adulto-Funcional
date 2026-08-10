import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

function formatNotificationDate(value) {
  if (!value) {
    return null;
  }

  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
  }).format(new Date(year, month - 1, day)).replace('.', '');
}

function notificationMessage(notification) {
  if (notification.message) {
    return notification.message;
  }

  const date = formatNotificationDate(notification.date);
  return date
    ? `${notification.subject} · ${date}`
    : notification.subject;
}

export function NotificationPanel({ notifications, onDismiss, palette }) {
  return (
    <View
      accessibilityLabel="Panel de notificaciones"
      style={[
        styles.panel,
        {
          backgroundColor: palette.surface,
          borderColor: palette.border,
          shadowColor: palette.shadow,
        },
      ]}
    >
      <View style={styles.titleRow}>
        <MaterialCommunityIcons
          color={palette.brandDeep}
          name="bell"
          size={31}
        />
        <Text style={[styles.title, { color: palette.brandDeep }]}>
          Notificaciones
        </Text>
      </View>

      {notifications.length === 0 ? (
        <Text style={[styles.emptyMessage, { color: palette.textMuted }]}>
          No tienes avisos pendientes.
        </Text>
      ) : notifications.map((notification) => (
        <View
          key={notification.id}
          style={[
            styles.notification,
            {
              backgroundColor: palette.cardMuted,
              borderColor: palette.border,
            },
          ]}
        >
          <View style={[styles.accent, { backgroundColor: palette.brandDeep }]} />
          <MaterialCommunityIcons
            color={palette.text}
            name="information"
            size={22}
            style={styles.infoIcon}
          />
          <View style={styles.notificationContent}>
            <Text numberOfLines={1} style={[styles.notificationTitle, { color: palette.text }]}>
              {notification.title}
            </Text>
            <Text numberOfLines={2} style={[styles.notificationMessage, { color: palette.textMuted }]}>
              {notificationMessage(notification)}
            </Text>
          </View>
          <Pressable
            accessibilityLabel={`Descartar notificación de ${notification.title}`}
            accessibilityRole="button"
            hitSlop={8}
            onPress={() => onDismiss(notification.id)}
            style={({ pressed }) => [styles.dismissButton, pressed && styles.pressed]}
          >
            <MaterialCommunityIcons
              color={palette.navigationMuted}
              name="close"
              size={24}
            />
          </Pressable>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  accent: {
    bottom: 0,
    left: 0,
    position: 'absolute',
    top: 0,
    width: 6,
  },
  dismissButton: {
    alignItems: 'center',
    height: 34,
    justifyContent: 'center',
    width: 34,
  },
  emptyMessage: {
    fontSize: 14,
    lineHeight: 20,
    paddingBottom: 12,
    paddingHorizontal: 8,
    textAlign: 'center',
  },
  infoIcon: {
    marginLeft: 15,
    marginTop: 13,
  },
  notification: {
    borderRadius: 8,
    borderWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    marginTop: 11,
    minHeight: 76,
    overflow: 'hidden',
    paddingBottom: 10,
    paddingRight: 5,
  },
  notificationContent: {
    flex: 1,
    paddingHorizontal: 11,
    paddingTop: 11,
  },
  notificationMessage: {
    fontSize: 12,
    lineHeight: 17,
    marginTop: 4,
  },
  notificationTitle: {
    fontSize: 15,
    fontWeight: '800',
  },
  panel: {
    borderRadius: 10,
    borderWidth: StyleSheet.hairlineWidth,
    elevation: 12,
    maxWidth: 350,
    paddingBottom: 15,
    paddingHorizontal: 14,
    paddingTop: 15,
    position: 'absolute',
    right: 14,
    shadowOffset: { height: 5, width: 0 },
    shadowOpacity: 0.18,
    shadowRadius: 12,
    top: 84,
    width: '82%',
    zIndex: 20,
  },
  pressed: {
    opacity: 0.55,
  },
  title: {
    fontSize: 22,
    fontWeight: '800',
    marginLeft: 8,
  },
  titleRow: {
    alignItems: 'center',
    flexDirection: 'row',
    paddingHorizontal: 2,
  },
});
