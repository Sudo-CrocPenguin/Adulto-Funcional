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
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ApiError } from '../../../../core/http/ApiError';
import { OptionPickerModal } from '../../../../shared/presentation/components/OptionPickerModal';
import {
  FIXED_EXPENSE_FREQUENCIES,
  FIXED_EXPENSE_STATUSES,
} from '../../domain/FixedExpense';
import { FixedExpenseValidationError } from '../../domain/FixedExpenseDraft';
import {
  FinancialDateField,
  FinancialSelectField,
  FinancialTextField,
} from './FinancialFormControls';

function initialForm() {
  const nextDueDate = new Date();
  nextDueDate.setDate(nextDueDate.getDate() + 1);
  nextDueDate.setHours(0, 0, 0, 0);
  return {
    amount: '',
    categoryId: '',
    frequency: 'MONTHLY',
    name: '',
    nextDueDate,
    status: 'ACTIVE',
  };
}

function dateFromIso(value) {
  const [year, month, day] = String(value).split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return Number.isNaN(date.getTime()) ? initialForm().nextDueDate : date;
}

function formFromExpense(expense) {
  return {
    amount: String(expense.amount),
    categoryId: expense.category?.id ?? '',
    frequency: expense.frequency,
    name: expense.name,
    nextDueDate: dateFromIso(expense.nextDueDate),
    status: expense.status,
  };
}

function backendFieldErrors(error) {
  return (error.fieldErrors ?? []).reduce((result, fieldError) => {
    if (fieldError?.field && fieldError?.message) {
      result[fieldError.field] = fieldError.message;
    }
    return result;
  }, {});
}

export function NewFixedExpenseSheet({
  categories,
  expense,
  onClose,
  onSubmit,
  palette,
  visible,
}) {
  const [errors, setErrors] = useState({});
  const [feedback, setFeedback] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setSubmitting] = useState(false);
  const [selector, setSelector] = useState(null);
  const editing = Boolean(expense);
  const categoryOptions = categories.map(({ id, name }) => ({ label: name, value: id }));
  const categoryLabel = categoryOptions.find(({ value }) => value === form.categoryId)?.label;
  const frequencyLabel = FIXED_EXPENSE_FREQUENCIES.find(({ value }) => value === form.frequency)?.label;
  const statusLabel = FIXED_EXPENSE_STATUSES.find(({ value }) => value === form.status)?.label;

  useEffect(() => {
    if (!visible) {
      return;
    }
    setErrors({});
    setFeedback(null);
    setForm(expense ? formFromExpense(expense) : initialForm());
    setSelector(null);
  }, [expense, visible]);

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
      if (error instanceof FixedExpenseValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendFieldErrors(error));
        setFeedback(error.message);
      } else {
        setFeedback('No fue posible guardar el gasto fijo. Inténtalo nuevamente.');
      }
    } finally {
      setSubmitting(false);
    }
  }

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
            accessibilityLabel={editing ? 'Cerrar edición de gasto fijo' : 'Cerrar nuevo gasto fijo'}
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
                  {editing ? 'Editar Gasto' : 'Nuevo Gasto'}
                </Text>
                <ScrollView
                  contentContainerStyle={styles.formContent}
                  keyboardShouldPersistTaps="handled"
                  showsVerticalScrollIndicator={false}
                >
                  {feedback ? (
                    <Text
                      accessibilityLiveRegion="polite"
                      style={[styles.feedback, { backgroundColor: palette.errorSoft, color: palette.error }]}
                    >
                      {feedback}
                    </Text>
                  ) : null}
                  <FinancialTextField
                    autoCapitalize="sentences"
                    error={errors.name}
                    label="Nombre"
                    maxLength={20}
                    onChangeText={(value) => updateField('name', value)}
                    palette={palette}
                    placeholder="Ej. Gimnasio"
                    value={form.name}
                  />
                  <FinancialSelectField
                    error={errors.categoryId}
                    label="Clasificación"
                    onPress={() => openSelector('categoryId', 'Selecciona la clasificación', categoryOptions)}
                    palette={palette}
                    value={categoryLabel}
                  />
                  {categories.length === 0 ? (
                    <Text style={[styles.categoriesEmpty, { color: palette.error }]}> 
                      No hay clasificaciones financieras disponibles en tu cuenta.
                    </Text>
                  ) : null}
                  <FinancialSelectField
                    error={errors.frequency}
                    label="Frecuencia"
                    onPress={() => openSelector('frequency', 'Selecciona la frecuencia', FIXED_EXPENSE_FREQUENCIES)}
                    palette={palette}
                    value={frequencyLabel}
                  />
                  <FinancialDateField
                    error={errors.nextDueDate}
                    label="Fecha de corte"
                    minimumDate={new Date(Date.now() + 86_400_000)}
                    onChange={(value) => updateField('nextDueDate', value)}
                    palette={palette}
                    value={form.nextDueDate}
                  />
                  <FinancialTextField
                    error={errors.amount}
                    keyboardType="decimal-pad"
                    label="Monto"
                    maxLength={11}
                    onChangeText={(value) => updateField('amount', value)}
                    palette={palette}
                    placeholder="0,00"
                    value={form.amount}
                  />
                  <FinancialSelectField
                    error={errors.status}
                    label="Estado"
                    onPress={() => openSelector('status', 'Selecciona el estado', FIXED_EXPENSE_STATUSES)}
                    palette={palette}
                    value={statusLabel}
                  />

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
    marginTop: 8,
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
  formContent: {
    paddingBottom: 8,
  },
  handle: {
    alignSelf: 'center',
    borderRadius: 5,
    height: 7,
    marginBottom: 19,
    opacity: 0.8,
    width: 90,
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
  saveButton: {
    alignItems: 'center',
    borderRadius: 8,
    justifyContent: 'center',
    minHeight: 51,
    minWidth: 126,
    paddingHorizontal: 18,
  },
  saveText: {
    fontSize: 18,
    fontWeight: '800',
  },
  sheet: {
    borderTopLeftRadius: 26,
    borderTopRightRadius: 26,
    maxHeight: '92%',
    paddingHorizontal: 29,
    paddingTop: 14,
  },
  title: {
    fontSize: 24,
    fontWeight: '900',
    marginBottom: 23,
    textAlign: 'center',
  },
});
