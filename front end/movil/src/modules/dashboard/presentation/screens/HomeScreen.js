import { StatusBar } from 'expo-status-bar';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '../../../../shared/theme/tokens';

export function HomeScreen() {
  return (
    <SafeAreaView edges={['top', 'left', 'right']} style={styles.safeArea}>
      <StatusBar backgroundColor={colors.brandSoft} style="dark" />
      <View style={styles.header}>
        <Text accessibilityRole="header" style={styles.title}>Inicio</Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    backgroundColor: colors.background,
    flex: 1,
  },
  header: {
    backgroundColor: colors.brandSoft,
    borderBottomColor: colors.brand,
    borderBottomWidth: 5,
    paddingHorizontal: 28,
    paddingVertical: 26,
  },
  title: {
    color: colors.text,
    fontSize: 30,
    fontWeight: '800',
  },
});
