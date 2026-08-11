import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

function formatMoney(value) {
  return `$${new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(value)}`;
}

export function FinanceSummaryCard({
  analytics = false,
  kind,
  label,
  onAnalytics,
  palette,
  value,
}) {
  const isBalance = kind === 'balance';
  const isIncome = kind === 'income';
  const color = isBalance
    ? palette.surfaceOnBrand
    : isIncome
      ? palette.success
      : palette.navigationMuted;

  return (
    <View style={[
      styles.card,
      { backgroundColor: isBalance ? palette.brandSecondary : palette.surface },
    ]}>
      {!isBalance ? (
        <MaterialCommunityIcons
          color={color}
          name={isIncome ? 'arrow-up' : 'arrow-down'}
          size={56}
          style={styles.icon}
        />
      ) : null}
      <View style={styles.content}>
        <Text style={[styles.label, { color: isBalance ? color : palette.navigationMuted }]}>
          {label}
        </Text>
        <Text
          adjustsFontSizeToFit
          numberOfLines={1}
          style={[styles.value, { color }]}
        >
          {formatMoney(value)}
        </Text>
      </View>
      {analytics ? (
        <Pressable
          accessibilityLabel="Ver análisis financiero"
          accessibilityRole="button"
          hitSlop={8}
          onPress={onAnalytics}
          style={({ pressed }) => [styles.eyeButton, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons
            color={palette.surfaceOnBrand}
            name="eye-outline"
            size={33}
          />
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    alignItems: 'center',
    borderRadius: 6,
    flexDirection: 'row',
    minHeight: 112,
    paddingHorizontal: 21,
    paddingVertical: 15,
  },
  content: {
    flex: 1,
  },
  eyeButton: {
    alignItems: 'center',
    borderRadius: 24,
    height: 48,
    justifyContent: 'center',
    marginLeft: 8,
    width: 48,
  },
  icon: {
    marginRight: 18,
  },
  label: {
    fontSize: 20,
    fontWeight: '800',
  },
  pressed: {
    opacity: 0.6,
  },
  value: {
    fontSize: 27,
    fontWeight: '800',
    marginTop: 7,
  },
});
