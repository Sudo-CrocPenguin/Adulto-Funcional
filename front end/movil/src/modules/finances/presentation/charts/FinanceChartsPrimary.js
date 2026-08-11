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

export function GroupedVerticalBarChart({ analytics, palette }) {
  const data = analytics.monthly;
  const maximum = maxValue(data.flatMap(({ expenses, income }) => [expenses, income]));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {[0, 1, 2, 3].map((index) => (
        <Line
          key={index}
          stroke={palette.divider}
          strokeWidth="1"
          x1="30"
          x2="310"
          y1={22 + index * 38}
          y2={22 + index * 38}
        />
      ))}
      {data.map((item, index) => {
        const groupX = 38 + index * 45;
        const incomeHeight = (item.income / maximum) * 118;
        const expenseHeight = (item.expenses / maximum) * 118;
        return (
          <G key={item.key}>
            <Rect fill={chartColor(0)} height={incomeHeight} rx="2" width="13" x={groupX} y={145 - incomeHeight} />
            <Rect fill={chartColor(3)} height={expenseHeight} rx="2" width="13" x={groupX + 15} y={145 - expenseHeight} />
            <SvgText fill={palette.textMuted} fontSize="10" textAnchor="middle" x={groupX + 14} y="164">{item.label}</SvgText>
          </G>
        );
      })}
      <Circle cx="108" cy="10" fill={chartColor(0)} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="116" y="13">Ingresos</SvgText>
      <Circle cx="181" cy="10" fill={chartColor(3)} r="4" />
      <SvgText fill={palette.textMuted} fontSize="9" x="189" y="13">Egresos</SvgText>
    </Svg>
  );
}

export function DonutExpenseChart({ analytics, palette }) {
  const data = analytics.expensesByCategory.slice(0, 6);
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return <ChartEmpty palette={palette} />;
  }
  const radius = 53;
  const circumference = Math.PI * 2 * radius;
  let offset = 0;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <G rotation="-90" origin="86,90">
        {data.map((item, index) => {
          const length = (item.value / total) * circumference;
          const element = (
            <Circle
              key={item.id}
              cx="86"
              cy="90"
              fill="none"
              r={radius}
              stroke={chartColor(index)}
              strokeDasharray={`${length} ${circumference - length}`}
              strokeDashoffset={-offset}
              strokeWidth="25"
            />
          );
          offset += length;
          return element;
        })}
      </G>
      <SvgText fill={palette.text} fontSize="13" fontWeight="700" textAnchor="middle" x="86" y="87">Egresos</SvgText>
      <SvgText fill={palette.brandDeep} fontSize="13" fontWeight="800" textAnchor="middle" x="86" y="105">{compactMoney(total)}</SvgText>
      {data.map((item, index) => (
        <G key={`legend:${item.id}`}>
          <Rect fill={chartColor(index)} height="9" rx="2" width="9" x="166" y={27 + index * 23} />
          <SvgText fill={palette.text} fontSize="10" x="181" y={35 + index * 23}>{item.label.slice(0, 16)}</SvgText>
          <SvgText fill={palette.textMuted} fontSize="9" textAnchor="end" x="310" y={35 + index * 23}>{Math.round((item.value / total) * 100)}%</SvgText>
        </G>
      ))}
    </Svg>
  );
}

export function CumulativeLineChart({ analytics, palette }) {
  const data = analytics.cumulative;
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  const values = data.map(({ balance }) => balance);
  const minimum = Math.min(...values, 0);
  const maximum = Math.max(...values, 1);
  const points = polylinePoints(values, { height: 120, maximum, minimum, width: 270, x: 34, y: 20 });
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {[0, 1, 2, 3].map((index) => (
        <Line key={index} stroke={palette.divider} x1="33" x2="308" y1={22 + index * 39} y2={22 + index * 39} />
      ))}
      <Polyline fill="none" points={pointsAttribute(points)} stroke={chartColor(2)} strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />
      {points.map((point, index) => (
        <Circle cx={point.x} cy={point.y} fill={palette.surface} key={data[index].id ?? index} r="3.5" stroke={chartColor(2)} strokeWidth="2" />
      ))}
      <SvgText fill={palette.textMuted} fontSize="9" x="33" y="165">{data[0]?.date?.slice(5)}</SvgText>
      <SvgText fill={palette.textMuted} fontSize="9" textAnchor="end" x="307" y="165">{data.at(-1)?.date?.slice(5)}</SvgText>
      <SvgText fill={palette.text} fontSize="11" fontWeight="700" textAnchor="end" x="307" y="15">Saldo {compactMoney(values.at(-1))}</SvgText>
    </Svg>
  );
}

export function HorizontalBudgetChart({ analytics, palette }) {
  const data = analytics.categoryComparison.slice(0, 5);
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  const maximum = maxValue(data.flatMap(({ budget, current }) => [budget, current]));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {data.map((item, index) => {
        const y = 17 + index * 32;
        return (
          <G key={item.id}>
            <SvgText fill={palette.text} fontSize="9" x="4" y={y + 9}>{item.label.slice(0, 12)}</SvgText>
            <Rect fill={palette.cardMuted} height="8" rx="4" width="188" x="112" y={y} />
            <Rect fill={chartColor(4)} height="8" rx="4" width={(item.budget / maximum) * 188} x="112" y={y} />
            <Rect fill={chartColor(6)} height="8" rx="4" width={(item.current / maximum) * 188} x="112" y={y + 11} />
          </G>
        );
      })}
      <Rect fill={chartColor(4)} height="8" rx="2" width="8" x="112" y="170" />
      <SvgText fill={palette.textMuted} fontSize="9" x="124" y="178">Referencia</SvgText>
      <Rect fill={chartColor(6)} height="8" rx="2" width="8" x="202" y="170" />
      <SvgText fill={palette.textMuted} fontSize="9" x="214" y="178">Real</SvgText>
    </Svg>
  );
}

export function WaterfallChart({ analytics, palette }) {
  const data = analytics.waterfall;
  const opening = data[0].value;
  const afterIncome = opening + data[1].value;
  const beforeExpense = afterIncome;
  const final = data[3].value;
  const values = [0, opening, afterIncome, final];
  const minimum = Math.min(...values);
  const maximum = Math.max(...values, 1);
  const range = maximum - minimum || 1;
  const y = (value) => 145 - ((value - minimum) / range) * 115;
  const bars = [
    { color: chartColor(3), end: opening, label: 'Inicial', start: 0 },
    { color: chartColor(4), end: afterIncome, label: 'Ingresos', start: opening },
    { color: chartColor(6), end: final, label: 'Egresos', start: beforeExpense },
    { color: chartColor(2), end: final, label: 'Final', start: 0 },
  ];
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      <Line stroke={palette.divider} x1="18" x2="307" y1={y(0)} y2={y(0)} />
      {bars.map((bar, index) => {
        const top = Math.min(y(bar.start), y(bar.end));
        const height = Math.max(Math.abs(y(bar.start) - y(bar.end)), 3);
        const x = 31 + index * 72;
        return (
          <G key={bar.label}>
            {index < bars.length - 1 ? (
              <Line stroke={palette.navigationMuted} strokeDasharray="3 3" x1={x + 43} x2={x + 72} y1={y(bar.end)} y2={y(bar.end)} />
            ) : null}
            <Rect fill={bar.color} height={height} rx="3" width="42" x={x} y={top} />
            <SvgText fill={palette.textMuted} fontSize="9" textAnchor="middle" x={x + 21} y="165">{bar.label}</SvgText>
          </G>
        );
      })}
    </Svg>
  );
}
