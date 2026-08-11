import * as Updates from 'expo-updates';
import { Platform } from 'react-native';

import { ApplicationUpdateRepository } from '../domain/ApplicationUpdateRepository';

export class ExpoApplicationUpdateRepository extends ApplicationUpdateRepository {
  isEnabled() {
    return Platform.OS !== 'web' && !__DEV__ && Boolean(Updates.isEnabled);
  }

  check() {
    return Updates.checkForUpdateAsync();
  }

  download() {
    return Updates.fetchUpdateAsync();
  }

  reload() {
    return Updates.reloadAsync({
      reloadScreenOptions: {
        backgroundColor: '#35598D',
        fade: true,
        imageResizeMode: 'contain',
        spinner: {
          color: '#FFFFFF',
          enabled: true,
          size: 'large',
        },
      },
    });
  }
}
