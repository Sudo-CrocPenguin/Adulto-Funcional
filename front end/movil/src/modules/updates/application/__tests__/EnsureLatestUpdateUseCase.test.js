import {
  EnsureLatestUpdateUseCase,
  UPDATE_PHASES,
  UPDATE_RESULTS,
} from '../EnsureLatestUpdateUseCase';

function repository(overrides = {}) {
  return {
    check: jest.fn().mockResolvedValue({ isAvailable: false }),
    download: jest.fn().mockResolvedValue({}),
    isEnabled: jest.fn().mockReturnValue(true),
    reload: jest.fn().mockResolvedValue(undefined),
    ...overrides,
  };
}

describe('EnsureLatestUpdateUseCase', () => {
  it('omite la comprobación donde expo-updates no está disponible', async () => {
    const updates = repository({ isEnabled: jest.fn().mockReturnValue(false) });
    const useCase = new EnsureLatestUpdateUseCase(updates);

    await expect(useCase.execute()).resolves.toEqual({ status: UPDATE_RESULTS.skipped });
    expect(updates.check).not.toHaveBeenCalled();
  });

  it('habilita la aplicación si ya ejecuta la versión más reciente', async () => {
    const phases = [];
    const updates = repository();
    const useCase = new EnsureLatestUpdateUseCase(updates);

    await expect(useCase.execute({ onPhase: (phase) => phases.push(phase) }))
      .resolves.toEqual({ status: UPDATE_RESULTS.current });

    expect(phases).toEqual([UPDATE_PHASES.checking]);
    expect(updates.download).not.toHaveBeenCalled();
    expect(updates.reload).not.toHaveBeenCalled();
  });

  it('descarga y reinicia antes de permitir una versión desactualizada', async () => {
    const phases = [];
    const updates = repository({
      check: jest.fn().mockResolvedValue({ isAvailable: true }),
    });
    const useCase = new EnsureLatestUpdateUseCase(updates);

    await expect(useCase.execute({ onPhase: (phase) => phases.push(phase) }))
      .resolves.toEqual({ status: UPDATE_RESULTS.restarting });

    expect(phases).toEqual([
      UPDATE_PHASES.checking,
      UPDATE_PHASES.downloading,
      UPDATE_PHASES.restarting,
    ]);
    expect(updates.download).toHaveBeenCalledTimes(1);
    expect(updates.reload).toHaveBeenCalledTimes(1);
  });

  it('propaga el fallo para que la compuerta permanezca bloqueada', async () => {
    const updates = repository({
      check: jest.fn().mockRejectedValue(new Error('sin red')),
    });
    const useCase = new EnsureLatestUpdateUseCase(updates);

    await expect(useCase.execute()).rejects.toThrow('sin red');
    expect(updates.download).not.toHaveBeenCalled();
  });
});
