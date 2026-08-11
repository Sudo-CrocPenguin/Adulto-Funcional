import { MaterialCommunityIcons } from '@expo/vector-icons';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { passwordStrength } from '../../domain/VaultCredential';

function formatDate(value) {
  if (!value) return 'Fecha no disponible';
  const [year, month, day] = value.split('-').map(Number);
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  }).format(new Date(year, month - 1, day));
}

function strengthStyle(strength, palette) {
  if (strength.level === 3) {
    return { backgroundColor: palette.successSoft, color: palette.success };
  }
  if (strength.level === 2) {
    return { backgroundColor: '#FFF3CD', color: '#9A6B00' };
  }
  if (strength.level === 1) {
    return { backgroundColor: palette.errorSoft, color: palette.error };
  }
  return { backgroundColor: palette.brandSoft, color: palette.brandDeep };
}

export function CredentialCard({
  credential,
  deleting,
  onDelete,
  onEdit,
  onToggleReveal,
  palette,
  revealedPassword,
  revealing,
}) {
  const strength = passwordStrength(revealedPassword);
  const badge = strengthStyle(strength, palette);
  const revealed = typeof revealedPassword === 'string';
  return (
    <View style={[styles.card, { backgroundColor: palette.surface, borderLeftColor: palette.brandSecondary }]}> 
      <View style={styles.header}>
        <Text numberOfLines={1} style={[styles.name, { color: palette.text }]}>{credential.applicationName}</Text>
        <View style={styles.actions}>
          <Pressable
            accessibilityLabel={`Editar ${credential.applicationName}`}
            accessibilityRole="button"
            disabled={deleting}
            hitSlop={7}
            onPress={onEdit}
            style={({ pressed }) => pressed && styles.pressed}
          >
            <MaterialCommunityIcons color={palette.navigationMuted} name="pencil" size={27} />
          </Pressable>
          <Pressable
            accessibilityLabel={`Eliminar ${credential.applicationName}`}
            accessibilityRole="button"
            disabled={deleting}
            hitSlop={7}
            onPress={onDelete}
            style={({ pressed }) => pressed && styles.pressed}
          >
            {deleting ? (
              <ActivityIndicator color={palette.error} size="small" />
            ) : (
              <MaterialCommunityIcons color={palette.navigationMuted} name="delete" size={28} />
            )}
          </Pressable>
        </View>
      </View>
      <View style={styles.secretRow}>
        <View style={[styles.strengthBadge, { backgroundColor: badge.backgroundColor }]}> 
          <Text style={[styles.strengthText, { color: badge.color }]}>{strength.label}</Text>
        </View>
        <View style={[styles.secretShell, { backgroundColor: palette.cardMuted }]}> 
          <Text
            accessibilityLabel={revealed ? 'Contraseña visible' : 'Contraseña oculta'}
            numberOfLines={1}
            selectable={revealed}
            style={[styles.secret, { color: palette.text }]}
          >
            {revealed ? revealedPassword : '••••••••'}
          </Text>
          <Pressable
            accessibilityLabel={revealed ? 'Ocultar contraseña' : 'Mostrar contraseña'}
            accessibilityRole="button"
            disabled={revealing}
            hitSlop={8}
            onPress={onToggleReveal}
            style={({ pressed }) => [styles.eyeButton, pressed && styles.pressed]}
          >
            {revealing ? (
              <ActivityIndicator color={palette.brandDeep} size="small" />
            ) : (
              <MaterialCommunityIcons color={palette.text} name={revealed ? 'eye-off-outline' : 'eye-outline'} size={24} />
            )}
          </Pressable>
        </View>
      </View>
      <Text style={[styles.date, { color: palette.navigationMuted }]}>Último cambio: {formatDate(credential.lastChangeDate)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  actions: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: 15,
    marginLeft: 10,
  },
  card: {
    borderLeftWidth: 5,
    borderRadius: 15,
    minHeight: 150,
    paddingBottom: 18,
    paddingHorizontal: 20,
    paddingTop: 15,
  },
  date: {
    fontSize: 12,
    marginLeft: 8,
    marginTop: 14,
  },
  eyeButton: {
    alignItems: 'center',
    height: 38,
    justifyContent: 'center',
    width: 42,
  },
  header: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  name: {
    flex: 1,
    fontSize: 20,
    fontWeight: '900',
  },
  pressed: {
    opacity: 0.58,
  },
  secret: {
    flex: 1,
    fontSize: 15,
    paddingLeft: 13,
  },
  secretRow: {
    alignItems: 'center',
    flexDirection: 'row',
    marginTop: 18,
  },
  secretShell: {
    alignItems: 'center',
    borderRadius: 15,
    flex: 1,
    flexDirection: 'row',
    height: 40,
    marginLeft: 12,
    overflow: 'hidden',
  },
  strengthBadge: {
    alignItems: 'center',
    borderRadius: 13,
    justifyContent: 'center',
    minHeight: 31,
    minWidth: 73,
    paddingHorizontal: 10,
  },
  strengthText: {
    fontSize: 12,
    fontWeight: '900',
  },
});
