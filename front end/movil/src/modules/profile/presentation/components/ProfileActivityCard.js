import { StyleSheet, Text, View } from 'react-native';

function ActivityMetric({ accent, label, value, valueHint, palette }) {
  return (
    <View style={[styles.metric, { backgroundColor: palette.brandSoft }]}> 
      <Text style={[styles.value, { color: accent }]}>{value}</Text>
      <Text style={[styles.label, { color: palette.textMuted }]}>{label}</Text>
      {valueHint ? <Text style={[styles.hint, { color: palette.navigationMuted }]}>{valueHint}</Text> : null}
    </View>
  );
}

export function ProfileActivityCard({ activity, palette }) {
  const vaultLocked = activity.passwordsCount === null;
  return (
    <View style={[styles.card, { backgroundColor: palette.surface, shadowColor: palette.shadow }]}> 
      <Text style={[styles.title, { color: palette.brandDeep }]}>MI ACTIVIDAD</Text>
      <View style={styles.grid}>
        <ActivityMetric
          accent={palette.brandDeep}
          label="Compromisos completados"
          palette={palette}
          value={String(activity.completedCommitments)}
        />
        <ActivityMetric
          accent={palette.warning}
          label="Racha máxima (días)"
          palette={palette}
          value={String(activity.maximumStreakDays)}
        />
        <ActivityMetric
          accent={palette.brandDeep}
          label="Contraseñas guardadas"
          palette={palette}
          value={vaultLocked ? '—' : String(activity.passwordsCount)}
          valueHint={vaultLocked ? 'Bóveda bloqueada' : null}
        />
        <ActivityMetric
          accent={palette.brandDeep}
          label="Gastos fijos registrados"
          palette={palette}
          value={String(activity.fixedExpensesCount)}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 22,
    elevation: 3,
    marginHorizontal: 24,
    marginTop: -56,
    padding: 18,
    shadowOffset: { height: 3, width: 0 },
    shadowOpacity: 0.12,
    shadowRadius: 8,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginTop: 14,
  },
  hint: {
    fontSize: 10,
    fontWeight: '700',
    marginTop: 4,
    textAlign: 'center',
  },
  label: {
    fontSize: 14,
    fontWeight: '800',
    lineHeight: 19,
    marginTop: 8,
    textAlign: 'center',
  },
  metric: {
    alignItems: 'center',
    borderRadius: 16,
    justifyContent: 'center',
    minHeight: 126,
    padding: 12,
    width: '48.4%',
  },
  title: {
    fontSize: 18,
    fontWeight: '900',
  },
  value: {
    fontSize: 36,
    fontWeight: '900',
  },
});
