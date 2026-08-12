import { COMMITMENT_FREQUENCIES, COMMITMENT_STATUSES } from './Commitment';

const ALLOWED_FREQUENCIES = new Set(
  COMMITMENT_FREQUENCIES.map(({ value }) => value),
);
const ALLOWED_PRIORITIES = new Set(['Baja', 'Media', 'Alta']);
const ALLOWED_STATUSES = new Set(COMMITMENT_STATUSES.map(({ value }) => value));

export const COMMITMENT_PRIORITIES = Object.freeze(['Alta', 'Media', 'Baja']);
export const COMMITMENT_REMINDERS = Object.freeze([
  Object.freeze({ label: '15 minutos antes', value: 15 }),
  Object.freeze({ label: '30 minutos antes', value: 30 }),
  Object.freeze({ label: '1 hora antes', value: 60 }),
  Object.freeze({ label: '1 día antes', value: 1440 }),
]);

export class CommitmentValidationError extends Error {
  constructor(fieldErrors) {
    super('Revisa los campos indicados.');
    this.name = 'CommitmentValidationError';
    this.fieldErrors = fieldErrors;
  }
}

function isValidDate(value) {
  return value instanceof Date && !Number.isNaN(value.getTime());
}

function localIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function localDateTime(date) {
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${localIsoDate(date)}T${hours}:${minutes}:00`;
}

function withDate(time, date) {
  return new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
    time.getHours(),
    time.getMinutes(),
  );
}

function startOfToday(clock) {
  return new Date(clock.getFullYear(), clock.getMonth(), clock.getDate());
}

export class CommitmentDraft {
  constructor(data) {
    Object.assign(this, data);
    Object.freeze(this);
  }

  static create(form, clock = new Date()) {
    return new CommitmentDraft(CommitmentDraft.validate(form, clock));
  }

  static update(form, commitment, clock = new Date()) {
    if (!commitment?.id) {
      throw new Error('El compromiso no tiene un identificador válido.');
    }

    return new CommitmentDraft(CommitmentDraft.validate(form, clock, commitment));
  }

  static validate(form, clock, commitment = null) {
    const title = String(form.title ?? '').trim();
    const categoryId = String(form.categoryId ?? '').trim();
    const frequency = Number(form.frequency);
    const priority = String(form.priority ?? 'Media');
    const reminderMinutes = Number(form.reminderMinutes);
    const status = String(form.status ?? commitment?.status ?? 'Pendiente');
    const eventDate = form.eventDate;
    const startTime = form.startTime;
    const endTime = form.endTime;
    const errors = {};

    if (!title) {
      errors.title = 'Escribe el nombre del compromiso.';
    } else if (title.length > 35) {
      errors.title = 'El nombre no puede superar 35 caracteres.';
    } else if (/<[^>]*>/.test(title)) {
      errors.title = 'El nombre no puede contener HTML.';
    }

    if (!categoryId) {
      errors.categoryId = 'Selecciona una categoría.';
    }
    if (!ALLOWED_FREQUENCIES.has(frequency)) {
      errors.frequency = 'Selecciona una frecuencia válida.';
    }
    if (!ALLOWED_PRIORITIES.has(priority)) {
      errors.priority = 'Selecciona una prioridad válida.';
    }
    if (!COMMITMENT_REMINDERS.some(({ value }) => value === reminderMinutes)) {
      errors.reminderMinutes = 'Selecciona un recordatorio válido.';
    }
    if (!ALLOWED_STATUSES.has(status)) {
      errors.status = 'Selecciona un estado válido.';
    }
    const unchangedEventDate = isValidDate(eventDate)
      && localIsoDate(eventDate) === commitment?.eventDate;
    if (!isValidDate(eventDate) || (!unchangedEventDate && eventDate < startOfToday(clock))) {
      errors.eventDate = 'Selecciona una fecha actual o futura.';
    }
    if (!isValidDate(startTime)) {
      errors.startTime = 'Selecciona la hora de inicio.';
    }
    if (!isValidDate(endTime)) {
      errors.endTime = 'Selecciona la hora de finalización.';
    }

    if (Object.keys(errors).length > 0) {
      throw new CommitmentValidationError(errors);
    }

    const start = withDate(startTime, eventDate);
    const end = withDate(endTime, eventDate);
    if (end <= start) {
      throw new CommitmentValidationError({
        endTime: 'La hora de finalización debe ser posterior al inicio.',
      });
    }

    const reminder = new Date(start.getTime() - reminderMinutes * 60_000);
    const zoneId = commitment?.zoneId
      || Intl.DateTimeFormat().resolvedOptions().timeZone
      || 'America/Bogota';

    return {
      categoryId,
      endHour: localDateTime(end),
      eventDate: localIsoDate(eventDate),
      frequency,
      priority,
      reminder: localDateTime(reminder),
      startHour: localDateTime(start),
      status,
      title,
      zoneId,
    };
  }

  toRequest() {
    return {
      categoryId: this.categoryId,
      endHour: this.endHour,
      eventDate: this.eventDate,
      frequency: this.frequency,
      priority: this.priority,
      reminder: this.reminder,
      startHour: this.startHour,
      status: this.status,
      title: this.title,
      zoneId: this.zoneId,
    };
  }

  toUpdateRequest(commitment) {
    const normalizeDateTime = (value) => String(value ?? '').slice(0, 19);
    const fields = {
      categoryId: this.categoryId,
      endHour: this.endHour,
      eventDate: this.eventDate,
      frequency: this.frequency,
      priority: this.priority,
      reminder: this.reminder,
      startHour: this.startHour,
      status: this.status,
      title: this.title,
      zoneId: this.zoneId,
    };

    return Object.fromEntries(Object.entries(fields).filter(([field, value]) => {
      if (field === 'categoryId') {
        return value !== (commitment.category?.id ?? null);
      }
      if (['endHour', 'reminder', 'startHour'].includes(field)) {
        return normalizeDateTime(value) !== normalizeDateTime(commitment[field]);
      }
      return value !== commitment[field];
    }));
  }
}
