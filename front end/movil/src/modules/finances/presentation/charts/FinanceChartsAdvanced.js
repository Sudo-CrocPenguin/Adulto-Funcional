import Svg, {
  Circle,
  G,
  Line,
  Path,
  Polygon,
  Polyline,
  Rect,
  Text as SvgText,
} from 'react-native-svg';

import {
  chartColor,
  ChartEmpty,
  compactMoney,
  pointsAttribute,
  polylinePoints,
} from './FinanceChartUtils';

const WIDTH = 320;
const HEIGHT = 180;

function polarPoint(cx, cy, radius, angle) {
  const radians = ((angle - 90) * Math.PI) / 180;
  return { x: cx + radius * Math.cos(radians), y: cy + radius * Math.sin(radians) };
}

function pieSlicePath(cx, cy, radius, startAngle, endAngle) {
  const start = polarPoint(cx, cy, radius, endAngle);
  const end = polarPoint(cx, cy, radius, startAngle);
  const largeArc = endAngle - startAngle <= 180 ? 0 : 1;
  return `M ${cx} ${cy} L ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArc} 0 ${end.x} ${end.y} Z`;
}

export function FinancialRadarChart({ analytics, palette }) {
  const data = analytics.health;
  const center = { x: 160, y: 88 };
  const radius = 63;
  const axes = data.map((_, index) => polarPoint(
    center.x,
    center.y,
    radius,
    (index / data.length) * 360,
  ));
  const values = data.map((item, index) => polarPoint(
    center.x,
    center.y,
    radius * (item.value / 100),
    (index / data.length) * 360,
  ));
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {[0.33, 0.66, 1].map((scale) => (
        <Polygon
          fill="none"
          key={scale}
          points={pointsAttribute(axes.map((_, index) => polarPoint(center.x, center.y, radius * scale, (index / data.length) * 360)))}
          stroke={palette.divider}
        />
      ))}
      {axes.map((point, index) => (
        <G key={data[index].label}>
          <Line stroke={palette.divider} x1={center.x} x2={point.x} y1={center.y} y2={point.y} />
          <SvgText
            fill={palette.textMuted}
            fontSize="8"
            textAnchor={point.x < center.x - 5 ? 'end' : point.x > center.x + 5 ? 'start' : 'middle'}
            x={point.x + (point.x < center.x ? -5 : point.x > center.x ? 5 : 0)}
            y={point.y + (point.y < center.y ? -4 : 10)}
          >
            {data[index].label}
          </SvgText>
        </G>
      ))}
      <Polygon fill={chartColor(2, palette)} fillOpacity="0.25" points={pointsAttribute(values)} stroke={chartColor(2, palette)} strokeWidth="3" />
      {values.map((point, index) => <Circle cx={point.x} cy={point.y} fill={chartColor(2, palette)} key={data[index].label} r="3" />)}
    </Svg>
  );
}

export function ExpensePieChart({ analytics, palette }) {
  const data = analytics.expensesByCategory.slice(0, 7);
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return <ChartEmpty palette={palette} />;
  }
  let angle = 0;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {data.length === 1 ? <Circle cx="87" cy="90" fill={chartColor(0, palette)} r="62" /> : data.map((item, index) => {
        const start = angle;
        angle += (item.value / total) * 360;
        return <Path d={pieSlicePath(87, 90, 62, start, angle)} fill={chartColor(index, palette)} key={item.id} />;
      })}
      {data.map((item, index) => (
        <G key={`legend:${item.id}`}>
          <Rect fill={chartColor(index, palette)} height="9" rx="2" width="9" x="169" y={23 + index * 21} />
          <SvgText fill={palette.text} fontSize="9" x="183" y={31 + index * 21}>{item.label.slice(0, 15)}</SvgText>
          <SvgText fill={palette.textMuted} fontSize="9" textAnchor="end" x="309" y={31 + index * 21}>{Math.round((item.value / total) * 100)}%</SvgText>
        </G>
      ))}
    </Svg>
  );
}

