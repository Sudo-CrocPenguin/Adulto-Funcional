import { MOVEMENT_TYPES } from '../FinanceMovement';
import { MovementDraft, MovementValidationError } from '../MovementDraft';

describe('MovementDraft', () => {
  it('serializa un movimiento financiero válido para la API', () => {
    const draft = MovementDraft.create({
      amount: '1500,50',
      categoryId: 'category-work',
      description: 'Salario mensual',
      movementDate: new Date(2026, 7, 10),
      movementType: MOVEMENT_TYPES.income,
    });

    expect(draft.toRequest()).toEqual({
      amount: 1500.5,
      categoryId: 'category-work',
      description: 'Salario mensual',
      movementDate: '2026-08-10',
      movementType: 'INCOME',
    });
  });

  it('rechaza montos, catálogos y descripciones incompatibles', () => {
    expect(() => MovementDraft.create({
      amount: '-15',
      categoryId: '',
      description: '<b>engaño</b>',
      movementDate: null,
      movementType: 'TRANSFER',
    })).toThrow(MovementValidationError);

    try {
      MovementDraft.create({
        amount: '-15',
        categoryId: '',
        description: '<b>engaño</b>',
        movementDate: null,
        movementType: 'TRANSFER',
      });
    } catch (error) {
      expect(error.fieldErrors).toMatchObject({
        amount: expect.any(String),
        categoryId: expect.any(String),
        description: expect.any(String),
        movementDate: expect.any(String),
        movementType: expect.any(String),
      });
    }
  });
});
