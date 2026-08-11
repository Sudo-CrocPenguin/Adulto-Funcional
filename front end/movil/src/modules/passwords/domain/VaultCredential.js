const CHANGE_INTERVAL_DAYS = 60;

function dateFromIso(value) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(String(value ?? ''))) {
    return null;
  }
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return Number.isNaN(date.getTime()) ? null : date;
}

export class VaultCredential {
  constructor({ applicationName, id, lastChangeDate, password }) {
    this.applicationName = applicationName;
    this.id = id;
    this.lastChangeDate = lastChangeDate;
    this.password = password ?? null;
    Object.freeze(this);
  }

  static fromApi(data) {
    return new VaultCredential({
      applicationName: String(data?.applicationName ?? ''),
      id: data?.id ?? null,
      lastChangeDate: data?.lastChangeDate ?? null,
      password: typeof data?.password === 'string' ? data.password : null,
    });
  }

  needsChangeAt(clock = new Date()) {
    const lastChange = dateFromIso(this.lastChangeDate);
    if (!lastChange) {
      return false;
    }
    const today = new Date(clock.getFullYear(), clock.getMonth(), clock.getDate());
    return today.getTime() - lastChange.getTime() >= CHANGE_INTERVAL_DAYS * 86_400_000;
  }
}

export function passwordStrength(password) {
  const value = String(password ?? '');
  if (!value) {
    return Object.freeze({ label: 'Cifrada', level: 0 });
  }
  let score = 0;
  if (value.length >= 10) score += 1;
  if (value.length >= 15) score += 1;
  if (/[a-záéíóúñ]/u.test(value) && /[A-ZÁÉÍÓÚÑ]/u.test(value)) score += 1;
  if (/\d/u.test(value)) score += 1;
  if (/[^\p{L}\p{N}\s]/u.test(value)) score += 1;

  if (score >= 4) {
    return Object.freeze({ label: 'Fuerte', level: 3 });
  }
  if (score >= 2) {
    return Object.freeze({ label: 'Media', level: 2 });
  }
  return Object.freeze({ label: 'Débil', level: 1 });
}
