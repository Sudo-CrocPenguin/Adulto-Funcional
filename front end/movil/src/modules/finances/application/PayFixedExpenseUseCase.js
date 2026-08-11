import { MovementDraft } from '../domain/MovementDraft';

export class FixedExpensePaymentSyncError extends Error {
  constructor(movement, cause) {
    super(
      'El egreso fue registrado, pero no pudimos actualizar el próximo vencimiento.',
      { cause },
    );
    this.name = 'FixedExpensePaymentSyncError';
    this.movement = movement;
  }
}

export class PayFixedExpenseUseCase {
  constructor(repository, clock = () => new Date()) {
    this.repository = repository;
    this.clock = clock;
  }

  async execute(expense, session) {
    if (!session?.accessToken) {
      throw new Error('La sesión no incluye un access token válido.');
    }
    if (!expense?.isActive || !expense.category?.id) {
      throw new Error('El gasto fijo no está disponible para registrar el pago.');
    }

    const now = this.clock();
    const movement = await this.repository.createMovement(
      MovementDraft.forFixedExpense(expense, now),
      session,
    );

    try {
      const updatedExpense = await this.repository.updateFixedExpense(
        expense.id,
        { nextDueDate: expense.nextDueDateAfterPayment(now) },
        session,
      );
      return { movement, updatedExpense };
    } catch (error) {
      throw new FixedExpensePaymentSyncError(movement, error);
    }
  }
}
