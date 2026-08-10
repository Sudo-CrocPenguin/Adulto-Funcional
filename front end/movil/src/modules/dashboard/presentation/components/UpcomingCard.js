import { Pressable, StyleSheet, Text, View } from 'react-native';

export function UpcomingCard({ date, emptyLabel, onPress, palette, title, type }) {
  const hasItem = Boolean(title);

  return (
    <View
      accessibilityLabel={`${type}: ${title || emptyLabel}`}
      style={[styles.card, { backgroundColor: palette.surface }]}
    >
      <View style={[styles.accent, { backgroundColor: palette.brandDeep }]} />
      <Text numberOfLines={1} style={[styles.type, { color: palette.navigationMuted }]}>{type}</Text>
      <Text
        numberOfLines={2}
        style={[
          styles.title,
          { color: palette.text },
          !hasItem && { color: palette.textMuted, fontWeight: '500' },
        ]}
      >
        {title || emptyLabel}
      </Text>
      <View style={styles.footer}>
        <Text style={[styles.date, { color: palette.navigationMuted }]}>{date || '—'}</Text>
        <Pressable
          accessibilityRole="button"
          disabled={!hasItem}
          onPress={onPress}
          style={({ pressed }) => [
            styles.button,
            { backgroundColor: palette.brandDeep },
            !hasItem && styles.buttonDisabled,
            pressed && styles.buttonPressed,
          ]}
        >
          <Text style={[styles.buttonText, { color: palette.surfaceOnBrand }]}>Ver</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  accent: {
    bottom: 0,
    left: 0,
    position: 'absolute',
    top: 0,
    width: 7,
  },
  button: {
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
    fontSize: 16,
  },
  card: {
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
  footer: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 'auto',
  },
  title: {
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
