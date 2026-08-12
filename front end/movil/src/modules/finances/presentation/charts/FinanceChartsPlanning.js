import { StyleSheet, Text, View } from 'react-native';
import Svg, {
  Circle,
  G,
  Line,
  Polyline,
  Rect,
  Text as SvgText,
} from 'react-native-svg';

import {
  chartColor,
  ChartEmpty,
  compactMoney,
  maxValue,
  pointsAttribute,
} from './FinanceChartUtils';

const WIDTH = 320;
const HEIGHT = 180;

export function ParetoExpenseChart({ analytics, palette }) {
  const data = analytics.expensesByCategory.slice(0, 7);
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return <ChartEmpty palette={palette} />;
  }
  let cumulative = 0;
  const percentages = data.map((item) => {
    cumulative += item.value;
    return (cumulative / total) * 100;
  });
  const maximum = maxValue(data.map(({ value }) => value));
  const points = percentages.map((value, index) => ({
    x: 37 + index * (250 / Math.max(data.length - 1, 1)),
    y: 137 - (value / 100) * 110,
  }));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <Line stroke={palette.divider} x1="27" x2="299" y1="137" y2="137" />
      {data.map((item, index) => {
        const height = (item.value / maximum) * 108;
        const x = 27 + index * (270 / data.length);
        return (
          <G key={item.id}>
            <Rect fill={chartColor(2, palette)} height={height} opacity="0.75" rx="2" width={Math.max(22, 250 / data.length - 7)} x={x} y={137 - height} />
            <SvgText fill={palette.textMuted} fontSize="8" textAnchor="middle" x={x + Math.max(22, 250 / data.length - 7) / 2} y="153">{item.label.slice(0, 5)}</SvgText>
          </G>
        );
      })}
      <Polyline fill="none" points={pointsAttribute(points)} stroke={chartColor(6, palette)} strokeWidth="3" />
      {points.map((point, index) => <Circle cx={point.x} cy={point.y} fill={chartColor(6, palette)} key={data[index].id} r="3" />)}
      <SvgText fill={chartColor(6, palette)} fontSize="9" textAnchor="end" x="305" y="18">100%</SvgText>
    </Svg>
  );
}

export function BudgetDumbbellChart({ analytics, palette }) {
  const data = analytics.categoryComparison.slice(0, 5);
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  const maximum = maxValue(data.flatMap(({ budget, current }) => [budget, current]));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {data.map((item, index) => {
        const y = 22 + index * 30;
        const budgetX = 113 + (item.budget / maximum) * 185;
        const currentX = 113 + (item.current / maximum) * 185;
        return (
          <G key={item.id}>
            <SvgText fill={palette.text} fontSize="9" x="5" y={y + 4}>{item.label.slice(0, 13)}</SvgText>
            <Line stroke={palette.navigationMuted} strokeWidth="3" x1={Math.min(budgetX, currentX)} x2={Math.max(budgetX, currentX)} y1={y} y2={y} />
            <Circle cx={budgetX} cy={y} fill={chartColor(4, palette)} r="5" />
            <Circle cx={currentX} cy={y} fill={chartColor(6, palette)} r="5" />
          </G>
        );
      })}
      <Circle cx="102" cy="169" fill={chartColor(4, palette)} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="111" y="172">Referencia</SvgText>
      <Circle cx="197" cy="169" fill={chartColor(6, palette)} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="206" y="172">Real</SvgText>
    </Svg>
  );
}

