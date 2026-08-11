import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

const MILESTONES = [7, 15, 23, 30];

export function CommitmentStreakCard({ days, palette }) {
  const progress = `${Math.min(days / 30, 1) * 100}%`;

  return (
    <View
      accessibilityLabel={`Racha de compromisos: ${days} días`}
      style={[styles.card, { backgroundColor: palette.brandDeep }]}
    >
      <Text style={[styles.title, { color: palette.surfaceOnBrand }]}>Racha de Compromisos</Text>
      <View style={styles.mainRow}>
        <View style={styles.daysBlock}>
          <Text style={[styles.daysNumber, { color: palette.surfaceOnBrand }]}>{days}</Text>
          <Text style={[styles.daysLabel, { color: palette.surfaceOnBrand }]}>Días Activos</Text>
        </View>
        <View style={styles.shieldWrap}>
          <MaterialCommunityIcons color={palette.warning} name="shield" size={94} />
          <View style={styles.shieldText}>
            <Text style={[styles.shieldNumber, { color: palette.surfaceOnBrand }]}>{days}</Text>
            <Text style={[styles.shieldDays, { color: palette.surfaceOnBrand }]}>Días</Text>
          </View>
        </View>
      </View>
      <View style={[styles.progressTrack, { backgroundColor: palette.cardMuted }]}>
        <View style={[styles.progressFill, { backgroundColor: palette.warning, width: progress }]} />
      </View>
      <View style={styles.milestones}>
        {MILESTONES.map((milestone) => (
          <Text
            key={milestone}
            style={[styles.milestone, { color: palette.surfaceOnBrand }]}
          >
            {milestone}
          </Text>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
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
    fontSize: 14,
    marginBottom: 9,
  },
  daysNumber: {
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
    borderRadius: 5,
    height: '100%',
    minWidth: 0,
  },
  progressTrack: {
    borderRadius: 5,
    height: 9,
    marginHorizontal: 19,
    overflow: 'hidden',
  },
  shieldDays: {
    fontSize: 16,
    lineHeight: 18,
  },
  shieldNumber: {
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
    fontSize: 16,
    fontWeight: '500',
  },
});
