import { Image, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '../../../../shared/theme/tokens';

export function BrandHero() {
  return (
    <SafeAreaView edges={['top']} style={styles.hero}>
      <View style={styles.content}>
        <Image
          accessibilityLabel="Logotipo de Adulto Funcional"
          accessibilityRole="image"
          source={require('../../../../../assets/icon.png')}
          style={styles.logo}
        />

        <Text style={styles.title}>Adulto Funcional</Text>
        <Text style={styles.subtitle}>
          Organiza tu vida con control y seguridad
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  hero: {
    backgroundColor: colors.brand,
    borderBottomLeftRadius: 8,
    borderBottomRightRadius: 8,
  },
  content: {
    alignItems: 'center',
    paddingBottom: 45,
    paddingHorizontal: 20,
    paddingTop: 14,
  },
  logo: {
    backgroundColor: colors.surface,
    borderRadius: 44,
    height: 88,
    resizeMode: 'contain',
    width: 88,
  },
  title: {
    color: colors.surface,
    fontSize: 31,
    fontWeight: '800',
    letterSpacing: -0.5,
    marginTop: 22,
    textAlign: 'center',
  },
  subtitle: {
    color: colors.surface,
    fontSize: 17,
    fontWeight: '400',
    lineHeight: 24,
    marginTop: 10,
    textAlign: 'center',
  },
});
