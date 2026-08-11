import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

export function SummaryCard({ backgroundColor, icon, label, palette, value }) {
  return (
    <View
      accessibilityLabel={`${label}: ${value}`}
      style={[styles.card, { backgroundColor }]}
    >
      <MaterialCommunityIcons
        color={palette.surfaceOnBrand}
        name={icon}
        size={39}
      />
      <Text
        numberOfLines={2}
        style={[styles.label, { color: palette.surfaceOnBrand }]}
      >
        {label}
      </Text>
      <Text
        adjustsFontSizeToFit
        numberOfLines={1}
        style={[styles.value, { color: palette.surfaceOnBrand }]}
      >
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    alignItems: 'center',
    borderRadius: 6,
    flexBasis: '48.4%',
    flexGrow: 1,
    justifyContent: 'center',
    minHeight: 132,
    paddingHorizontal: 10,
    paddingVertical: 13,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 19,
    marginTop: 4,
    minHeight: 20,
    textAlign: 'center',
  },
  value: {
    fontSize: 23,
    fontWeight: '800',
    marginTop: 7,
    maxWidth: '100%',
    textAlign: 'center',
  },
});
