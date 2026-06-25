import { createFileRoute, Navigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useStore } from "@/lib/store";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ReservationForm } from "@/components/ReservationForm";
import { ReservationList } from "@/components/ReservationList";
import { Card, CardContent } from "@/components/ui/card";
import { Building2, MonitorSmartphone, ClipboardList } from "lucide-react";

export const Route = createFileRoute("/app")({
  component: UserApp,
});

function UserApp() {
  const { currentUser, state, loading } = useStore();
  const [tab, setTab] = useState("salas");

  const myReservations = useMemo(
    () =>
      currentUser
        ? state.reservations.filter((r) => r.userId === currentUser.id)
        : [],
    [state.reservations, currentUser],
  );
  const active = myReservations.filter((r) =>
    ["pending_approval", "approved", "checked_in"].includes(r.status),
  );

  if (loading) return null;
  if (!currentUser) return <Navigate to="/login" />;
  if (currentUser.role === "admin") return <Navigate to="/admin" />;

  return (
    <AppShell
      title="Biblioteca · Reservas"
      nav={[
        { to: "/app", label: "Minha área" },
      ]}
    >
      <div className="mb-8 flex flex-col gap-2">
        <p className="text-sm font-medium text-primary">
          Olá, {currentUser.name.split(" ")[0]} 👋
        </p>
        <h1 className="text-3xl font-semibold">Faça sua reserva</h1>
        <p className="text-sm text-muted-foreground">
          Slots de 1 hora · funcionamento 07:00–22:00 · cancelamento até 30 min antes ·
          check-in disponível até 1 hora antes (caso contrário a reserva expira).
        </p>
      </div>

      <div className="mb-8 grid gap-4 sm:grid-cols-3">
        <StatCard
          icon={<Building2 className="h-5 w-5" />}
          label="Salas disponíveis"
          value={state.resources.filter((r) => r.kind === "room" && r.active).length}
        />
        <StatCard
          icon={<MonitorSmartphone className="h-5 w-5" />}
          label="Computadores"
          value={state.resources.filter((r) => r.kind === "computer" && r.active).length}
        />
        <StatCard
          icon={<ClipboardList className="h-5 w-5" />}
          label="Minhas reservas ativas"
          value={active.length}
        />
      </div>

      <Tabs value={tab} onValueChange={setTab} className="w-full">
        <TabsList className="grid w-full grid-cols-3 sm:w-auto">
          <TabsTrigger value="salas">Salas</TabsTrigger>
          <TabsTrigger value="pcs">Computadores</TabsTrigger>
          <TabsTrigger value="minhas">Minhas reservas</TabsTrigger>
        </TabsList>

        <TabsContent value="salas" className="mt-6">
          <ReservationForm kind="room" />
        </TabsContent>
        <TabsContent value="pcs" className="mt-6">
          <ReservationForm kind="computer" />
        </TabsContent>
        <TabsContent value="minhas" className="mt-6">
          <ReservationList reservations={myReservations} />
        </TabsContent>
      </Tabs>
    </AppShell>
  );
}

function StatCard({
  icon,
  label,
  value,
}: {
  icon: React.ReactNode;
  label: string;
  value: number;
}) {
  return (
    <Card className="border-border/70 shadow-card">
      <CardContent className="flex items-center gap-4 py-5">
        <div className="grid h-11 w-11 place-items-center rounded-md bg-primary/10 text-primary">
          {icon}
        </div>
        <div>
          <p className="text-xs uppercase tracking-wider text-muted-foreground">
            {label}
          </p>
          <p className="text-2xl font-semibold">{value}</p>
        </div>
      </CardContent>
    </Card>
  );
}
