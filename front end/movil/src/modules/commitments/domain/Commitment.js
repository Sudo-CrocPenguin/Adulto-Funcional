export const COMMITMENT_FILTERS = Object.freeze({
  all: 'all',
  completed: 'completed',
  pending: 'pending',
});

export const COMMITMENT_FREQUENCIES = Object.freeze([
  Object.freeze({ label: 'Una vez', value: 0 }),
  Object.freeze({ label: 'Diario', value: 1 }),
  Object.freeze({ label: 'Semanal', value: 7 }),
  Object.freeze({ label: 'Mensual', value: 30 }),
  Object.freeze({ label: 'Anual', value: 365 }),
]);

export const COMMITMENT_STATUSES = Object.freeze([
  Object.freeze({ label: 'Pendiente', value: 'Pendiente' }),
  Object.freeze({ label: 'Completado', value: 'Completado' }),
  Object.freeze({ label: 'Pospuesto', value: 'Pospuesto' }),
  Object.freeze({ label: 'Cancelado', value: 'Cancelado' }),
]);

const FREQUENCY_LABELS = Object.freeze(
  Object.fromEntries(COMMITMENT_FREQUENCIES.map(({ label, value }) => [value, label])),
);

export class Commitment {
  constructor(data) {
    Object.assign(this, data);
    if (this.category) {
      Object.freeze(this.category);
    }
    Object.freeze(this);
  }

  static fromApi(data) {
    return new Commitment({
      category: data?.category ?? null,
      description: data?.description ?? '',
      endHour: data?.endHour ?? null,
      eventDate: data?.eventDate ?? null,
      frequency: Number(data?.frequency ?? 0),
      id: data?.id ?? null,
      priority: data?.priority ?? 'Media',
      reminder: data?.reminder ?? null,
      startHour: data?.startHour ?? null,
      status: data?.status ?? 'Pendiente',
      title: data?.title ?? '',
      zoneId: data?.zoneId ?? null,
    });
  }

  get categoryName() {
    return this.category?.name ?? 'Sin categoría';
  }

  get frequencyLabel() {
    return FREQUENCY_LABELS[this.frequency] ?? `Cada ${this.frequency} días`;
  }

  get isCompleted() {
    return this.status === 'Completado';
  }

  get isPending() {
    return this.status === 'Pendiente';
  }
}
