import { StyleSheet, Text, View } from 'react-native';

const SERIES = Object.freeze([
  { color: '#66D6D8', key: 'income', label: 'Ingresos' },
  { color: '#43B3CF', key: 'expenses', label: 'Egresos' },
  { color: '#3296BE', key: 'leisure', label: 'Ocio' },
  { color: '#35669D', key: 'savings', label: 'Ahorros' },
]);

function compactAmount(value) {
  return new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 1,
    notation: 'compact',
  }).format(value);
}

export function StatisticsCard({ palette, statistics }) {
  const maximum = Math.max(...SERIES.map(({ key }) => statistics[key]), 1);

  return (
    <View style={[styles.card, { backgroundColor: palette.surface }]}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: palette.text }]}>Reporte estadístico</Text>
        <Text style={[styles.period, { color: palette.text }]}>Últimos 3 meses⌄</Text>
      </View>
      <View style={styles.legend}>
        {SERIES.map((serie) => (
          <View key={serie.key} style={styles.legendItem}>
            <View style={[styles.legendDot, { backgroundColor: serie.color }]} />
            <Text style={[styles.legendText, { color: palette.textMuted }]}>{serie.label}</Text>
          </View>
        ))}
      </View>
      <View style={styles.chart}>
        <View pointerEvents="none" style={styles.grid}>
          {[0, 1, 2, 3].map((line) => (
            <View key={line} style={[styles.gridLine, { backgroundColor: palette.divider }]} />
          ))}
        </View>
        {SERIES.map((serie) => {
          const value = statistics[serie.key];
          const barHeight = value > 0 ? Math.max((value / maximum) * 122, 5) : 2;
          return (
            <View key={serie.key} style={styles.barColumn}>
              <Text numberOfLines={1} style={[styles.barValue, { color: palette.textMuted }]}>
                {compactAmount(value)}
              </Text>
              <View
                accessibilityLabel={`${serie.label}: ${value}`}
                style={[
                  styles.bar,
                  { backgroundColor: serie.color, height: barHeight },
                ]}
              />
              <Text style={[styles.barLabel, { color: palette.text }]}>{serie.label}</Text>
            </View>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  bar: {
    borderTopLeftRadius: 3,
    borderTopRightRadius: 3,
    width: 40,
  },
  barColumn: {
    alignItems: 'center',
    flex: 1,
    height: 168,
    justifyContent: 'flex-end',
    zIndex: 1,
  },
  barLabel: {
    fontSize: 11,
    marginTop: 6,
  },
  barValue: {
    fontSize: 9,
    marginBottom: 3,
    maxWidth: 64,
  },
  card: {
    borderRadius: 18,
    marginHorizontal: 28,
    marginTop: 13,
    paddingBottom: 14,
    paddingHorizontal: 16,
    paddingTop: 14,
  },
  chart: {
    flexDirection: 'row',
    height: 170,
    marginTop: 7,
    position: 'relative',
  },
  grid: {
    bottom: 24,
    justifyContent: 'space-between',
    left: 0,
    position: 'absolute',
    right: 0,
    top: 17,
  },
  gridLine: {
    height: StyleSheet.hairlineWidth,
  },
  header: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  legend: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 11,
    justifyContent: 'center',
    marginTop: 13,
  },
  legendDot: {
    borderRadius: 6,
    height: 11,
    marginRight: 5,
    width: 11,
  },
  legendItem: {
    alignItems: 'center',
    flexDirection: 'row',
  },
  legendText: {
    fontSize: 11,
  },
  period: {
    fontSize: 13,
  },
  title: {
    fontSize: 18,
    fontWeight: '800',
  },
});
