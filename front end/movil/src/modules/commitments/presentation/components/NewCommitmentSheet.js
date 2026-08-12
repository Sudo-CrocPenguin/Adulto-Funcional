import DateTimePicker from '@react-native-community/datetimepicker';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Keyboard,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '../../../../core/http/ApiError';
import { COMMITMENT_FREQUENCIES, COMMITMENT_STATUSES } from '../../domain/Commitment';
import {
  COMMITMENT_PRIORITIES,
  COMMITMENT_REMINDERS,
  CommitmentValidationError,
} from '../../domain/CommitmentDraft';
import { OptionPickerModal } from '../../../../shared/presentation/components/OptionPickerModal';

function initialForm() {
  const eventDate = new Date();
  eventDate.setDate(eventDate.getDate() + 1);
  eventDate.setHours(0, 0, 0, 0);

  const startTime = new Date(eventDate);
  startTime.setHours(9, 0, 0, 0);
  const endTime = new Date(eventDate);
  endTime.setHours(10, 0, 0, 0);

  return {
    categoryId: '',
    endTime,
    eventDate,
    frequency: 0,
    priority: 'Media',
    reminderMinutes: 60,
    startTime,
    status: 'Pendiente',
    title: '',
  };
}

function parseLocalDateTime(value, fallback) {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(String(value));
  if (!match) {
    return fallback;
  }
  const [, year, month, day, hours, minutes] = match.map(Number);
  return new Date(year, month - 1, day, hours, minutes, 0, 0);
}

function formFromCommitment(commitment) {
  const defaults = initialForm();
  const startTime = parseLocalDateTime(commitment.startHour, defaults.startTime);
  const endTime = parseLocalDateTime(commitment.endHour, defaults.endTime);
  const reminder = parseLocalDateTime(commitment.reminder, null);
  const reminderMinutes = reminder
    ? Math.round((startTime.getTime() - reminder.getTime()) / 60_000)
    : defaults.reminderMinutes;

  return {
    categoryId: commitment.category?.id ?? '',
    endTime,
    eventDate: parseLocalDateTime(`${commitment.eventDate}T00:00`, defaults.eventDate),
    frequency: commitment.frequency,
    priority: commitment.priority,
    reminderMinutes: COMMITMENT_REMINDERS.some(({ value }) => value === reminderMinutes)
      ? reminderMinutes
      : defaults.reminderMinutes,
    startTime,
    status: commitment.status,
    title: commitment.title,
  };
}

function backendFieldErrors(error) {
  return error.fieldErrors.reduce((result, fieldError) => {
    if (fieldError?.field && fieldError?.message) {
      result[fieldError.field] = fieldError.message;
    }
    return result;
  }, {});
}

