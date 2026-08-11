jest.mock('expo-updates', () => ({
  checkForUpdateAsync: jest.fn().mockResolvedValue({ isAvailable: true }),
  fetchUpdateAsync: jest.fn().mockResolvedValue({ isNew: true }),
  isEnabled: true,
  reloadAsync: jest.fn().mockResolvedValue(undefined),
}));

import * as Updates from 'expo-updates';

import { ExpoApplicationUpdateRepository } from '../ExpoApplicationUpdateRepository';

describe('ExpoApplicationUpdateRepository', () => {
  it('delega comprobación, descarga y reinicio en expo-updates', async () => {
    const repository = new ExpoApplicationUpdateRepository();

    await repository.check();
    await repository.download();
    await repository.reload();

    expect(Updates.checkForUpdateAsync).toHaveBeenCalledTimes(1);
    expect(Updates.fetchUpdateAsync).toHaveBeenCalledTimes(1);
    expect(Updates.reloadAsync).toHaveBeenCalledWith({
      reloadScreenOptions: expect.objectContaining({
        backgroundColor: '#35598D',
        fade: true,
        spinner: expect.objectContaining({ enabled: true }),
      }),
    });
  });
});
