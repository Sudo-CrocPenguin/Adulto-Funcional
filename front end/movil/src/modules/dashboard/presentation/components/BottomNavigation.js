import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '../../../../shared/theme/tokens';

const ITEMS = Object.freeze([
  { active: true, icon: 'home-variant-outline', label: 'Inicio' },
  { icon: 'clipboard-check-outline', label: 'Compromisos' },
  { icon: 'wallet-outline', label: 'Finanzas' },
  { icon: 'hand-coin-outline', label: 'Gastos Fijos' },
  { icon: 'lock-outline', label: 'Contraseñas' },
  { icon: 'account-circle-outline', label: 'Perfil' },
]);

export function BottomNavigation({ onSelect }) {
  return (
    <SafeAreaView edges={['bottom']} style={styles.safeArea}>
      <View accessibilityRole="tablist" style={styles.navigation}>
        {ITEMS.map((item) => {
          const color = item.active ? colors.brandDeep : colors.navigationMuted;
          return (
            <Pressable
              key={item.label}
              accessibilityRole="tab"
              accessibilityState={{ selected: Boolean(item.active) }}
              onPress={() => onSelect(item.label)}
              style={({ pressed }) => [styles.item, pressed && styles.pressed]}
            >
              <MaterialCommunityIcons color={color} name={item.icon} size={29} />
              <Text
                numberOfLines={1}
                style={[styles.label, { color }, item.active && styles.activeLabel]}
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
    backgroundColor: colors.surface,
    flexDirection: 'row',
    minHeight: 66,
  },
  pressed: {
    opacity: 0.6,
  },
  safeArea: {
    backgroundColor: colors.surface,
    borderTopColor: '#EDF0F3',
    borderTopWidth: StyleSheet.hairlineWidth,
  },
});
