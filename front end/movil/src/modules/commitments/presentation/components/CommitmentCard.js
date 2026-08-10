import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

const PRIORITY_STYLES = Object.freeze({
  Alta: Object.freeze({ accent: '#FF7477', pill: '#FFD6D7', text: '#E6383D' }),
  Baja: Object.freeze({ accent: '#37A921', pill: '#DCF7D3', text: '#2B9317' }),
  Media: Object.freeze({ accent: '#F4A93D', pill: '#F8E9B8', text: '#B78A18' }),
});

function formatDate(value) {
  if (!value) {
    return 'Sin fecha';
  }

  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'short',
  }).format(new Date(year, month - 1, day)).replace('.', '');
}

export function CommitmentCard({ commitment, palette }) {
  const priorityStyle = PRIORITY_STYLES[commitment.priority]
    ?? PRIORITY_STYLES.Media;

  return (
    <View
      accessibilityLabel={[
        commitment.title,
        commitment.categoryName,
        commitment.frequencyLabel,
        `prioridad ${commitment.priority}`,
        commitment.status,
      ].join(', ')}
      style={[
        styles.card,
        {
          backgroundColor: palette.surface,
          borderColor: palette.border,
          opacity: commitment.isCompleted ? 0.78 : 1,
        },
      ]}
    >
      <View style={[styles.accent, { backgroundColor: priorityStyle.accent }]} />
      <View style={styles.titleRow}>
        <Text
          numberOfLines={2}
          style={[
            styles.title,
            { color: palette.text },
            commitment.isCompleted && styles.completedTitle,
          ]}
        >
          {commitment.title}
        </Text>
        <Text style={[styles.date, { color: palette.navigationMuted }]}>
          {formatDate(commitment.eventDate)}
        </Text>
      </View>
      <View style={styles.metadata}>
        <Text numberOfLines={1} style={[styles.metadataText, { color: palette.navigationMuted }]}>
          {commitment.categoryName}
        </Text>
        <MaterialCommunityIcons
          color={palette.navigationMuted}
          name="sync"
          size={17}
        />
        <Text numberOfLines={1} style={[styles.metadataText, { color: palette.navigationMuted }]}>
          {commitment.frequencyLabel}
        </Text>
        <View style={[styles.priorityPill, { backgroundColor: priorityStyle.pill }]}>
          <Text style={[styles.priorityText, { color: priorityStyle.text }]}>
            {commitment.priority}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  accent: {
    bottom: 0,
    left: 0,
    position: 'absolute',
    top: 0,
    width: 8,
  },
  card: {
    borderRadius: 6,
    borderWidth: StyleSheet.hairlineWidth,
    minHeight: 116,
    overflow: 'hidden',
    paddingBottom: 18,
    paddingLeft: 27,
    paddingRight: 20,
    paddingTop: 18,
  },
  completedTitle: {
    textDecorationLine: 'line-through',
  },
  date: {
    fontSize: 12,
    marginLeft: 10,
  },
  metadata: {
    alignItems: 'center',
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 5,
    marginTop: 17,
  },
  metadataText: {
    fontSize: 13,
    fontWeight: '700',
    maxWidth: '34%',
  },
  priorityPill: {
    borderRadius: 15,
    marginLeft: 5,
    paddingHorizontal: 10,
    paddingVertical: 3,
  },
  priorityText: {
    fontSize: 13,
    fontWeight: '800',
  },
  title: {
    flex: 1,
    fontSize: 18,
    fontWeight: '800',
    lineHeight: 23,
  },
  titleRow: {
    alignItems: 'flex-start',
    flexDirection: 'row',
  },
});
