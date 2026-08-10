import { StyleSheet, Text, View } from 'react-native';

export function FinanceChartCard({ children, index, palette, subtitle, title }) {
  return (
    <View style={[styles.card, { backgroundColor: palette.surface, shadowColor: palette.shadow }]}> 
      <View style={styles.heading}>
        <View style={[styles.number, { backgroundColor: palette.brandSoft }]}> 
          <Text style={[styles.numberText, { color: palette.brandDeep }]}>{index}</Text>
        </View>
        <View style={styles.headingText}>
          <Text style={[styles.title, { color: palette.text }]}>{title}</Text>
          <Text style={[styles.subtitle, { color: palette.textMuted }]}>{subtitle}</Text>
        </View>
      </View>
      <View style={styles.chart}>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 14,
    elevation: 2,
    padding: 15,
    shadowOffset: { height: 2, width: 0 },
    shadowOpacity: 0.09,
    shadowRadius: 5,
  },
  chart: {
    marginTop: 13,
    minHeight: 175,
  },
  heading: {
    alignItems: 'flex-start',
    flexDirection: 'row',
  },
  headingText: {
    flex: 1,
  },
  number: {
    alignItems: 'center',
    borderRadius: 14,
    height: 28,
    justifyContent: 'center',
    marginRight: 10,
    width: 28,
  },
  numberText: {
    fontSize: 12,
    fontWeight: '900',
  },
  subtitle: {
    fontSize: 12,
    lineHeight: 17,
    marginTop: 3,
  },
  title: {
    fontSize: 17,
    fontWeight: '900',
  },
});
