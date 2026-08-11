import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export function OptionPickerModal({
  onClose,
  onSelect,
  options,
  palette,
  selectedValue,
  title,
  visible,
}) {
  return (
    <Modal
      animationType="fade"
      onRequestClose={onClose}
      statusBarTranslucent
      transparent
      visible={visible}
    >
      <View style={[styles.overlay, { backgroundColor: palette.overlay }]}>
        <Pressable
          accessibilityLabel="Cerrar opciones"
          onPress={onClose}
          style={StyleSheet.absoluteFill}
        />
        <View style={[styles.sheet, { backgroundColor: palette.surface }]}>
          <SafeAreaView edges={['bottom']}>
            <Text style={[styles.title, { color: palette.text }]}>{title}</Text>
            <ScrollView style={styles.options}>
              {options.map((option) => {
                const selected = option.value === selectedValue;
                return (
                  <Pressable
                    key={String(option.value)}
                    accessibilityRole="button"
                    accessibilityState={{ selected }}
                    onPress={() => onSelect(option.value)}
                    style={({ pressed }) => [
                      styles.option,
                      { borderBottomColor: palette.divider },
                      pressed && styles.pressed,
                    ]}
                  >
                    <Text style={[styles.optionText, { color: palette.text }]}>
                      {option.label}
                    </Text>
                    {selected ? (
                      <MaterialCommunityIcons
                        color={palette.brandSecondary}
                        name="check-circle"
                        size={24}
                      />
                    ) : null}
                  </Pressable>
                );
              })}
            </ScrollView>
          </SafeAreaView>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  option: {
    alignItems: 'center',
    borderBottomWidth: StyleSheet.hairlineWidth,
    flexDirection: 'row',
    justifyContent: 'space-between',
    minHeight: 55,
    paddingHorizontal: 6,
  },
  optionText: {
    flex: 1,
    fontSize: 17,
    marginRight: 12,
  },
  options: {
    maxHeight: 350,
    marginTop: 12,
  },
  overlay: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  pressed: {
    opacity: 0.6,
  },
  sheet: {
    borderTopLeftRadius: 22,
    borderTopRightRadius: 22,
    paddingHorizontal: 24,
    paddingTop: 22,
  },
  title: {
    fontSize: 22,
    fontWeight: '800',
    textAlign: 'center',
  },
});
