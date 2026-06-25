import type { ReservationStatus, ResourceKind } from "./types";

export const fmtHour = (h: number) => `${String(h).padStart(2, "0")}:00`;
export const fmtSlot = (start: number) => `${fmtHour(start)} – ${fmtHour(start + 1)}`;

export const fmtDate = (iso: string) => {
  const [y, m, d] = iso.split("-");
  return `${d}/${m}/${y}`;
};

export const todayISO = () => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
};

export const kindLabel = (k: ResourceKind) =>
  k === "room" ? "Sala" : "Computador";

export const statusLabel: Record<ReservationStatus, string> = {
  pending_approval: "Aguardando aprovação",
  approved: "Aprovada",
  rejected: "Recusada",
  cancelled: "Cancelada",
  checked_in: "Em uso (check-in)",
  completed: "Concluída",
  no_show: "Não compareceu",
  expired: "Expirada (sem confirmação)",
};

export const statusTone: Record<ReservationStatus, string> = {
  pending_approval: "bg-warning/15 text-warning-foreground border-warning/40",
  approved: "bg-primary/10 text-primary border-primary/30",
  rejected: "bg-destructive/10 text-destructive border-destructive/30",
  cancelled: "bg-muted text-muted-foreground border-border",
  checked_in: "bg-success/15 text-success border-success/40",
  completed: "bg-secondary text-secondary-foreground border-border",
  no_show: "bg-destructive/10 text-destructive border-destructive/30",
  expired: "bg-destructive/10 text-destructive border-destructive/30",
};
