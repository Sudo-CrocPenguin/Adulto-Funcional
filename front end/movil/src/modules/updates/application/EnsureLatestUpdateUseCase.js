export const UPDATE_PHASES = Object.freeze({
  checking: 'checking',
  downloading: 'downloading',
  restarting: 'restarting',
});

export const UPDATE_RESULTS = Object.freeze({
  current: 'current',
  restarting: 'restarting',
  skipped: 'skipped',
});

export class EnsureLatestUpdateUseCase {
  constructor(repository) {
    this.repository = repository;
  }

  async execute({ onPhase = () => undefined } = {}) {
    if (!this.repository.isEnabled()) {
      return { status: UPDATE_RESULTS.skipped };
    }

    onPhase(UPDATE_PHASES.checking);
    const update = await this.repository.check();
    if (!update?.isAvailable) {
      return { status: UPDATE_RESULTS.current };
    }

    onPhase(UPDATE_PHASES.downloading);
    await this.repository.download();
    onPhase(UPDATE_PHASES.restarting);
    await this.repository.reload();

    return { status: UPDATE_RESULTS.restarting };
  }
}
