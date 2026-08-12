import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

export function AppHeader({
  notificationCount,
  onNotifications,
  onSettings,
  palette,
  title,
}) {
  const badge = notificationCount > 99 ? '99+' : String(notificationCount);

  return (
    <View style={[
      styles.header,
      {
        backgroundColor: palette.brandSoft,
        borderBottomColor: palette.brandDeep,
        elevation: palette.isNeon ? 8 : 0,
        shadowColor: palette.glow,
        shadowOpacity: palette.glowOpacity,
      },
    ]}>
      <Text
        accessibilityRole="header"
        adjustsFontSizeToFit
        minimumFontScale={0.68}
        numberOfLines={1}
        style={[styles.title, { color: palette.text }]}
      >
        {title}
      </Text>
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
            <View style={[styles.badge, { backgroundColor: palette.error }]}>
              <Text style={[styles.badgeText, { color: palette.surfaceOnBrand }]}>{badge}</Text>
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
    shadowOffset: { height: 0, width: 0 },
    shadowRadius: 12,
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
    flex: 1,
    fontSize: 30,
    fontWeight: '800',
    letterSpacing: -0.4,
    marginRight: 12,
  },
});
