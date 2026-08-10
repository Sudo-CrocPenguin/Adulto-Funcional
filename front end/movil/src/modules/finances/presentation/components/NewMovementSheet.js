import { useState } from 'react';
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
import { MOVEMENT_TYPES } from '../../domain/FinanceMovement';
import { MovementValidationError } from '../../domain/MovementDraft';
import {
  FinancialDateField,
  FinancialReadOnlyField,
  FinancialSelectField,
  FinancialTextField,
} from './FinancialFormControls';

const MOVEMENT_OPTIONS = Object.freeze([
  Object.freeze({ label: 'Ingreso', value: MOVEMENT_TYPES.income }),
  Object.freeze({ label: 'Egreso', value: MOVEMENT_TYPES.expense }),
]);

function initialForm() {
  return {
    amount: '',
    categoryId: '',
    description: '',
    movementDate: new Date(),
    movementType: MOVEMENT_TYPES.income,
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

export function NewMovementSheet({
  categories,
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

  const categoryOptions = categories.map(({ id, name }) => ({ label: name, value: id }));
  const selectedCategory = categoryOptions.find(({ value }) => value === form.categoryId)?.label;
  const selectedType = MOVEMENT_OPTIONS.find(({ value }) => value === form.movementType)?.label;

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
      if (error instanceof MovementValidationError) {
        setErrors(error.fieldErrors);
        setFeedback(error.message);
      } else if (error instanceof ApiError) {
        setErrors(backendFieldErrors(error));
        setFeedback(error.message);
      } else {
        setFeedback('No fue posible guardar el movimiento. Inténtalo nuevamente.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  function selectOption(value) {
    updateField(selector.field, value);
    setSelector(null);
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
            accessibilityLabel="Cerrar nuevo movimiento"
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
                <Text style={[styles.title, { color: palette.text }]}>Nuevo Movimiento</Text>
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

                  <FinancialSelectField
                    error={errors.movementType}
                    label="Movimiento"
                    onPress={() => setSelector({
                      field: 'movementType',
                      options: MOVEMENT_OPTIONS,
                      title: 'Selecciona el movimiento',
                    })}
                    palette={palette}
                    value={selectedType}
                  />
                  <FinancialDateField
                    error={errors.movementDate}
                    label="Fecha del movimiento"
                    onChange={(value) => updateField('movementDate', value)}
                    palette={palette}
                    value={form.movementDate}
                  />
                  <FinancialReadOnlyField
                    label="Fecha de registro"
                    palette={palette}
                    value="Automática al guardar"
                  />
                  <FinancialSelectField
                    error={errors.categoryId}
                    label="Clasificación"
                    onPress={() => setSelector({
                      field: 'categoryId',
                      options: categoryOptions,
                      title: 'Selecciona la clasificación',
                    })}
                    palette={palette}
                    value={selectedCategory}
                  />
                  {categories.length === 0 ? (
                    <Text style={[styles.categoriesEmpty, { color: palette.error }]}> 
                      No hay clasificaciones financieras disponibles en tu cuenta.
                    </Text>
                  ) : null}
                  <FinancialTextField
                    autoCapitalize="sentences"
                    error={errors.description}
                    label="Descripción"
                    maxLength={120}
                    onChangeText={(value) => updateField('description', value)}
                    palette={palette}
                    placeholder="Ej. Salario o supermercado"
                    returnKeyType="next"
                    value={form.description}
                  />
                  <FinancialTextField
                    error={errors.amount}
                    keyboardType="decimal-pad"
                    label="Monto"
                    maxLength={11}
                    onChangeText={(value) => updateField('amount', value)}
                    palette={palette}
                    placeholder="0,00"
                    returnKeyType="done"
                    value={form.amount}
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
                        <Text style={[styles.saveText, { color: palette.surfaceOnBrand }]}> 
                          Guardar
                        </Text>
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
    maxHeight: '91%',
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
