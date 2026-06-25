export type Role = "admin" | "user";

export interface User {
  id: string;
  name: string;
  email: string;
  password: string;
  role: Role;
}

export type ResourceKind = "room" | "computer";

export interface Resource {
  id: string;
  kind: ResourceKind;
  name: string;
  capacity: number;
  active: boolean;
}

export type ReservationStatus =
  | "pending_approval"
  | "approved"
  | "rejected"
  | "cancelled"
  | "checked_in"
  | "completed"
  | "no_show"
  | "expired";

export interface Reservation {
  id: string;
  userId: string;
  userName: string;
  resourceId: string;
  resourceName: string;
  resourceKind: ResourceKind;
  /** ISO date YYYY-MM-DD */
  date: string;
  /** Start hour 7..21 (inclusive). End is start+1 (slots de 1h). */
  startHour: number;
  people: number;
  status: ReservationStatus;
  createdAt: string;
  approvedBy?: string;
  decidedAt?: string;
  checkInAt?: string;
  checkOutAt?: string;
  confirmedAt?: string;
  rejectionReason?: string;
}

export const OPENING_HOUR = 7;
export const CLOSING_HOUR = 22; // last slot starts at 21
export const SLOT_HOURS = Array.from(
  { length: CLOSING_HOUR - OPENING_HOUR },
  (_, i) => OPENING_HOUR + i,
);
