import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { OptionPickerModal } from '../../../../shared/presentation/components/OptionPickerModal';
import {
  FIXED_EXPENSE_FREQUENCIES,
  FIXED_EXPENSE_STATUSES,
} from '../../domain/FixedExpense';
import { FIXED_EXPENSE_TABS } from '../../domain/FixedExpenseCollection';

function FilterButton({ label, onPress, palette, selected }) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.filterButton,
        { backgroundColor: selected ? palette.brandSoft : palette.surface },
        pressed && styles.pressed,
      ]}
    >
      <Text numberOfLines={1} style={[styles.filterText, { color: selected ? palette.brandDeep : palette.text }]}> 
        {label}
      </Text>
      <MaterialCommunityIcons color={selected ? palette.brandDeep : palette.text} name="chevron-down" size={24} />
    </Pressable>
  );
}

export function FixedExpenseFilters({ categories, filters, onChange, palette }) {
  const [selector, setSelector] = useState(null);
  const frequencyOptions = [{ label: 'Todas las frecuencias', value: '' }, ...FIXED_EXPENSE_FREQUENCIES];
  const statusOptions = [{ label: 'Todos los estados', value: '' }, ...FIXED_EXPENSE_STATUSES];
  const categoryOptions = [
    { label: 'Todas las clasificaciones', value: '' },
    ...categories.map(({ id, name }) => ({ label: name, value: id })),
  ];
  const frequencyLabel = FIXED_EXPENSE_FREQUENCIES.find(({ value }) => value === filters.frequency)?.label;
  const statusLabel = FIXED_EXPENSE_STATUSES.find(({ value }) => value === filters.status)?.label;
  const categoryLabel = categoryOptions.find(({ value }) => value === filters.categoryId)?.label;

  function open(field, title, options) {
    setSelector({ field, options, title });
  }

  function select(value) {
    onChange({ ...filters, [selector.field]: value });
    setSelector(null);
  }

  return (
    <>
      <View style={styles.filterRow}>
        <FilterButton
          label={frequencyLabel ?? 'Frecuencias'}
          onPress={() => open('frequency', 'Filtrar por frecuencia', frequencyOptions)}
          palette={palette}
          selected={Boolean(filters.frequency)}
        />
        <FilterButton
          label={statusLabel ?? 'Estados'}
          onPress={() => open('status', 'Filtrar por estado', statusOptions)}
          palette={palette}
          selected={Boolean(filters.status)}
        />
        <FilterButton
          label={filters.categoryId ? categoryLabel : 'Clasificación'}
          onPress={() => open('categoryId', 'Filtrar por clasificación', categoryOptions)}
          palette={palette}
          selected={Boolean(filters.categoryId)}
        />
      </View>

      <View accessibilityRole="tablist" style={styles.tabs}>
        {[
          { label: 'Todos', value: FIXED_EXPENSE_TABS.all },
          { label: 'Próximos a vencer', value: FIXED_EXPENSE_TABS.dueSoon },
        ].map((tab) => {
          const active = filters.tab === tab.value;
          return (
            <Pressable
              key={tab.value}
              accessibilityRole="tab"
              accessibilityState={{ selected: active }}
              onPress={() => onChange({ ...filters, tab: tab.value })}
              style={[styles.tab, active && { borderBottomColor: palette.brandDeep }]}
            >
              <Text style={[styles.tabText, { color: active ? palette.brandDeep : palette.navigationMuted }]}> 
                {tab.label}
              </Text>
            </Pressable>
          );
        })}
      </View>

      <OptionPickerModal
        onClose={() => setSelector(null)}
        onSelect={select}
        options={selector?.options ?? []}
        palette={palette}
        selectedValue={selector ? filters[selector.field] : null}
        title={selector?.title ?? ''}
        visible={Boolean(selector)}
      />
    </>
  );
}

const styles = StyleSheet.create({
  filterButton: {
    alignItems: 'center',
    borderRadius: 5,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    minHeight: 53,
    paddingHorizontal: 10,
  },
  filterRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 22,
  },
  filterText: {
    flex: 1,
    fontSize: 13,
    marginRight: 2,
  },
  pressed: {
    opacity: 0.65,
  },
  tab: {
    alignItems: 'center',
    borderBottomWidth: 3,
    borderBottomColor: 'transparent',
    flex: 1,
    minHeight: 52,
    paddingBottom: 9,
    paddingTop: 6,
  },
  tabText: {
    fontSize: 18,
    fontWeight: '900',
    textAlign: 'center',
  },
  tabs: {
    flexDirection: 'row',
    marginBottom: 26,
  },
});
