import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

function InformationRow({ icon, label, palette, value }) {
  return (
    <View style={[styles.row, { borderTopColor: palette.divider }]}> 
      <View style={[styles.iconBox, { backgroundColor: palette.cardMuted }]}> 
        <MaterialCommunityIcons color={palette.textMuted} name={icon} size={31} />
      </View>
      <View style={styles.textBlock}>
        <Text style={[styles.label, { color: palette.brandDeep }]}>{label}</Text>
        <Text numberOfLines={2} style={[styles.value, { color: palette.text }]}>{value || 'No registrado'}</Text>
      </View>
    </View>
  );
}

export function PersonalInformationCard({ palette, profile }) {
  return (
    <View style={[styles.card, { backgroundColor: palette.surface }]}> 
      <Text style={[styles.title, { color: palette.brandDeep }]}>INFORMACIÓN PERSONAL</Text>
      <InformationRow icon="account-outline" label="Nombre completo" palette={palette} value={profile.fullName} />
      <InformationRow icon="email-outline" label="Correo electrónico" palette={palette} value={profile.email} />
      <InformationRow icon="phone-outline" label="Teléfono" palette={palette} value={profile.phone} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 20,
    marginHorizontal: 24,
    marginTop: 16,
    overflow: 'hidden',
    paddingTop: 15,
  },
  iconBox: {
    alignItems: 'center',
    borderRadius: 8,
    height: 52,
    justifyContent: 'center',
    width: 52,
  },
  label: {
    fontSize: 13,
    fontWeight: '800',
  },
  row: {
    alignItems: 'center',
    borderTopWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    minHeight: 78,
    paddingHorizontal: 18,
    paddingVertical: 10,
  },
  textBlock: {
    flex: 1,
    marginLeft: 13,
  },
  title: {
    fontSize: 17,
    fontWeight: '900',
    paddingBottom: 14,
    paddingHorizontal: 22,
  },
  value: {
    fontSize: 16,
    fontWeight: '800',
    lineHeight: 21,
    marginTop: 2,
  },
});
