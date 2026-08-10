import DateTimePicker from '@react-native-community/datetimepicker';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useState } from 'react';
import {
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

function isoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function parseDate(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value.trim());
  if (!match) {
    return null;
  }
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year
    && date.getMonth() === month - 1
    && date.getDate() === day
    ? date
    : null;
}

function displayDate(date) {
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

export function FinancialFieldError({ message, palette }) {
  return message ? (
    <Text
      accessibilityLiveRegion="polite"
      style={[styles.error, { color: palette.error }]}
    >
      {message}
    </Text>
  ) : null;
}

export function FinancialTextField({
  error,
  label,
  onChangeText,
  palette,
  value,
  ...inputProps
}) {
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
          onChangeText={onChangeText}
          placeholderTextColor={palette.textMuted}
          style={[styles.input, { color: palette.text }]}
          value={value}
          {...inputProps}
        />
      </View>
      <FinancialFieldError message={error} palette={palette} />
    </View>
  );
}

export function FinancialSelectField({
  error,
  label,
  onPress,
  palette,
  placeholder = 'Seleccionar',
  value,
}) {
  return (
    <View style={styles.group}>
      <Text style={[styles.label, { color: palette.text }]}>{label}</Text>
      <Pressable
        accessibilityRole="button"
        onPress={onPress}
        style={({ pressed }) => [
          styles.shell,
          {
            backgroundColor: palette.fieldBackground,
            borderColor: error ? palette.error : palette.border,
          },
          pressed && styles.pressed,
        ]}
      >
        <Text
          numberOfLines={1}
          style={[styles.selectText, { color: value ? palette.text : palette.textMuted }]}
        >
          {value || placeholder}
        </Text>
        <MaterialCommunityIcons color={palette.text} name="chevron-down" size={31} />
      </Pressable>
      <FinancialFieldError message={error} palette={palette} />
    </View>
  );
}

export function FinancialDateField({
  error,
  label,
  minimumDate,
  onChange,
  palette,
  value,
}) {
  const [pickerVisible, setPickerVisible] = useState(false);

  function changeNative(event, nextValue) {
    setPickerVisible(false);
    if (event.type !== 'dismissed' && nextValue) {
      onChange(nextValue);
    }
  }

  return (
    <View style={styles.group}>
      <Text style={[styles.label, { color: palette.text }]}>{label}</Text>
      {Platform.OS === 'web' ? (
        <View style={[
          styles.shell,
          {
            backgroundColor: palette.fieldBackground,
            borderColor: error ? palette.error : palette.border,
          },
        ]}>
          <TextInput
            accessibilityLabel={label}
            defaultValue={isoDate(value)}
            key={`${label}:${isoDate(value)}`}
            onEndEditing={({ nativeEvent }) => {
              const date = parseDate(nativeEvent.text);
              if (date) {
                onChange(date);
              }
            }}
            style={[styles.input, { color: palette.text }]}
          />
        </View>
      ) : (
        <Pressable
          accessibilityRole="button"
          onPress={() => setPickerVisible(true)}
          style={({ pressed }) => [
            styles.shell,
            {
              backgroundColor: palette.fieldBackground,
              borderColor: error ? palette.error : palette.border,
            },
            pressed && styles.pressed,
          ]}
        >
          <Text style={[styles.selectText, { color: palette.text }]}>
            {displayDate(value)}
          </Text>
          <MaterialCommunityIcons
            color={palette.brandSecondary}
            name="calendar-month"
            size={25}
          />
        </Pressable>
      )}
      <FinancialFieldError message={error} palette={palette} />
      {pickerVisible && Platform.OS !== 'web' ? (
        <DateTimePicker
          display="default"
          minimumDate={minimumDate}
          mode="date"
          onChange={changeNative}
          value={value}
        />
      ) : null}
    </View>
  );
}

export function FinancialReadOnlyField({ label, palette, value }) {
  return (
    <View style={styles.group}>
      <Text style={[styles.label, { color: palette.text }]}>{label}</Text>
      <View style={[
        styles.shell,
        { backgroundColor: palette.cardMuted, borderColor: palette.border },
      ]}>
        <Text style={[styles.selectText, { color: palette.textMuted }]}>{value}</Text>
        <MaterialCommunityIcons
          color={palette.navigationMuted}
          name="lock-outline"
          size={21}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  error: {
    fontSize: 12,
    lineHeight: 16,
    marginHorizontal: 4,
    marginTop: 4,
  },
  group: {
    marginBottom: 14,
  },
  input: {
    flex: 1,
    fontSize: 16,
    minHeight: 47,
    paddingVertical: 8,
  },
  label: {
    fontSize: 17,
    fontWeight: '800',
    marginBottom: 6,
  },
  pressed: {
    opacity: 0.65,
  },
  selectText: {
    flex: 1,
    fontSize: 16,
    marginRight: 8,
  },
  shell: {
    alignItems: 'center',
    borderRadius: 17,
    borderWidth: 3,
    flexDirection: 'row',
    minHeight: 53,
    paddingHorizontal: 14,
  },
});
