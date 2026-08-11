import { StyleSheet, Text, View } from 'react-native';

export const CHART_COLORS = Object.freeze([
  '#27C2C8',
  '#3EAEC9',
  '#308EBA',
  '#315D96',
  '#77C66E',
  '#F4A93D',
  '#F16D75',
  '#8B6FC7',
  '#5B8DEF',
  '#D58BBD',
]);

export function chartColor(index) {
  return CHART_COLORS[index % CHART_COLORS.length];
}

export function compactMoney(value) {
  const absolute = Math.abs(Number(value) || 0);
  if (absolute >= 1_000_000) {
    return `$${(Number(value) / 1_000_000).toFixed(1)}M`;
  }
  if (absolute >= 1_000) {
    return `$${(Number(value) / 1_000).toFixed(1)}k`;
  }
  return `$${Math.round(Number(value) || 0)}`;
}

export function maxValue(values, fallback = 1) {
  const maximum = Math.max(...values.map((value) => Math.abs(Number(value) || 0)), 0);
  return maximum || fallback;
}

export function polylinePoints(values, {
  height,
  maximum = Math.max(...values, 1),
  minimum = Math.min(...values, 0),
  width,
  x = 0,
  y = 0,
}) {
  const range = maximum - minimum || 1;
  const divisor = Math.max(values.length - 1, 1);
  return values.map((value, index) => ({
    x: x + (index / divisor) * width,
    y: y + height - ((value - minimum) / range) * height,
  }));
}

export function pointsAttribute(points) {
  return points.map((point) => `${point.x},${point.y}`).join(' ');
}

export function ChartEmpty({ message = 'Aún no hay datos suficientes', palette }) {
  return (
    <View style={styles.empty}>
      <Text style={[styles.emptyText, { color: palette.textMuted }]}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  empty: {
    alignItems: 'center',
    height: 175,
    justifyContent: 'center',
  },
  emptyText: {
    fontSize: 13,
    textAlign: 'center',
  },
});
