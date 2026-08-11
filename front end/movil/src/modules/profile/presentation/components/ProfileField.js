import { StyleSheet, Text, TextInput, View } from 'react-native';

export function ProfileField({ error, label, onChangeText, palette, value, ...inputProps }) {
  return (
    <View style={styles.group}>
      <Text style={[styles.label, { color: palette.text }]}>{label}</Text>
      <TextInput
        accessibilityLabel={label}
        autoCorrect={false}
        onChangeText={onChangeText}
        placeholderTextColor={palette.textMuted}
        selectionColor={palette.brandSecondary}
        style={[
          styles.input,
          {
            backgroundColor: palette.fieldBackground,
            borderColor: error ? palette.error : palette.border,
            color: palette.text,
          },
        ]}
        value={value}
        {...inputProps}
      />
      {error ? <Text style={[styles.error, { color: palette.error }]}>{error}</Text> : null}
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
  group: {
    marginBottom: 14,
  },
  input: {
    borderRadius: 17,
    borderWidth: 3,
    fontSize: 16,
    minHeight: 54,
    paddingHorizontal: 15,
    paddingVertical: 9,
  },
  label: {
    fontSize: 16,
    fontWeight: '800',
    marginBottom: 6,
  },
});
