import { useMemo } from "react";
import type { Reservation } from "@/lib/types";
import { useStore } from "@/lib/store";
import { fmtDate, fmtSlot, statusLabel, statusTone } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "sonner";
import { CalendarX, LogIn, LogOut, Users } from "lucide-react";
import { cn } from "@/lib/utils";

interface Props {
  reservations: Reservation[];
  showActions?: boolean;
}

export function ReservationList({ reservations, showActions = true }: Props) {
  const { cancelReservation, checkIn, checkOut } = useStore();

  const sorted = useMemo(
    () =>
      [...reservations].sort((a, b) => {
        const ka = `${a.date}-${String(a.startHour).padStart(2, "0")}`;
        const kb = `${b.date}-${String(b.startHour).padStart(2, "0")}`;
        return kb.localeCompare(ka);
      }),
    [reservations],
  );

  if (sorted.length === 0) {
    return (
      <Card>
        <CardContent className="py-10 text-center text-sm text-muted-foreground">
          Nenhuma reserva por aqui ainda.
        </CardContent>
      </Card>
    );
  }

  const handle = async (label: string, fn: () => Promise<{ ok: boolean; message?: string }>) => {
    const r = await fn();
    if (r.ok) toast.success(label);
    else toast.error(r.message ?? "Operação falhou.");
  };

  return (
    <div className="grid gap-3">
      {sorted.map((r) => {
        const start = new Date(`${r.date}T00:00:00`);
        start.setHours(r.startHour, 0, 0, 0);
        const minsToStart = (start.getTime() - Date.now()) / 60000;

        const cancelVisible = ["pending_approval", "approved", "checked_in"].includes(r.status);
        const cancelEnabled = cancelVisible && minsToStart >= 30;
        const cancelReason = !cancelEnabled
          ? "Cancelamento permitido apenas até 30 min antes."
          : undefined;

        const checkInVisible = r.status === "approved";
        const checkInEnabled = checkInVisible && minsToStart >= 60;
        const checkInReason = !checkInEnabled
          ? "Check-in deve ser feito até 1 hora antes do horário."
          : undefined;

        const checkOutVisible = r.status === "checked_in";

        return (
          <Card key={r.id} className="border-border/70 shadow-card">
            <CardContent className="flex flex-col gap-4 py-5 sm:flex-row sm:items-center sm:justify-between">
              <div className="space-y-1.5">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold">{r.resourceName}</span>
                  <span
                    className={cn(
                      "rounded-full border px-2 py-0.5 text-[11px] font-medium",
                      statusTone[r.status],
                    )}
                  >
                    {statusLabel[r.status]}
                  </span>
                </div>
                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-muted-foreground">
                  <span>
                    {fmtDate(r.date)} · {fmtSlot(r.startHour)}
                  </span>
                  <span className="inline-flex items-center gap-1">
                    <Users className="h-3.5 w-3.5" />
                    {r.people} {r.people > 1 ? "pessoas" : "pessoa"}
                  </span>
                  <span className="text-xs">por {r.userName}</span>
                  {r.checkInAt && (
                    <span className="text-xs text-success">
                      Check-in: {new Date(r.checkInAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}
                    </span>
                  )}
                  {r.checkOutAt && (
                    <span className="text-xs text-muted-foreground">
                      Check-out: {new Date(r.checkOutAt).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })}
                    </span>
                  )}
                </div>
                {r.rejectionReason && (
                  <p className="text-xs text-destructive">
                    Motivo: {r.rejectionReason}
                  </p>
                )}
              </div>

              {showActions && (
                <div className="flex flex-wrap gap-2">

                  {checkInVisible && (
                    <Button
                      size="sm"
                      disabled={!checkInEnabled}
                      title={checkInReason}
                      className="bg-success text-success-foreground hover:opacity-90"
                      onClick={() =>
                        handle("Check-in realizado.", () => checkIn(r.id))
                      }
                    >
                      <LogIn className="mr-1 h-4 w-4" /> Check-in
                    </Button>
                  )}
                  {checkOutVisible && (
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() =>
                        handle("Check-out realizado.", () => checkOut(r.id))
                      }
                    >
                      <LogOut className="mr-1 h-4 w-4" /> Check-out
                    </Button>
                  )}
                  {cancelVisible && (
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={!cancelEnabled}
                      title={cancelReason}
                      className="text-destructive hover:bg-destructive/10"
                      onClick={() =>
                        handle("Reserva cancelada.", () =>
                          cancelReservation(r.id),
                        )
                      }
                    >
                      <CalendarX className="mr-1 h-4 w-4" /> Cancelar
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
