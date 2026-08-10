import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

export function DashboardHeader({
  notificationCount,
  onNotifications,
  onSettings,
  palette,
}) {
  const badge = notificationCount > 99 ? '99+' : String(notificationCount);

  return (
    <View style={[
      styles.header,
      { backgroundColor: palette.brandSoft, borderBottomColor: palette.brandDeep },
    ]}>
      <Text accessibilityRole="header" style={[styles.title, { color: palette.text }]}>Inicio</Text>
      <View style={styles.actions}>
        <Pressable
          accessibilityLabel={`Notificaciones: ${notificationCount}`}
          accessibilityRole="button"
          hitSlop={8}
          onPress={onNotifications}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons color={palette.warning} name="bell" size={38} />
          {notificationCount > 0 ? (
            <View style={styles.badge}>
              <Text style={[styles.badgeText, { color: palette.surface }]}>{badge}</Text>
            </View>
          ) : null}
        </Pressable>
        <Pressable
          accessibilityLabel="Abrir configuración"
          accessibilityRole="button"
          hitSlop={8}
          onPress={onSettings}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons
            color={palette.navigationMuted}
            name="cog"
            size={40}
          />
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  actions: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 18,
  },
  badge: {
    alignItems: 'center',
    backgroundColor: '#EF3D3D',
    borderRadius: 12,
    height: 22,
    justifyContent: 'center',
    minWidth: 22,
    paddingHorizontal: 5,
    position: 'absolute',
    right: 1,
    top: 0,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '800',
  },
  header: {
    alignItems: 'center',
    borderBottomWidth: 5,
    flexDirection: 'row',
    justifyContent: 'space-between',
    minHeight: 94,
    paddingHorizontal: 28,
    paddingVertical: 17,
  },
  iconButton: {
    alignItems: 'center',
    height: 48,
    justifyContent: 'center',
    width: 48,
  },
  pressed: {
    opacity: 0.65,
  },
  title: {
    fontSize: 30,
    fontWeight: '800',
    letterSpacing: -0.4,
  },
});