export function SavingBulletChart({ analytics, palette }) {
  const goal = analytics.savingGoal;
  const actual = analytics.savingActual;
  const scale = Math.max(goal * 1.25, actual, 1);
  const goalX = 27 + (goal / scale) * 268;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <SvgText fill={palette.text} fontSize="13" fontWeight="800" x="27" y="32">Ahorro del mes</SvgText>
      <SvgText fill={palette.brandDeep} fontSize="24" fontWeight="900" x="27" y="61">{compactMoney(actual)}</SvgText>
      <Rect fill={palette.cardMuted} height="34" rx="4" width="268" x="27" y="84" />
      <Rect fill={chartColor(4, palette)} height="18" rx="3" width={(actual / scale) * 268} x="27" y="92" />
      <Line stroke={palette.text} strokeWidth="4" x1={goalX} x2={goalX} y1="78" y2="124" />
      <SvgText fill={palette.textMuted} fontSize="10" textAnchor="middle" x={goalX} y="141">Meta {compactMoney(goal)}</SvgText>
      <SvgText fill={palette.textMuted} fontSize="10" x="27" y="162">Compacto: progreso real frente a meta automática</SvgText>
    </Svg>
  );
}

export function ExpenseCalendarChart({ analytics, palette }) {
  const data = analytics.dailyExpenses;
  const maximum = maxValue(data.map(({ value }) => value));
  const now = new Date();
  const firstOffset = (new Date(now.getFullYear(), now.getMonth(), 1).getDay() + 6) % 7;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {['L', 'M', 'M', 'J', 'V', 'S', 'D'].map((label, index) => (
        <SvgText fill={palette.textMuted} fontSize="9" key={`${label}:${index}`} textAnchor="middle" x={37 + index * 41} y="14">{label}</SvgText>
      ))}
      {data.map((item) => {
        const position = firstOffset + item.day - 1;
        const row = Math.floor(position / 7);
        const column = position % 7;
        const level = item.value / maximum;
        const fill = !item.value ? palette.cardMuted : level > 0.65 ? chartColor(6, palette) : level > 0.3 ? chartColor(5, palette) : chartColor(4, palette);
        return (
          <G key={item.day}>
            <Rect fill={fill} height="25" rx="5" width="34" x={20 + column * 41} y={23 + row * 29} />
            <SvgText fill={item.value && level > 0.65 ? palette.surfaceOnBrand : palette.text} fontSize="9" textAnchor="middle" x={37 + column * 41} y={39 + row * 29}>{item.day}</SvgText>
            {item.value ? <Circle cx={48 + column * 41} cy={28 + row * 29} fill={palette.surface} r="2" /> : null}
          </G>
        );
      })}
    </Svg>
  );
}

export function CategoryProgressList({ analytics, palette }) {
  const data = analytics.categoryComparison.slice(0, 6);
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  return (
    <View style={styles.progressList}>
      {data.map((item, index) => {
        const reference = item.budget || item.current || 1;
        const progress = Math.min(item.current / reference, 1);
        return (
          <View key={item.id} style={styles.progressItem}>
            <View style={styles.progressHeader}>
              <Text numberOfLines={1} style={[styles.progressLabel, { color: palette.text }]}>{item.label}</Text>
              <Text style={[styles.progressValue, { color: palette.textMuted }]}>{Math.round((item.current / reference) * 100)}%</Text>
            </View>
            <View style={[styles.track, { backgroundColor: palette.cardMuted }]}> 
              <View style={[styles.fill, { backgroundColor: chartColor(index, palette), width: `${progress * 100}%` }]} />
            </View>
            <Text style={[styles.progressMoney, { color: palette.textMuted }]}> 
              {compactMoney(item.current)} de {compactMoney(reference)}
            </Text>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  fill: {
    borderRadius: 4,
    height: 8,
  },
  progressHeader: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressItem: {
    marginBottom: 11,
  },
  progressLabel: {
    flex: 1,
    fontSize: 13,
    fontWeight: '700',
  },
  progressList: {
    minHeight: 175,
    paddingHorizontal: 5,
    paddingTop: 4,
  },
  progressMoney: {
    fontSize: 9,
    marginTop: 3,
    textAlign: 'right',
  },
  progressValue: {
    fontSize: 11,
    fontWeight: '800',
    marginLeft: 8,
  },
  track: {
    borderRadius: 4,
    height: 8,
    marginTop: 4,
    overflow: 'hidden',
  },
});
