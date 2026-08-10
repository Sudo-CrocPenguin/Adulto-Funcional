import { MaterialCommunityIcons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '../../../../shared/theme/tokens';

export function BrandHero() {
  return (
    <SafeAreaView edges={['top']} style={styles.hero}>
      <View style={styles.content}>
        <View
          accessibilityLabel="Logotipo de Adulto Funcional"
          accessibilityRole="image"
          style={styles.logo}
        >
          <MaterialCommunityIcons
            color={colors.avatar}
            name="account"
            size={52}
            style={styles.avatar}
          />
          <View style={styles.shieldContainer}>
            <MaterialCommunityIcons
              color={colors.shield}
              name="shield"
              size={43}
            />
            <MaterialCommunityIcons
              color={colors.lock}
              name="lock"
              size={17}
              style={styles.lock}
            />
          </View>
        </View>

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
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: 44,
    height: 88,
    justifyContent: 'flex-start',
    overflow: 'hidden',
    position: 'relative',
    width: 88,
  },
  avatar: {
    marginTop: 2,
  },
  shieldContainer: {
    alignItems: 'center',
    bottom: 2,
    height: 43,
    justifyContent: 'center',
    position: 'absolute',
  },
  lock: {
    position: 'absolute',
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

