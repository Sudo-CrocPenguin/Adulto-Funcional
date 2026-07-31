export type ApiResponse<T> = {
  status: number;
  message: string;
  data: T;
};

export type AuthResponse = {
  token?: string | null;
  tokenType?: string | null;
  expiresIn: number;
  accountId: string;
  names: string;
  lastnames: string;
  email: string;
  phone: string;
  createdAt: string;
  hasMasterKey: boolean;
};

export type CategoryResponse = {
  id: string;
  name: string;
  type: 'FINANCES' | 'AGENDA' | string;
};

export type MovementResponse = {
  id: string;
  movementType: 'INCOME' | 'EXPENSE' | string;
  amount: number;
  registerDate: string;
  description?: string | null;
  movementDate: string;
  category?: CategoryResponse | null;
};

export type EventResponse = {
  id: string;
  title: string;
  priority: string;
  eventDate: string;
  frequency: number;
  reminder: string;
  startHour: string;
  endHour: string;
  description?: string | null;
  status: string;
  category?: CategoryResponse | null;
};

export type FixedExpenseResponse = {
  id: string;
  name: string;
  frequency: string;
  amount: number;
  status: 'ACTIVE' | 'INACTIVE' | string;
  nextDueDate: string;
  category?: CategoryResponse | null;
};

export type PasswordResponse = {
  id: string;
  applicationName: string;
  password?: string | null;
  lastChangeDate?: string | null;
};
