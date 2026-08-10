import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors } from '../../../../shared/theme/tokens';

export function UpcomingCard({ date, emptyLabel, onPress, title, type }) {
  const hasItem = Boolean(title);

  return (
    <View accessibilityLabel={`${type}: ${title || emptyLabel}`} style={styles.card}>
      <View style={styles.accent} />
      <Text numberOfLines={1} style={styles.type}>{type}</Text>
      <Text numberOfLines={2} style={[styles.title, !hasItem && styles.emptyTitle]}>
        {title || emptyLabel}
      </Text>
      <View style={styles.footer}>
        <Text style={styles.date}>{date || '—'}</Text>
        <Pressable
          accessibilityRole="button"
          disabled={!hasItem}
          onPress={onPress}
          style={({ pressed }) => [
            styles.button,
            !hasItem && styles.buttonDisabled,
            pressed && styles.buttonPressed,
          ]}
        >
          <Text style={styles.buttonText}>Ver</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  accent: {
    backgroundColor: colors.brandDeep,
    bottom: 0,
    left: 0,
    position: 'absolute',
    top: 0,
    width: 7,
  },
  button: {
    backgroundColor: colors.brandDeep,
    borderRadius: 6,
    paddingHorizontal: 13,
    paddingVertical: 6,
  },
  buttonDisabled: {
    opacity: 0.35,
  },
  buttonPressed: {
    opacity: 0.75,
  },
  buttonText: {
    color: colors.surface,
    fontSize: 16,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: 6,
    flex: 1,
    minHeight: 126,
    overflow: 'hidden',
    paddingBottom: 10,
    paddingLeft: 28,
    paddingRight: 12,
    paddingTop: 10,
  },
  date: {
    color: '#969696',
    fontSize: 14,
  },
  emptyTitle: {
    color: colors.textMuted,
    fontWeight: '500',
  },
  footer: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 'auto',
  },
  title: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '800',
    lineHeight: 21,
    marginTop: 7,
  },
  type: {
    color: '#929292',
    fontSize: 15,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
});
