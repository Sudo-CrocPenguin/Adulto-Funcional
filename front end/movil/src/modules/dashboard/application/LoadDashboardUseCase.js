export class MissingAccessTokenError extends Error {
  constructor() {
    super('La sesión no incluye un access token válido.');
    this.name = 'MissingAccessTokenError';
  }
}

export class LoadDashboardUseCase {
  constructor(dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  execute(session) {
    if (!session?.accessToken) {
      throw new MissingAccessTokenError();
    }

    return this.dashboardRepository.load(session);
  }
}
