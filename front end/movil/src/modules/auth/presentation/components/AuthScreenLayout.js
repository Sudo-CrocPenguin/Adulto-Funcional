import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
  View,
} from 'react-native';

import { colors } from '../../../../shared/theme/tokens';
import { BrandHero } from './BrandHero';

export function AuthScreenLayout({ children, cardStyle }) {
  const { width } = useWindowDimensions();
  const cardWidth = Math.min(width - 36, 520);

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={styles.keyboardView}
    >
      <ScrollView
        bounces={false}
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <BrandHero />
        <View style={styles.formRegion}>
          <View style={[styles.card, { width: cardWidth }, cardStyle]}>
            {children}
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  keyboardView: {
    backgroundColor: colors.background,
    flex: 1,
  },
  scrollContent: {
    backgroundColor: colors.background,
    flexGrow: 1,
  },
  formRegion: {
    alignItems: 'center',
    paddingBottom: 42,
    paddingTop: 36,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: 22,
    elevation: 8,
    paddingBottom: 24,
    paddingHorizontal: 24,
    paddingTop: 23,
    shadowColor: '#A9B3C1',
    shadowOffset: { width: -5, height: 2 },
    shadowOpacity: 0.35,
    shadowRadius: 7,
  },
});

