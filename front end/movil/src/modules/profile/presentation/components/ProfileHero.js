import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

function membershipLabel(value) {
  if (!value) return 'Fecha de registro no disponible';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Fecha de registro no disponible';
  return `Miembro desde ${new Intl.DateTimeFormat('es-CO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date)}`;
}

export function ProfileHero({ onEdit, palette, profile }) {
  return (
    <View style={[styles.hero, { backgroundColor: palette.brandDeep }]}> 
      <View style={[styles.avatar, { backgroundColor: palette.brandSecondary, borderColor: '#8FB3D8' }]}> 
        <Text adjustsFontSizeToFit numberOfLines={1} style={[styles.initials, { color: palette.surfaceOnBrand }]}> 
          {profile.initials}
        </Text>
        <Pressable
          accessibilityLabel="Editar perfil"
          accessibilityRole="button"
          hitSlop={9}
          onPress={onEdit}
          style={({ pressed }) => [styles.avatarEdit, { backgroundColor: palette.surface }, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons color={palette.brandDeep} name="pencil-outline" size={18} />
        </Pressable>
      </View>
      <Text numberOfLines={2} style={[styles.name, { color: palette.surfaceOnBrand }]}>{profile.fullName}</Text>
      <Text numberOfLines={1} style={styles.email}>{profile.email}</Text>
      <View style={[styles.membership, { backgroundColor: palette.brandSoft }]}> 
        <Text numberOfLines={1} style={[styles.membershipText, { color: palette.brandDeep }]}>{membershipLabel(profile.createdAt)}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  avatar: {
    alignItems: 'center',
    borderRadius: 72,
    borderWidth: 3,
    height: 144,
    justifyContent: 'center',
    position: 'relative',
    width: 144,
  },
  avatarEdit: {
    alignItems: 'center',
    borderRadius: 18,
    bottom: 7,
    height: 36,
    justifyContent: 'center',
    position: 'absolute',
    right: -2,
    width: 36,
  },
  email: {
    color: '#D4E0EF',
    fontSize: 14,
    marginTop: 3,
    maxWidth: '88%',
  },
  hero: {
    alignItems: 'center',
    minHeight: 350,
    paddingBottom: 74,
    paddingHorizontal: 22,
    paddingTop: 38,
  },
  initials: {
    fontSize: 62,
    fontWeight: '800',
    maxWidth: 110,
  },
  membership: {
    borderRadius: 12,
    marginTop: 11,
    maxWidth: '90%',
    paddingHorizontal: 15,
    paddingVertical: 4,
  },
  membershipText: {
    fontSize: 11,
    fontWeight: '800',
    textAlign: 'center',
  },
  name: {
    fontSize: 22,
    fontWeight: '900',
    marginTop: 15,
    textAlign: 'center',
  },
  pressed: {
    opacity: 0.62,
  },
});
