import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

function fromIso(value) {
  const [year, month, day] = String(value).split('-').map(Number);
  return new Date(year, month - 1, day);
}

function daysUntil(value, now) {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  return Math.ceil((fromIso(value).getTime() - today.getTime()) / 86_400_000);
}

function formatDate(value) {
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(fromIso(value)).replace('.', '');
}

function formatMoney(value) {
  return `$${new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(value)}`;
}

function urgency(days, active, palette) {
  if (!active) {
    return palette.navigationMuted;
  }
  if (days <= 0) {
    return palette.error;
  }
  if (days <= 7) {
    return palette.warning;
  }
  return palette.success;
}

export function FixedExpenseCard({ expense, isPaying, now, onPay, palette }) {
  const days = daysUntil(expense.nextDueDate, now);
  const accent = urgency(days, expense.isActive, palette);
  const daysLabel = days < 0
    ? `${Math.abs(days)} día${Math.abs(days) === 1 ? '' : 's'} vencido`
    : days === 0
      ? 'Vence hoy'
      : `${days} día${days === 1 ? '' : 's'}`;

  return (
    <View style={[styles.card, { backgroundColor: palette.surface, borderLeftColor: accent }]}> 
      <View style={styles.header}>
        <View style={styles.titleBlock}>
          <Text numberOfLines={1} style={[styles.name, { color: palette.text }]}>{expense.name}</Text>
          <Text numberOfLines={1} style={[styles.category, { color: palette.navigationMuted }]}> 
            {expense.categoryName.toLocaleUpperCase('es')}
          </Text>
        </View>
        <View style={[styles.statusTag, { backgroundColor: expense.isActive ? palette.successSoft : palette.cardMuted }]}> 
          <Text style={[styles.statusText, { color: expense.isActive ? palette.success : palette.textMuted }]}> 
            {expense.isActive ? 'Activo' : 'Inactivo'}
          </Text>
        </View>
      </View>

      <Text style={[styles.nextLabel, { color: palette.navigationMuted }]}>Próximo pago</Text>
      <View style={styles.dueRow}>
        <Text style={[styles.date, { color: palette.text }]}>{formatDate(expense.nextDueDate)}</Text>
        <Text style={[styles.days, { color: accent }]}>({daysLabel})</Text>
      </View>

      <View style={styles.footer}>
        <Text style={[styles.amount, { color: palette.text }]}>{formatMoney(expense.amount)}</Text>
        <View style={styles.tags}>
          <View style={[styles.frequencyTag, { backgroundColor: palette.cardMuted }]}> 
            <Text style={[styles.frequencyText, { color: palette.textMuted }]}>{expense.frequencyLabel}</Text>
          </View>
          {expense.isActive ? (
            <Pressable
              accessibilityLabel={`Registrar pago de ${expense.name}`}
              accessibilityRole="button"
              disabled={isPaying}
              onPress={onPay}
              style={({ pressed }) => [
                styles.payButton,
                { backgroundColor: palette.brandSecondary },
                pressed && { backgroundColor: palette.brandPressed },
                isPaying && styles.disabled,
              ]}
            >
              {isPaying ? (
                <ActivityIndicator color={palette.surfaceOnBrand} size="small" />
              ) : (
                <>
                  <MaterialCommunityIcons color={palette.surfaceOnBrand} name="cash-check" size={17} />
                  <Text style={[styles.payText, { color: palette.surfaceOnBrand }]}>Registrar pago</Text>
                </>
              )}
            </Pressable>
          ) : null}
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  amount: {
    fontSize: 25,
    fontWeight: '900',
  },
  card: {
    borderLeftWidth: 6,
    borderRadius: 7,
    minHeight: 207,
    paddingBottom: 15,
    paddingHorizontal: 23,
    paddingTop: 18,
  },
  category: {
    fontSize: 12,
    fontWeight: '800',
    marginTop: 2,
  },
  date: {
    fontSize: 18,
    fontWeight: '800',
  },
  days: {
    fontSize: 16,
    fontWeight: '900',
    marginLeft: 6,
  },
  disabled: {
    opacity: 0.6,
  },
  dueRow: {
    alignItems: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 3,
  },
  footer: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 15,
  },
  frequencyTag: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 5,
  },
  frequencyText: {
    fontSize: 11,
    fontWeight: '800',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  name: {
    fontSize: 22,
    fontWeight: '900',
  },
  nextLabel: {
    fontSize: 16,
    marginTop: 17,
  },
  payButton: {
    alignItems: 'center',
    borderRadius: 7,
    flexDirection: 'row',
    gap: 4,
    minHeight: 34,
    paddingHorizontal: 9,
  },
  payText: {
    fontSize: 11,
    fontWeight: '900',
  },
  statusTag: {
    alignSelf: 'flex-start',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 5,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '800',
  },
  tags: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 7,
  },
  titleBlock: {
    flex: 1,
    marginRight: 10,
  },
});
