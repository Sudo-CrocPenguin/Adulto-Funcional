import { MaterialCommunityIcons } from '@expo/vector-icons';
import { forwardRef, useState } from 'react';
import {
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

import { colors } from '../../../../shared/theme/tokens';

export const FormField = forwardRef(function FormField(
  {
    error,
    label,
    onChangeText,
    onSubmitEditing,
    secure = false,
    value,
    ...inputProps
  },
  ref,
) {
  const [isPasswordVisible, setPasswordVisible] = useState(false);
  const inputIsSecure = secure && !isPasswordVisible;

  return (
    <View style={styles.group}>
      <Text style={styles.label}>{label}</Text>
      <View style={[styles.inputShell, error && styles.inputShellError]}>
        <TextInput
          ref={ref}
          accessibilityLabel={label}
          autoCapitalize="none"
          onChangeText={onChangeText}
          onSubmitEditing={onSubmitEditing}
          placeholderTextColor={colors.textMuted}
          secureTextEntry={inputIsSecure}
          selectionColor={colors.brand}
          style={styles.input}
          value={value}
          {...inputProps}
        />
        {secure ? (
          <Pressable
            accessibilityLabel={
              isPasswordVisible ? 'Ocultar contraseña' : 'Mostrar contraseña'
            }
            accessibilityRole="button"
            hitSlop={10}
            onPress={() => setPasswordVisible((current) => !current)}
            style={({ pressed }) => [
              styles.visibilityButton,
              pressed && styles.visibilityButtonPressed,
            ]}
          >
            <MaterialCommunityIcons
              color={colors.textMuted}
              name={isPasswordVisible ? 'eye-off-outline' : 'eye-outline'}
              size={27}
            />
          </Pressable>
        ) : null}
      </View>
      {error ? (
        <Text accessibilityLiveRegion="polite" style={styles.error}>
          {error}
        </Text>
      ) : null}
    </View>
  );
});

const styles = StyleSheet.create({
  group: {
    marginBottom: 13,
  },
  label: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '500',
    lineHeight: 23,
    marginBottom: 6,
  },
  inputShell: {
    alignItems: 'center',
    backgroundColor: colors.fieldBackground,
    borderColor: colors.border,
    borderRadius: 25,
    borderWidth: 3,
    flexDirection: 'row',
    minHeight: 51,
  },
  inputShellError: {
    borderColor: colors.error,
  },
  input: {
    color: colors.text,
    flex: 1,
    fontSize: 17,
    minHeight: 48,
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  visibilityButton: {
    alignItems: 'center',
    borderRadius: 22,
    height: 44,
    justifyContent: 'center',
    marginRight: 4,
    width: 44,
  },
  visibilityButtonPressed: {
    backgroundColor: colors.brandSoft,
  },
  error: {
    color: colors.error,
    fontSize: 13,
    lineHeight: 18,
    marginHorizontal: 5,
    marginTop: 4,
  },
});

