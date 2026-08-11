import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { useAppDependencies } from '../../../../composition/AppDependenciesContext';
import { ApiError } from '../../../../core/http/ApiError';
import { useAppSession } from '../../../../session/AppSessionContext';
import { useAppTheme } from '../../../../theme/AppThemeContext';
import { FinanceAnalytics } from '../../domain/FinanceAnalytics';
import {
  BalanceAreaChart,
  ExpensePieChart,
  ExpenseTreemapChart,
  FinancialRadarChart,
  MoneySankeyChart,
} from '../charts/FinanceChartsAdvanced';
import {
  CumulativeLineChart,
  DonutExpenseChart,
  GroupedVerticalBarChart,
  HorizontalBudgetChart,
  WaterfallChart,
} from '../charts/FinanceChartsPrimary';
import {
  DailyHeatmapChart,
  MonthComparisonChart,
  SavingGaugeChart,
  SavingsSparklineChart,
  StackedIncomeChart,
} from '../charts/FinanceChartsPatterns';
import {
  BudgetDumbbellChart,
  CategoryProgressList,
  ExpenseCalendarChart,
  ParetoExpenseChart,
  SavingBulletChart,
} from '../charts/FinanceChartsPlanning';
import { FinanceChartCard } from '../components/FinanceChartCard';

const CHARTS = Object.freeze([
  Object.freeze({
    component: GroupedVerticalBarChart,
    subtitle: 'Compara ingresos y egresos de los últimos seis meses.',
    title: 'Ingresos vs egresos por mes',
  }),
  Object.freeze({
    component: DonutExpenseChart,
    subtitle: 'Participación porcentual de cada clasificación de egreso.',
    title: 'Distribución de egresos',
  }),
  Object.freeze({
    component: CumulativeLineChart,
    subtitle: 'Evolución cronológica del balance después de cada movimiento.',
    title: 'Balance acumulado',
  }),
  Object.freeze({
    component: HorizontalBudgetChart,
    subtitle: 'El mes anterior funciona como referencia automática de presupuesto.',
    title: 'Referencia vs gasto real',
  }),
  Object.freeze({
    component: WaterfallChart,
    subtitle: 'Explica cómo ingresos y egresos transforman el saldo inicial.',
    title: 'Flujo del saldo',
  }),
  Object.freeze({
    component: DailyHeatmapChart,
    subtitle: 'Intensidad del gasto de cada día del mes actual.',
    title: 'Mapa de calor diario',
  }),
  Object.freeze({
    component: SavingsSparklineChart,
    subtitle: 'Tendencia compacta de los meses que terminaron en positivo.',
    title: 'Racha de ahorro',
  }),
  Object.freeze({
    component: SavingGaugeChart,
    subtitle: 'Avance frente a una meta automática del 20% de los ingresos.',
    title: 'Medidor de meta de ahorro',
  }),
  Object.freeze({
    component: StackedIncomeChart,
    subtitle: 'Composición de los ingresos del mes por fuente o clasificación.',
    title: 'Ingresos por fuente',
  }),
  Object.freeze({
    component: MonthComparisonChart,
    subtitle: 'Compara los egresos del mes actual con el inmediatamente anterior.',
    title: 'Este mes vs mes anterior',
  }),
  Object.freeze({
    component: FinancialRadarChart,
    subtitle: 'Lectura orientativa de ahorro, gasto, estabilidad, ingresos y liquidez.',
    title: 'Salud financiera',
  }),
  Object.freeze({
    component: ExpensePieChart,
    subtitle: 'Vista clásica de proporciones de gasto por clasificación.',
    title: 'Pastel de egresos',
  }),
  Object.freeze({
    component: BalanceAreaChart,
    subtitle: 'Refuerza visualmente el volumen del balance acumulado.',
    title: 'Área de balance',
  }),
  Object.freeze({
    component: MoneySankeyChart,
    subtitle: 'Flujo visual desde las fuentes de ingreso hacia los destinos del gasto.',
    title: 'Flujo del dinero',
  }),
  Object.freeze({
    component: ExpenseTreemapChart,
    subtitle: 'Rectángulos proporcionales para leer muchas categorías rápidamente.',
    title: 'Mapa de categorías',
  }),
  Object.freeze({
    component: ParetoExpenseChart,
    subtitle: 'Identifica las categorías que concentran la mayor parte del gasto.',
    title: 'Pareto de egresos',
  }),
  Object.freeze({
    component: BudgetDumbbellChart,
    subtitle: 'La distancia muestra la diferencia entre referencia y gasto real.',
    title: 'Mancuerna de presupuesto',
  }),
  Object.freeze({
    component: SavingBulletChart,
    subtitle: 'Indicador compacto del ahorro real frente a su objetivo calculado.',
    title: 'Progreso de ahorro',
  }),
  Object.freeze({
    component: ExpenseCalendarChart,
    subtitle: 'Calendario mensual con señal visual del nivel de gasto por fecha.',
    title: 'Calendario de gastos',
  }),
  Object.freeze({
    component: CategoryProgressList,
    subtitle: 'Uso por categoría frente a la referencia construida con el mes anterior.',
    title: 'Progreso por categoría',
  }),
]);

