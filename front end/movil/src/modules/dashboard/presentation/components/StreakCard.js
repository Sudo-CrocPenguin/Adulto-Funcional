import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

import { colors } from '../../../../shared/theme/tokens';

const MILESTONES = [7, 15, 23, 30];

export function StreakCard({ days }) {
  const progress = `${Math.min(days / 30, 1) * 100}%`;

  return (
    <View accessibilityLabel={`Racha de compromisos: ${days} días`} style={styles.card}>
      <Text style={styles.title}>Racha de Compromisos</Text>
      <View style={styles.mainRow}>
        <View style={styles.daysBlock}>
          <Text style={styles.daysNumber}>{days}</Text>
          <Text style={styles.daysLabel}>Días Activos</Text>
        </View>
        <View style={styles.shieldWrap}>
          <MaterialCommunityIcons color={colors.warning} name="shield" size={94} />
          <View style={styles.shieldText}>
            <Text style={styles.shieldNumber}>{days}</Text>
            <Text style={styles.shieldDays}>Días</Text>
          </View>
        </View>
      </View>
      <View style={styles.progressTrack}>
        <View style={[styles.progressFill, { width: progress }]} />
      </View>
      <View style={styles.milestones}>
        {MILESTONES.map((milestone) => (
          <Text key={milestone} style={styles.milestone}>{milestone}</Text>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.brandDeep,
    borderRadius: 6,
    marginTop: 18,
    paddingBottom: 16,
    paddingHorizontal: 16,
    paddingTop: 11,
  },
  daysBlock: {
    alignItems: 'flex-end',
    flexDirection: 'row',
    gap: 8,
  },
  daysLabel: {
    color: colors.surface,
    fontSize: 14,
    marginBottom: 9,
  },
  daysNumber: {
    color: colors.surface,
    fontSize: 48,
    fontWeight: '500',
    lineHeight: 55,
  },
  mainRow: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    minHeight: 75,
  },
  milestone: {
    color: colors.surface,
    fontSize: 10,
    opacity: 0.92,
    textAlign: 'center',
    width: 28,
  },
  milestones: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginHorizontal: 45,
    marginTop: 4,
  },
  progressFill: {
    backgroundColor: colors.warning,
    borderRadius: 5,
    height: '100%',
    minWidth: 0,
  },
  progressTrack: {
    backgroundColor: '#E9EFF6',
    borderRadius: 5,
    height: 9,
    marginHorizontal: 19,
    overflow: 'hidden',
  },
  shieldDays: {
    color: colors.surface,
    fontSize: 16,
    lineHeight: 18,
  },
  shieldNumber: {
    color: colors.surface,
    fontSize: 27,
    fontWeight: '800',
    lineHeight: 30,
  },
  shieldText: {
    alignItems: 'center',
    left: 0,
    position: 'absolute',
    right: 0,
    top: 24,
  },
  shieldWrap: {
    height: 89,
    marginRight: -2,
    marginTop: -17,
    position: 'relative',
    width: 94,
  },
  title: {
    color: colors.surface,
    fontSize: 16,
    fontWeight: '500',
  },
});