export function BalanceAreaChart({ analytics, palette }) {
  const data = analytics.cumulative;
  if (data.length === 0) {
    return <ChartEmpty palette={palette} />;
  }
  const values = data.map(({ balance }) => balance);
  const points = polylinePoints(values, {
    height: 115,
    maximum: Math.max(...values, 1),
    minimum: Math.min(...values, 0),
    width: 272,
    x: 25,
    y: 21,
  });
  const areaPoints = [{ x: 25, y: 145 }, ...points, { x: 297, y: 145 }];
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {[0, 1, 2, 3].map((index) => <Line key={index} stroke={palette.divider} x1="24" x2="299" y1={25 + index * 40} y2={25 + index * 40} />)}
      <Polygon fill={chartColor(0, palette)} fillOpacity="0.24" points={pointsAttribute(areaPoints)} />
      <Polyline fill="none" points={pointsAttribute(points)} stroke={chartColor(0, palette)} strokeWidth="4" />
      <SvgText fill={palette.text} fontSize="11" fontWeight="700" x="25" y="167">Volumen acumulado</SvgText>
      <SvgText fill={palette.brandDeep} fontSize="12" fontWeight="900" textAnchor="end" x="297" y="167">{compactMoney(values[values.length - 1])}</SvgText>
    </Svg>
  );
}

export function MoneySankeyChart({ analytics, palette }) {
  const sources = analytics.incomeBySource.slice(0, 4);
  const targets = analytics.expensesByCategory.slice(0, 5);
  const sourceTotal = sources.reduce((sum, item) => sum + item.value, 0);
  const targetTotal = targets.reduce((sum, item) => sum + item.value, 0);
  if (!sourceTotal && !targetTotal) {
    return <ChartEmpty palette={palette} />;
  }
  const sourceMax = Math.max(sourceTotal, 1);
  const targetMax = Math.max(targetTotal, 1);
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {sources.map((source, sourceIndex) => targets.map((target, targetIndex) => {
        const sourceY = 32 + sourceIndex * 40;
        const targetY = 24 + targetIndex * 32;
        const share = (source.value / sourceMax) * (target.value / targetMax);
        return (
          <Path
            d={`M 76 ${sourceY} C 135 ${sourceY}, 186 ${targetY}, 246 ${targetY}`}
            fill="none"
            key={`${source.id}:${target.id}`}
            opacity={0.18 + share * 0.45}
            stroke={chartColor(targetIndex, palette)}
            strokeWidth={Math.max(1, share * 18)}
          />
        );
      }))}
      {sources.map((item, index) => (
        <G key={item.id}>
          <Rect fill={chartColor(index, palette)} height="24" rx="3" width="9" x="67" y={20 + index * 40} />
          <SvgText fill={palette.text} fontSize="8" textAnchor="end" x="62" y={31 + index * 40}>{item.label.slice(0, 9)}</SvgText>
        </G>
      ))}
      {targets.map((item, index) => (
        <G key={item.id}>
          <Rect fill={chartColor(index, palette)} height="20" rx="3" width="9" x="246" y={14 + index * 32} />
          <SvgText fill={palette.text} fontSize="8" x="260" y={27 + index * 32}>{item.label.slice(0, 9)}</SvgText>
        </G>
      ))}
      <SvgText fill={palette.textMuted} fontSize="9" x="5" y="174">Fuentes</SvgText>
      <SvgText fill={palette.textMuted} fontSize="9" textAnchor="end" x="315" y="174">Categorías</SvgText>
    </Svg>
  );
}

export function ExpenseTreemapChart({ analytics, palette }) {
  const data = analytics.expensesByCategory;
  const total = data.reduce((sum, item) => sum + item.value, 0);
  if (!total) {
    return <ChartEmpty palette={palette} />;
  }
  const primary = data.slice(0, 4);
  const rest = data.slice(4).reduce((sum, item) => sum + item.value, 0);
  const items = rest ? [...primary, { id: 'other', label: 'Otros', value: rest }] : primary;
  let x = 8;
  return (
    <Svg height={HEIGHT} viewBox={`0 0 ${WIDTH} ${HEIGHT}`} width="100%">
      {items.map((item, index) => {
        const width = Math.max((item.value / total) * 304, 1);
        const element = (
          <G key={item.id}>
            <Rect fill={chartColor(index, palette)} height="145" opacity="0.9" rx="3" width={Math.max(width - 3, 1)} x={x} y="9" />
            {width > 46 ? (
              <>
                <SvgText fill={palette.surfaceOnBrand} fontSize="10" fontWeight="800" textAnchor="middle" x={x + width / 2} y="75">{item.label.slice(0, 10)}</SvgText>
                <SvgText fill={palette.surfaceOnBrand} fontSize="9" textAnchor="middle" x={x + width / 2} y="91">{Math.round((item.value / total) * 100)}%</SvgText>
              </>
            ) : null}
          </G>
        );
        x += width;
        return element;
      })}
      <SvgText fill={palette.textMuted} fontSize="9" x="8" y="171">Tamaño proporcional al gasto del mes</SvgText>
    </Svg>
  );
}