export function FinanceAnalyticsScreen({ navigation }) {
  const { loadFinances } = useAppDependencies();
  const { session } = useAppSession();
  const { isDark, palette } = useAppTheme();
  const [error, setError] = useState(null);
  const [isLoading, setLoading] = useState(true);
  const [isRefreshing, setRefreshing] = useState(false);
  const [ledger, setLedger] = useState(null);

  const load = useCallback(async ({ refresh = false } = {}) => {
    if (refresh) {
      setRefreshing(true);
    } else {
      setLoading(true);
    }
    setError(null);
    try {
      setLedger(await loadFinances.execute(session));
    } catch (loadError) {
      setError(loadError instanceof ApiError
        ? loadError.message
        : 'No fue posible preparar el análisis financiero.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [loadFinances, session]);

  useEffect(() => {
    load();
  }, [load]);

  const analytics = useMemo(
    () => ledger ? FinanceAnalytics.fromMovements(ledger.movements) : null,
    [ledger],
  );

  return (
    <SafeAreaView edges={['top', 'left', 'right', 'bottom']} style={[styles.safeArea, { backgroundColor: palette.background }]}> 
      <StatusBar backgroundColor={palette.brandSoft} style={isDark ? 'light' : 'dark'} />
      <View style={[styles.header, { backgroundColor: palette.brandSoft, borderBottomColor: palette.brandDeep }]}> 
        <Pressable
          accessibilityLabel="Volver a finanzas"
          accessibilityRole="button"
          hitSlop={8}
          onPress={() => navigation.goBack()}
          style={({ pressed }) => [styles.backButton, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons color={palette.brandDeep} name="arrow-left" size={29} />
        </Pressable>
        <View style={styles.headerText}>
          <Text style={[styles.headerTitle, { color: palette.text }]}>Análisis financiero</Text>
          <Text style={[styles.headerSubtitle, { color: palette.textMuted }]}>20 perspectivas de tus movimientos</Text>
        </View>
        <MaterialCommunityIcons color={palette.brandDeep} name="chart-box-outline" size={34} />
      </View>

      {isLoading && !analytics ? (
        <View style={styles.centerState}>
          <ActivityIndicator color={palette.brand} size="large" />
          <Text style={[styles.loadingText, { color: palette.textMuted }]}>Construyendo visualizaciones…</Text>
        </View>
      ) : error && !analytics ? (
        <View style={styles.centerState}>
          <MaterialCommunityIcons color={palette.navigationMuted} name="chart-bell-curve" size={56} />
          <Text style={[styles.errorText, { color: palette.text }]}>{error}</Text>
          <Pressable
            accessibilityRole="button"
            onPress={() => load()}
            style={({ pressed }) => [styles.retryButton, { backgroundColor: palette.brand }, pressed && styles.pressed]}
          >
            <Text style={[styles.retryText, { color: palette.surfaceOnBrand }]}>Reintentar</Text>
          </Pressable>
        </View>
      ) : (
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          refreshControl={(
            <RefreshControl
              colors={[palette.brand]}
              onRefresh={() => load({ refresh: true })}
              refreshing={isRefreshing}
              tintColor={palette.brand}
            />
          )}
          showsVerticalScrollIndicator={false}
        >
          <View style={[styles.information, { backgroundColor: palette.brandSoft }]}> 
            <MaterialCommunityIcons color={palette.brandDeep} name="information-outline" size={24} />
            <Text style={[styles.informationText, { color: palette.text }]}> 
              La referencia de presupuesto es el gasto del mes anterior y la meta de ahorro es el 20% de los ingresos del mes. Son cálculos automáticos, no límites guardados.
            </Text>
          </View>
          {error ? (
            <Text style={[styles.inlineError, { backgroundColor: palette.errorSoft, color: palette.error }]}>{error}</Text>
          ) : null}
          <View style={styles.cards}>
            {CHARTS.map((chart, index) => {
              const Chart = chart.component;
              return (
                <FinanceChartCard
                  index={index + 1}
                  key={chart.title}
                  palette={palette}
                  subtitle={chart.subtitle}
                  title={chart.title}
                >
                  <Chart analytics={analytics} palette={palette} />
                </FinanceChartCard>
              );
            })}
          </View>
        </ScrollView>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  backButton: {
    alignItems: 'center',
    borderRadius: 22,
    height: 44,
    justifyContent: 'center',
    marginRight: 7,
    width: 44,
  },
  cards: {
    gap: 15,
  },
  centerState: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 38,
  },
  errorText: {
    fontSize: 16,
    lineHeight: 23,
    marginTop: 14,
    textAlign: 'center',
  },
  header: {
    alignItems: 'center',
    borderBottomWidth: 5,
    flexDirection: 'row',
    minHeight: 92,
    paddingHorizontal: 14,
  },
  headerSubtitle: {
    fontSize: 11,
    marginTop: 2,
  },
  headerText: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 23,
    fontWeight: '900',
  },
  information: {
    alignItems: 'flex-start',
    borderRadius: 11,
    flexDirection: 'row',
    marginBottom: 15,
    padding: 13,
  },
  informationText: {
    flex: 1,
    fontSize: 12,
    lineHeight: 18,
    marginLeft: 9,
  },
  inlineError: {
    borderRadius: 8,
    fontSize: 13,
    marginBottom: 13,
    padding: 10,
    textAlign: 'center',
  },
  loadingText: {
    fontSize: 15,
    marginTop: 13,
  },
  pressed: {
    opacity: 0.62,
  },
  retryButton: {
    borderRadius: 8,
    marginTop: 20,
    paddingHorizontal: 23,
    paddingVertical: 12,
  },
  retryText: {
    fontSize: 16,
    fontWeight: '800',
  },
  safeArea: {
    flex: 1,
  },
  scrollContent: {
    padding: 13,
    paddingBottom: 28,
  },
});