function dateLabel(date) {
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

function timeLabel(date) {
  return new Intl.DateTimeFormat('es-CO', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

function webDateValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function webTimeValue(date) {
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
}

function parseWebDate(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value.trim());
  if (!match) {
    return null;
  }
  const [, year, month, day] = match.map(Number);
  const date = new Date(year, month - 1, day);
  return date.getFullYear() === year
    && date.getMonth() === month - 1
    && date.getDate() === day
    ? date
    : null;
}

function parseWebTime(value, current) {
  const match = /^(\d{2}):(\d{2})$/.exec(value.trim());
  if (!match) {
    return null;
  }
  const [, hours, minutes] = match.map(Number);
  if (hours > 23 || minutes > 59) {
    return null;
  }
  const date = new Date(current);
  date.setHours(hours, minutes, 0, 0);
  return date;
}

function FieldError({ message, palette }) {
  return message ? (
    <Text accessibilityLiveRegion="polite" style={[styles.fieldError, { color: palette.error }]}>
      {message}
    </Text>
  ) : null;
}

function SelectField({ error, label, onPress, palette, placeholder, value }) {
  return (
    <View style={styles.fieldGroup}>
      <Text style={[styles.fieldLabel, { color: palette.text }]}>{label}</Text>
      <Pressable
        accessibilityRole="button"
        onPress={onPress}
        style={({ pressed }) => [
          styles.fieldShell,
          {
            backgroundColor: palette.fieldBackground,
            borderColor: error ? palette.error : palette.border,
          },
          pressed && styles.pressed,
        ]}
      >
        <Text
          numberOfLines={1}
          style={[styles.fieldValue, { color: value ? palette.text : palette.textMuted }]}
        >
          {value || placeholder}
        </Text>
        <MaterialCommunityIcons
          color={palette.text}
          name="chevron-down"
          size={31}
        />
      </Pressable>
      <FieldError message={error} palette={palette} />
    </View>
  );
}

function DateTimeField({
  error,
  label,
  mode,
  onOpen,
  onWebChange,
  palette,
  value,
}) {
  const displayValue = mode === 'date' ? dateLabel(value) : timeLabel(value);
  const webValue = mode === 'date' ? webDateValue(value) : webTimeValue(value);

  return (
    <View style={[styles.fieldGroup, mode === 'time' && styles.timeField]}>
      <Text style={[styles.fieldLabel, { color: palette.text }]}>{label}</Text>
      {Platform.OS === 'web' ? (
        <View style={[
          styles.fieldShell,
          {
            backgroundColor: palette.fieldBackground,
            borderColor: error ? palette.error : palette.border,
          },
        ]}>
          <TextInput
            accessibilityLabel={label}
            defaultValue={webValue}
            key={`${label}:${webValue}`}
            onEndEditing={({ nativeEvent }) => onWebChange(nativeEvent.text)}
            style={[styles.webDateInput, { color: palette.text }]}
          />
        </View>
      ) : (
        <Pressable
          accessibilityRole="button"
          onPress={onOpen}
          style={({ pressed }) => [
            styles.fieldShell,
            {
              backgroundColor: palette.fieldBackground,
              borderColor: error ? palette.error : palette.border,
            },
            pressed && styles.pressed,
          ]}
        >
          <Text style={[styles.fieldValue, { color: palette.text }]}>
            {displayValue}
          </Text>
          <MaterialCommunityIcons
            color={palette.brandSecondary}
            name={mode === 'date' ? 'calendar-month' : 'clock-outline'}
            size={25}
          />
        </Pressable>
      )}
      <FieldError message={error} palette={palette} />
    </View>
  );
}

export function NewCommitmentSheet({
  categories,
  commitment,
  onClose,
  onSubmit,
  palette,
  visible,
}) {
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setSubmitting] = useState(false);
  const [pickerField, setPickerField] = useState(null);
  const [selector, setSelector] = useState(null);
  const editing = Boolean(commitment);

  const categoryOptions = categories.map(({ id, name }) => ({
    label: name,
    value: id,
  }));
  const selectedCategory = categoryOptions.find(({ value }) => (
    value === form.categoryId
  ))?.label;
  const selectedFrequency = COMMITMENT_FREQUENCIES.find(({ value }) => (
    value === form.frequency
  ))?.label;
  const selectedReminder = COMMITMENT_REMINDERS.find(({ value }) => (
    value === form.reminderMinutes
  ))?.label;
  const selectedStatus = COMMITMENT_STATUSES.find(({ value }) => (
    value === form.status
  ))?.label;

  useEffect(() => {
    if (!visible) {
      return;
    }
    setErrors({});
    setFeedback(null);
    setForm(commitment ? formFromCommitment(commitment) : initialForm());
    setPickerField(null);
    setSelector(null);
  }, [commitment, visible]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => {
      if (!current[field]) {
        return current;
      }
      const next = { ...current };
      delete next[field];
      return next;
    });
    setFeedback(null);
  }

  function close() {
    if (isSubmitting) {
      return;
    }
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setPickerField(null);
    setSelector(null);
    onClose();
  }

  function openSelector(field, title, options) {
    setSelector({ field, options, title });
  }

  function selectOption(value) {
    updateField(selector.field, value);
    setSelector(null);
  }

  function changeNativeDate(event, value) {
    setPickerField(null);
    if (event.type !== 'dismissed' && value) {
      updateField(pickerField, value);
    }
  }

  function changeWebDate(field, value, mode) {
    const parsed = mode === 'date'
      ? parseWebDate(value)
      : parseWebTime(value, form[field]);
    if (parsed) {
      updateField(field, parsed);
    }
  }

  async function submit() {
    Keyboard.dismiss();
    setErrors({});
    setFeedback(null);
    setSubmitting(true);

    try {
      await onSubmit(form);
      setForm(initialForm());
      onClose();
    } catch (error) {
      if (error instanceof CommitmentValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendFieldErrors(error));
        setFeedback(error.message);
      } else {
        setFeedback('No fue posible guardar el compromiso. Inténtalo nuevamente.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  const pickerMode = pickerField === 'eventDate' ? 'date' : 'time';

  return (
    <>
      <Modal
        animationType="slide"
        onRequestClose={close}
        statusBarTranslucent
        transparent
        visible={visible}
      >
        <View style={[styles.overlay, { backgroundColor: palette.overlay }]}>
          <Pressable
            accessibilityLabel={editing ? 'Cerrar edición de compromiso' : 'Cerrar nuevo compromiso'}
            onPress={close}
            style={StyleSheet.absoluteFill}
          />
          <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            style={styles.keyboardArea}
          >
            <View style={[styles.sheet, { backgroundColor: palette.surface }]}>
              <SafeAreaView edges={['bottom']}>
                <View style={[styles.handle, { backgroundColor: palette.navigationMuted }]} />
                <Text style={[styles.title, { color: palette.text }]}>
                  {editing ? 'Editar Compromiso' : 'Nuevo Compromiso'}
                </Text>
                <ScrollView
                  contentContainerStyle={styles.formContent}
                  keyboardShouldPersistTaps="handled"
                  showsVerticalScrollIndicator={false}
                >
                  {feedback ? (
                    <Text
                      accessibilityLiveRegion="polite"
                      style={[
                        styles.feedback,
                        { backgroundColor: palette.errorSoft, color: palette.error },
                      ]}
                    >
                      {feedback}
                    </Text>
                  ) : null}

                  <View style={styles.fieldGroup}>
                    <Text style={[styles.fieldLabel, { color: palette.text }]}>Nombre</Text>
                    <View style={[
                      styles.fieldShell,
                      {
                        backgroundColor: palette.fieldBackground,
                        borderColor: errors.title ? palette.error : palette.border,
                      },
                    ]}>
                      <TextInput
                        accessibilityLabel="Nombre"
                        autoCapitalize="sentences"
                        maxLength={35}
                        onChangeText={(value) => updateField('title', value)}
                        placeholder="Ej. Preparar presentación"
                        placeholderTextColor={palette.textMuted}
                        returnKeyType="done"
                        style={[styles.textInput, { color: palette.text }]}
                        value={form.title}
                      />
                    </View>
                    <FieldError message={errors.title} palette={palette} />
                  </View>

                  <SelectField
                    error={errors.categoryId}
                    label="Categoría"
                    onPress={() => openSelector(
                      'categoryId',
                      'Selecciona una categoría',
                      categoryOptions,
                    )}
                    palette={palette}
                    placeholder="Seleccionar"
                    value={selectedCategory}
                  />
                  {categories.length === 0 ? (
                    <Text style={[styles.categoriesEmpty, { color: palette.error }]}>
                      No hay categorías de agenda disponibles en tu cuenta.
                    </Text>
                  ) : null}

                  <SelectField
                    error={errors.frequency}
                    label="Frecuencia"
                    onPress={() => openSelector(
                      'frequency',
                      'Selecciona la frecuencia',
                      COMMITMENT_FREQUENCIES,
                    )}
                    palette={palette}
                    placeholder="Seleccionar"
                    value={selectedFrequency}
                  />

                  <View style={styles.fieldGroup}>
                    <Text style={[styles.fieldLabel, { color: palette.text }]}>Prioridad</Text>
                    <View style={styles.priorityRow}>
                      {COMMITMENT_PRIORITIES.map((priority) => {
                        const active = form.priority === priority;
                        return (
                          <Pressable
                            key={priority}
                            accessibilityRole="button"
                            accessibilityState={{ selected: active }}
                            onPress={() => updateField('priority', priority)}
                            style={({ pressed }) => [
                              styles.priorityButton,
                              {
                                backgroundColor: active
                                  ? palette.brandSoft
                                  : palette.fieldBackground,
                                borderColor: active
                                  ? palette.brandSecondary
                                  : palette.border,
                              },
                              pressed && styles.pressed,
                            ]}
                          >
                            <Text style={[
                              styles.priorityLabel,
                              { color: active ? palette.brandDeep : palette.navigationMuted },
                            ]}>
                              {priority}
                            </Text>
                          </Pressable>
                        );
                      })}
                    </View>
                    <FieldError message={errors.priority} palette={palette} />
                  </View>

                  <DateTimeField
                    error={errors.eventDate}
                    label="Fecha"
                    mode="date"
                    onOpen={() => setPickerField('eventDate')}
                    onWebChange={(value) => changeWebDate('eventDate', value, 'date')}
                    palette={palette}
                    value={form.eventDate}
                  />

                  <View style={styles.timeRow}>
                    <DateTimeField
                      error={errors.startTime}
                      label="Hora de inicio"
                      mode="time"
                      onOpen={() => setPickerField('startTime')}
                      onWebChange={(value) => changeWebDate('startTime', value, 'time')}
                      palette={palette}
                      value={form.startTime}
                    />
                    <DateTimeField
                      error={errors.endTime}
                      label="Hora de fin"
                      mode="time"
                      onOpen={() => setPickerField('endTime')}
                      onWebChange={(value) => changeWebDate('endTime', value, 'time')}
                      palette={palette}
                      value={form.endTime}
                    />
                  </View>

                  <SelectField
                    error={errors.reminderMinutes || errors.reminder}
                    label="Recordatorio"
                    onPress={() => openSelector(
                      'reminderMinutes',
                      'Selecciona el recordatorio',
                      COMMITMENT_REMINDERS,
                    )}
                    palette={palette}
                    placeholder="Seleccionar"
                    value={selectedReminder}
                  />

                  {editing ? (
                    <SelectField
                      error={errors.status}
                      label="Estado"
                      onPress={() => openSelector(
                        'status',
                        'Selecciona el estado',
                        COMMITMENT_STATUSES,
                      )}
                      palette={palette}
                      placeholder="Seleccionar"
                      value={selectedStatus}
                    />
                  ) : null}

                  <View style={styles.actions}>
                    <Pressable
                      accessibilityRole="button"
                      disabled={isSubmitting}
                      onPress={close}
                      style={({ pressed }) => [
                        styles.cancelButton,
                        { backgroundColor: palette.cardMuted },
                        pressed && styles.pressed,
                      ]}
                    >
                      <Text style={[styles.cancelText, { color: palette.text }]}>Cancelar</Text>
                    </Pressable>
                    <Pressable
                      accessibilityRole="button"
                      disabled={isSubmitting || categories.length === 0}
                      onPress={submit}
                      style={({ pressed }) => [
                        styles.saveButton,
                        { backgroundColor: palette.brandSecondary },
                        pressed && { backgroundColor: palette.brandPressed },
                        (isSubmitting || categories.length === 0) && styles.disabled,
                      ]}
                    >
                      {isSubmitting ? (
                        <ActivityIndicator color={palette.surfaceOnBrand} size="small" />
                      ) : (
                        <Text style={[styles.saveText, { color: palette.surfaceOnBrand }]}>Guardar</Text>
                      )}
                    </Pressable>
                  </View>
                </ScrollView>

                {pickerField && Platform.OS !== 'web' ? (
                  <DateTimePicker
                    display="default"
                    minimumDate={pickerMode === 'date' ? new Date() : undefined}
                    mode={pickerMode}
                    onChange={changeNativeDate}
                    value={form[pickerField]}
                  />
                ) : null}
              </SafeAreaView>
            </View>
          </KeyboardAvoidingView>
        </View>
      </Modal>

      <OptionPickerModal
        onClose={() => setSelector(null)}
        onSelect={selectOption}
        options={selector?.options ?? []}
        palette={palette}
        selectedValue={selector ? form[selector.field] : null}
        title={selector?.title ?? ''}
        visible={Boolean(selector)}
      />
    </>
  );
}

const styles = StyleSheet.create({
  actions: {
    flexDirection: 'row',
    gap: 13,
    justifyContent: 'flex-end',
    marginTop: 12,
  },
  cancelButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 51,
    paddingHorizontal: 18,
  },
  cancelText: {
    fontSize: 18,
    fontWeight: '800',
  },
  categoriesEmpty: {
    fontSize: 13,
    marginBottom: 12,
    marginTop: -7,
  },
  disabled: {
    opacity: 0.55,
  },
  feedback: {
    borderRadius: 9,
    fontSize: 14,
    lineHeight: 19,
    marginBottom: 15,
    padding: 10,
    textAlign: 'center',
  },
  fieldError: {
    fontSize: 12,
    lineHeight: 16,
    marginHorizontal: 4,
    marginTop: 4,
  },
  fieldGroup: {
    marginBottom: 14,
  },
  fieldLabel: {
    fontSize: 17,
    fontWeight: '800',
    marginBottom: 6,
  },
  fieldShell: {
    alignItems: 'center',
    borderRadius: 17,
    borderWidth: 3,
    flexDirection: 'row',
    minHeight: 53,
    paddingHorizontal: 14,
  },
  fieldValue: {
    flex: 1,
    fontSize: 16,
    marginRight: 8,
  },
  formContent: {
    paddingBottom: 8,
  },
  handle: {
    alignSelf: 'center',
    borderRadius: 4,
    height: 6,
    marginBottom: 18,
    opacity: 0.75,
    width: 78,
  },
  keyboardArea: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  overlay: {
    flex: 1,
  },
  pressed: {
    opacity: 0.65,
  },
  priorityButton: {
    alignItems: 'center',
    borderRadius: 15,
    borderWidth: 3,
    flex: 1,
    justifyContent: 'center',
    minHeight: 49,
  },
  priorityLabel: {
    fontSize: 17,
    fontWeight: '800',
  },
  priorityRow: {
    flexDirection: 'row',
    gap: 11,
  },
  saveButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 51,
    minWidth: 120,
    paddingHorizontal: 20,
  },
  saveText: {
    fontSize: 18,
    fontWeight: '800',
  },
  sheet: {
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    maxHeight: '92%',
    paddingHorizontal: 28,
    paddingTop: 15,
  },
  textInput: {
    flex: 1,
    fontSize: 16,
    minHeight: 47,
    paddingVertical: 8,
  },
  timeField: {
    flex: 1,
  },
  timeRow: {
    flexDirection: 'row',
    gap: 12,
  },
  title: {
    fontSize: 27,
    fontWeight: '800',
    marginBottom: 22,
    textAlign: 'center',
  },
  webDateInput: {
    flex: 1,
    fontSize: 16,
    minHeight: 47,
  },
});
