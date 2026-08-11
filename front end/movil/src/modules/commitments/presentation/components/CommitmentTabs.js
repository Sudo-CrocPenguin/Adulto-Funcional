import { Pressable, StyleSheet, Text, View } from 'react-native';

import { COMMITMENT_FILTERS } from '../../domain/Commitment';

const TABS = Object.freeze([
  Object.freeze({ label: 'Todas', value: COMMITMENT_FILTERS.all }),
  Object.freeze({ label: 'Pendientes', value: COMMITMENT_FILTERS.pending }),
  Object.freeze({ label: 'Completadas', value: COMMITMENT_FILTERS.completed }),
]);

export function CommitmentTabs({ activeFilter, onSelect, palette }) {
  return (
    <View style={[styles.container, { backgroundColor: palette.surface }]}>
      {TABS.map((tab) => {
        const active = tab.value === activeFilter;
        return (
          <Pressable
            key={tab.value}
            accessibilityRole="tab"
            accessibilityState={{ selected: active }}
            onPress={() => onSelect(tab.value)}
            style={({ pressed }) => [styles.tab, pressed && styles.pressed]}
          >
            <Text
              adjustsFontSizeToFit
              numberOfLines={1}
              style={[
                styles.label,
                { color: active ? palette.link : palette.navigationMuted },
                active && styles.activeLabel,
              ]}
            >
              {tab.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  activeLabel: {
    fontWeight: '800',
  },
  container: {
    borderRadius: 21,
    flexDirection: 'row',
    marginTop: 20,
    minHeight: 68,
    overflow: 'hidden',
  },
  label: {
    fontSize: 18,
    fontWeight: '700',
    textAlign: 'center',
  },
  pressed: {
    opacity: 0.6,
  },
  tab: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 5,
  },
});
