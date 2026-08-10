import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors } from '../../../../shared/theme/tokens';

export function DashboardHeader({ notificationCount, onNotifications, onSettings }) {
  const badge = notificationCount > 99 ? '99+' : String(notificationCount);

  return (
    <View style={styles.header}>
      <Text accessibilityRole="header" style={styles.title}>Inicio</Text>
      <View style={styles.actions}>
        <Pressable
          accessibilityLabel={`Compromisos pendientes: ${notificationCount}`}
          accessibilityRole="button"
          hitSlop={8}
          onPress={onNotifications}
          style={({ pressed }) => [styles.iconButton, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons color={colors.warning} name="bell" size={38} />
          {notificationCount > 0 ? (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{badge}</Text>
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
            color={colors.navigationMuted}
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
    color: colors.surface,
    fontSize: 11,
    fontWeight: '800',
  },
  header: {
    alignItems: 'center',
    backgroundColor: colors.brandSoft,
    borderBottomColor: colors.brandDeep,
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
    color: colors.text,
    fontSize: 30,
    fontWeight: '800',
    letterSpacing: -0.4,
  },
});
