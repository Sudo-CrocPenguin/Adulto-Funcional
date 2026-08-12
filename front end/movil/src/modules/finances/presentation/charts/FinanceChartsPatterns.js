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
  polylinePoints,
} from './FinanceChartUtils';

const WIDTH = 320;
const HEIGHT = 180;

export function DailyHeatmapChart({ analytics, palette }) {
  const data = analytics.dailyExpenses;
  const maximum = maxValue(data.map(({ value }) => value));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {['L', 'M', 'M', 'J', 'V', 'S', 'D'].map((label, index) => (
        <SvgText fill={palette.textMuted} fontSize="9" key={`${label}:${index}`} textAnchor="middle" x={37 + index * 41} y="15">{label}</SvgText>
      ))}
      {data.map((item, index) => {
        const row = Math.floor(index / 7);
        const column = index % 7;
        const opacity = item.value ? 0.2 + (item.value / maximum) * 0.8 : 0.08;
        return (
          <G key={item.day}>
            <Rect fill={chartColor(2, palette)} height="26" opacity={opacity} rx="4" width="34" x={20 + column * 41} y={25 + row * 30} />
            <SvgText fill={item.value / maximum > 0.55 ? palette.surfaceOnBrand : palette.text} fontSize="9" textAnchor="middle" x={37 + column * 41} y={42 + row * 30}>{item.day}</SvgText>
          </G>
        );
      })}
    </Svg>
  );
}

export function SavingsSparklineChart({ analytics, palette }) {
  const values = analytics.savingsTrend.map(({ value }) => value);
  const minimum = Math.min(...values, 0);
  const maximum = Math.max(...values, 1);
  const points = polylinePoints(values, { height: 74, maximum, minimum, width: 260, x: 30, y: 50 });
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <SvgText fill={palette.textMuted} fontSize="12" x="30" y="27">Racha de ahorro</SvgText>
      <SvgText fill={palette.brandDeep} fontSize="30" fontWeight="900" x="30" y="55">{analytics.savingsStreak}</SvgText>
      <SvgText fill={palette.textMuted} fontSize="11" x="52" y="55">meses</SvgText>
      <Line stroke={palette.divider} x1="28" x2="293" y1="126" y2="126" />
      <Polyline fill="none" points={pointsAttribute(points)} stroke={chartColor(4, palette)} strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />
      {points.map((point, index) => (
        <Circle cx={point.x} cy={point.y} fill={chartColor(4, palette)} key={analytics.savingsTrend[index].key} r="3" />
      ))}
      {analytics.savingsTrend.map((item, index) => (
        <SvgText fill={palette.textMuted} fontSize="9" key={`label:${item.key}`} textAnchor="middle" x={points[index]?.x ?? 0} y="146">{item.label}</SvgText>
      ))}
    </Svg>
  );
}

export function SavingGaugeChart({ analytics, palette }) {
  const goal = analytics.savingGoal;
  const progress = goal > 0 ? Math.min(analytics.savingActual / goal, 1) : 0;
  const radius = 58;
  const circumference = Math.PI * 2 * radius;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <G rotation="-90" origin="160,87">
        <Circle cx="160" cy="87" fill="none" r={radius} stroke={palette.cardMuted} strokeWidth="18" />
        <Circle
          cx="160"
          cy="87"
          fill="none"
          r={radius}
          stroke={chartColor(4, palette)}
          strokeDasharray={`${circumference * progress} ${circumference * (1 - progress)}`}
          strokeLinecap="round"
          strokeWidth="18"
        />
      </G>
      <SvgText fill={palette.text} fontSize="28" fontWeight="900" textAnchor="middle" x="160" y="83">{Math.round(progress * 100)}%</SvgText>
      <SvgText fill={palette.textMuted} fontSize="11" textAnchor="middle" x="160" y="103">de la meta automática</SvgText>
      <SvgText fill={palette.brandDeep} fontSize="11" fontWeight="700" textAnchor="middle" x="160" y="166">{compactMoney(analytics.savingActual)} / {compactMoney(goal)}</SvgText>
    </Svg>
  );
}

export function StackedIncomeChart({ analytics, palette }) {
  const data = analytics.incomeBySource;
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return <ChartEmpty palette={palette} />;
  }
  let offset = 20;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <SvgText fill={palette.textMuted} fontSize="11" x="20" y="22">Ingresos del mes</SvgText>
      <SvgText fill={palette.text} fontSize="22" fontWeight="900" x="20" y="48">{compactMoney(total)}</SvgText>
      {data.map((item, index) => {
        const width = (item.value / total) * 280;
        const element = <Rect fill={chartColor(index, palette)} height="31" key={item.id} rx="4" width={width} x={offset} y="63" />;
        offset += width;
        return element;
      })}
      {data.slice(0, 4).map((item, index) => (
        <G key={`legend:${item.id}`}>
          <Circle cx={30 + (index % 2) * 145} cy={119 + Math.floor(index / 2) * 28} fill={chartColor(index, palette)} r="5" />
          <SvgText fill={palette.text} fontSize="10" x={41 + (index % 2) * 145} y={123 + Math.floor(index / 2) * 28}>{item.label.slice(0, 14)}</SvgText>
          <SvgText fill={palette.textMuted} fontSize="9" x={41 + (index % 2) * 145} y={135 + Math.floor(index / 2) * 28}>{Math.round((item.value / total) * 100)}%</SvgText>
        </G>
      ))}
    </Svg>
  );
}

export function MonthComparisonChart({ analytics, palette }) {
  const data = analytics.categoryComparison.slice(0, 5);
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  const maximum = maxValue(data.flatMap(({ current, previous }) => [current, previous]));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {data.map((item, index) => {
        const x = 34 + index * 56;
        const previousHeight = (item.previous / maximum) * 108;
        const currentHeight = (item.current / maximum) * 108;
        return (
          <G key={item.id}>
            <Rect fill={palette.navigationMuted} height={previousHeight} opacity="0.75" rx="2" width="18" x={x} y={137 - previousHeight} />
            <Rect fill={chartColor(2, palette)} height={currentHeight} rx="2" width="18" x={x + 20} y={137 - currentHeight} />
            <SvgText fill={palette.textMuted} fontSize="8" textAnchor="middle" x={x + 19} y="153">{item.label.slice(0, 7)}</SvgText>
          </G>
        );
      })}
      <Circle cx="93" cy="169" fill={palette.navigationMuted} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="102" y="172">Mes anterior</SvgText>
      <Circle cx="190" cy="169" fill={chartColor(2, palette)} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="199" y="172">Este mes</SvgText>
    </Svg>
  );
}
