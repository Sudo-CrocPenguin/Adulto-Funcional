import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

function formatMoney(value) {
  return new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(value);
}

function formatDate(value) {
  if (!value) {
    return '—';
  }
  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
  }).format(new Date(year, month - 1, day)).replace('.', '');
}

export function MovementRow({ movement, palette }) {
  const color = movement.isIncome ? palette.success : palette.navigationMuted;
  return (
    <View style={[styles.row, { borderBottomColor: palette.divider }]}>
      <View style={[
        styles.iconBox,
        { backgroundColor: movement.isIncome ? palette.successSoft : palette.cardMuted },
      ]}>
        <MaterialCommunityIcons
          color={color}
          name={movement.isIncome ? 'arrow-up' : 'arrow-down'}
          size={36}
        />
      </View>
      <View style={styles.details}>
        <Text numberOfLines={1} style={[styles.title, { color: palette.text }]}>
          {movement.title}
        </Text>
        <Text numberOfLines={1} style={[styles.category, { color: palette.textMuted }]}>
          {movement.categoryName}
        </Text>
      </View>
      <View style={styles.amountBlock}>
        <Text
          adjustsFontSizeToFit
          numberOfLines={1}
          style={[styles.amount, { color }]}
        >
          {movement.isIncome ? '+' : '−'} ${formatMoney(movement.amount)}
        </Text>
        <Text style={[styles.date, { color: palette.text }]}>
          {formatDate(movement.movementDate)}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  amount: {
    fontSize: 17,
    fontWeight: '800',
  },
  amountBlock: {
    alignItems: 'flex-end',
    maxWidth: '37%',
  },
  category: {
    fontSize: 14,
    marginTop: 7,
  },
  date: {
    fontSize: 12,
    marginTop: 9,
  },
  details: {
    flex: 1,
    paddingHorizontal: 14,
  },
  iconBox: {
    alignItems: 'center',
    borderRadius: 7,
    height: 57,
    justifyContent: 'center',
    width: 57,
  },
  row: {
    alignItems: 'center',
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    minHeight: 94,
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '800',
  },
});
