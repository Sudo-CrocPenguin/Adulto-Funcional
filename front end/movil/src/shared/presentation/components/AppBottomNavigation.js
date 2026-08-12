import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

const ITEMS = Object.freeze([
  { icon: 'home-variant-outline', label: 'Inicio' },
  { icon: 'clipboard-check-outline', label: 'Compromisos' },
  { icon: 'wallet-outline', label: 'Finanzas' },
  { icon: 'hand-coin-outline', label: 'Gastos Fijos' },
  { icon: 'lock-outline', label: 'Contraseñas' },
  { icon: 'account-circle-outline', label: 'Perfil' },
]);

export function AppBottomNavigation({ activeItem, onSelect, palette }) {
  return (
    <SafeAreaView
      edges={['bottom']}
      style={[
        styles.safeArea,
        {
          backgroundColor: palette.surface,
          borderTopColor: palette.divider,
          elevation: palette.isNeon ? 10 : 0,
          shadowColor: palette.glow,
          shadowOpacity: palette.glowOpacity,
        },
      ]}
    >
      <View
        accessibilityRole="tablist"
        style={[styles.navigation, { backgroundColor: palette.surface }]}
      >
        {ITEMS.map((item) => {
          const active = item.label === activeItem;
          const color = active ? palette.brandDeep : palette.navigationMuted;
          return (
            <Pressable
              key={item.label}
              accessibilityRole="tab"
              accessibilityState={{ selected: active }}
              onPress={() => onSelect(item.label)}
              style={({ pressed }) => [styles.item, pressed && styles.pressed]}
            >
              <MaterialCommunityIcons color={color} name={item.icon} size={29} />
              <Text
                numberOfLines={1}
                style={[styles.label, { color }, active && styles.activeLabel]}
              >
                {item.label}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  activeLabel: {
    fontWeight: '800',
  },
  item: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    minWidth: 0,
    paddingHorizontal: 1,
    paddingVertical: 7,
  },
  label: {
    fontSize: 9,
    fontWeight: '700',
    marginTop: 2,
  },
  navigation: {
    flexDirection: 'row',
    minHeight: 66,
  },
  pressed: {
    opacity: 0.6,
  },
  safeArea: {
    borderTopWidth: StyleSheet.hairlineWidth,
    shadowOffset: { height: 0, width: 0 },
    shadowRadius: 12,
  },
});
