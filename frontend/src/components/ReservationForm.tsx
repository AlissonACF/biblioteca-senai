import { useEffect, useMemo, useState } from "react";

import { useStore } from "@/lib/store";
import { SLOT_HOURS } from "@/lib/types";
import { fmtSlot, todayISO } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import { CalendarClock, Info, Users } from "lucide-react";

interface Props {
  kind: "room" | "computer";
}

export function ReservationForm({ kind }: Props) {
  const { state, createReservation } = useStore();
  const resources = useMemo(
    () => state.resources.filter((r) => r.kind === kind && r.active),
    [state.resources, kind],
  );

  const [resourceId, setResourceId] = useState(resources[0]?.id ?? "");
  const [date, setDate] = useState(todayISO());
  const [startHour, setStartHour] = useState<number>(SLOT_HOURS[0]);
  const [people, setPeople] = useState(1);

  useEffect(() => {
    if (!resourceId && resources[0]) {
      setResourceId(resources[0].id);
    }
  }, [resourceId, resources]);

  const resource = resources.find((r) => r.id === resourceId);
  const cap = resource?.capacity ?? 1;
  const requiresApproval = people > 1;

  const takenSlots = useMemo(() => {
    const set = new Set<number>();
    state.reservations.forEach((r) => {
      if (
        r.resourceId === resourceId &&
        r.date === date &&
        ["pending_approval", "approved", "checked_in"].includes(r.status)
      )
        set.add(r.startHour);
    });
    return set;
  }, [state.reservations, resourceId, date]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resourceId) {
      toast.error("Selecione um recurso.");
      return;
    }
    const safePeople = Math.min(Math.max(1, people), cap);
    const res = await createReservation({
      resourceId,
      date,
      startHour,
      people: safePeople,
    });
    if (!res.ok) {
      toast.error(res.message ?? "Não foi possível reservar.");
      return;
    }
    if (res.reservation?.status === "pending_approval") {
      toast.success("Solicitação enviada ao administrador para aprovação.");
    } else {
      toast.success("Reserva confirmada!");
    }
  };

  if (resources.length === 0) {
    return (
      <Card>
        <CardContent className="py-10 text-center text-sm text-muted-foreground">
          Nenhum recurso disponível no momento.
        </CardContent>
      </Card>
    );
  }

  return (
    <form onSubmit={submit}>
      <Card className="border-border/70 shadow-card">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <CalendarClock className="h-5 w-5 text-primary" />
            Nova reserva — {kind === "room" ? "Sala" : "Computador"}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <Label>Recurso</Label>
            <Select value={resourceId} onValueChange={setResourceId}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {resources.map((r) => (
                  <SelectItem key={r.id} value={r.id}>
                    {r.name} · até {r.capacity} pessoas
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>Data</Label>
            <Input
              type="date"
              value={date}
              min={todayISO()}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>
          <div className="space-y-2">
            <Label>Horário (slots de 1h)</Label>
            <Select
              value={String(startHour)}
              onValueChange={(v) => setStartHour(Number(v))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {SLOT_HOURS.map((h) => {
                  const taken = takenSlots.has(h);
                  return (
                    <SelectItem key={h} value={String(h)} disabled={taken}>
                      {fmtSlot(h)} {taken ? "· indisponível" : ""}
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label className="flex items-center gap-1">
              <Users className="h-4 w-4" /> Pessoas (incluindo você)
            </Label>
            <Input
              type="number"
              min={1}
              max={cap}
              value={people}
              onChange={(e) => setPeople(Number(e.target.value))}
            />
          </div>
        </CardContent>
        <CardContent className="flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
          {requiresApproval ? (
            <p className="flex items-start gap-2 text-sm text-warning-foreground">
              <Info className="mt-0.5 h-4 w-4 text-warning" />
              Reservas com mais de 1 pessoa precisam de aprovação do
              administrador antes de serem confirmadas.
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              Reserva individual: confirmação imediata.
            </p>
          )}
          <Button
            type="submit"
            className="bg-primary text-primary-foreground hover:bg-primary-hover"
          >
            Confirmar reserva
          </Button>
        </CardContent>
      </Card>
    </form>
  );
}
