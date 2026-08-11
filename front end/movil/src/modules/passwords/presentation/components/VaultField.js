import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

export function VaultField({
  error,
  label,
  onChangeText,
  palette,
  secure = false,
  value,
  ...inputProps
}) {
  const [visible, setVisible] = useState(false);
  return (
    <View style={styles.group}>
      <Text style={[styles.label, { color: palette.text }]}>{label}</Text>
      <View style={[
        styles.shell,
        {
          backgroundColor: palette.fieldBackground,
          borderColor: error ? palette.error : palette.border,
        },
      ]}>
        <TextInput
          accessibilityLabel={label}
          autoCapitalize="none"
          autoCorrect={false}
          onChangeText={onChangeText}
          placeholderTextColor={palette.textMuted}
          secureTextEntry={secure && !visible}
          selectionColor={palette.brandSecondary}
          style={[styles.input, { color: palette.text }]}
          value={value}
          {...inputProps}
        />
        {secure ? (
          <Pressable
            accessibilityLabel={visible ? `Ocultar ${label}` : `Mostrar ${label}`}
            accessibilityRole="button"
            hitSlop={8}
            onPress={() => setVisible((current) => !current)}
            style={({ pressed }) => [styles.eyeButton, pressed && styles.pressed]}
          >
            <MaterialCommunityIcons
              color={palette.textMuted}
              name={visible ? 'eye-off-outline' : 'eye-outline'}
              size={25}
            />
          </Pressable>
        ) : null}
      </View>
      {error ? (
        <Text accessibilityLiveRegion="polite" style={[styles.error, { color: palette.error }]}> 
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  error: {
    fontSize: 12,
    lineHeight: 17,
    marginHorizontal: 4,
    marginTop: 4,
  },
  eyeButton: {
    alignItems: 'center',
    height: 42,
    justifyContent: 'center',
    width: 42,
  },
  group: {
    marginBottom: 16,
  },
  input: {
    flex: 1,
    fontSize: 16,
    minHeight: 49,
    paddingVertical: 8,
  },
  label: {
    fontSize: 17,
    fontWeight: '700',
    marginBottom: 7,
  },
  pressed: {
    opacity: 0.62,
  },
  shell: {
    alignItems: 'center',
    borderRadius: 19,
    borderWidth: 3,
    flexDirection: 'row',
    minHeight: 55,
    paddingLeft: 14,
    paddingRight: 4,
  },
});
