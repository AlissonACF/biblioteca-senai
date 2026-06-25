import { createFileRoute, Navigate } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { AppShell } from "@/components/AppShell";
import { useStore } from "@/lib/store";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { ReservationForm } from "@/components/ReservationForm";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { fmtDate, fmtSlot, kindLabel, statusLabel, statusTone } from "@/lib/format";
import { toast } from "sonner";
import {
  Building2,
  MonitorSmartphone,
  Plus,
  Trash2,
  UserPlus,
  CheckCircle2,
  XCircle,
  ClipboardList,
  AlertCircle,
  CalendarX,
} from "lucide-react";
import type { ResourceKind } from "@/lib/types";

export const Route = createFileRoute("/admin")({
  component: AdminApp,
});

function AdminApp() {
  const { currentUser, loading } = useStore();
  const [tab, setTab] = useState("aprovacoes");

  if (loading) return null;
  if (!currentUser) return <Navigate to="/login" />;
  if (currentUser.role !== "admin") return <Navigate to="/app" />;

  return (
    <AppShell title="Painel administrativo" nav={[{ to: "/admin", label: "Admin" }]}>
      <div className="mb-8">
        <p className="text-sm font-medium text-primary">Administração</p>
        <h1 className="mt-1 text-3xl font-semibold">Gestão da Biblioteca</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Aprove pedidos, gerencie recursos e usuários, e acompanhe o uso.
        </p>
      </div>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList className="flex w-full flex-wrap gap-1 sm:w-auto">
          <TabsTrigger value="aprovacoes">Aprovações</TabsTrigger>
          <TabsTrigger value="reservas">Todas as reservas</TabsTrigger>
          <TabsTrigger value="nova">Nova reserva</TabsTrigger>
          <TabsTrigger value="recursos">Recursos</TabsTrigger>
          <TabsTrigger value="usuarios">Usuários</TabsTrigger>
          <TabsTrigger value="relatorios">Relatórios</TabsTrigger>
        </TabsList>

        <TabsContent value="aprovacoes" className="mt-6">
          <ApprovalsPanel />
        </TabsContent>
        <TabsContent value="reservas" className="mt-6">
          <AllReservations />
        </TabsContent>
        <TabsContent value="nova" className="mt-6 grid gap-6 lg:grid-cols-2">
          <ReservationForm kind="room" />
          <ReservationForm kind="computer" />
        </TabsContent>
        <TabsContent value="recursos" className="mt-6">
          <ResourcesPanel />
        </TabsContent>
        <TabsContent value="usuarios" className="mt-6">
          <UsersPanel />
        </TabsContent>
        <TabsContent value="relatorios" className="mt-6">
          <ReportsPanel />
        </TabsContent>
      </Tabs>
    </AppShell>
  );
}

/* ---------------- Aprovações ---------------- */

function ApprovalsPanel() {
  const { state, decideReservation } = useStore();
  const pending = useMemo(
    () => state.reservations.filter((r) => r.status === "pending_approval"),
    [state.reservations],
  );

  if (pending.length === 0)
    return (
      <Card>
        <CardContent className="py-10 text-center text-sm text-muted-foreground">
          Nenhum pedido aguardando aprovação.
        </CardContent>
      </Card>
    );

  return (
    <div className="grid gap-3">
      {pending.map((r) => (
        <Card key={r.id} className="border-warning/40 shadow-card">
          <CardContent className="flex flex-col gap-4 py-5 sm:flex-row sm:items-center sm:justify-between">
            <div className="space-y-1">
              <p className="flex items-center gap-2 text-sm font-medium">
                <AlertCircle className="h-4 w-4 text-warning" />
                Pedido de reserva — aguardando análise
              </p>
              <p className="text-base font-semibold">{r.resourceName}</p>
              <p className="text-sm text-muted-foreground">
                {fmtDate(r.date)} · {fmtSlot(r.startHour)} · {r.people} pessoas
              </p>
              <p className="text-xs text-muted-foreground">
                Solicitado por {r.userName}
              </p>
            </div>
            <div className="flex gap-2">
              <RejectDialog
                onConfirm={async (reason) => {
                  const result = await decideReservation(r.id, false, reason);
                  if (result.ok) toast.success("Pedido recusado.");
                  else toast.error(result.message ?? "Erro ao recusar pedido.");
                }}
              />
              <Button
                size="sm"
                className="bg-success text-success-foreground hover:opacity-90"
                onClick={async () => {
                  const result = await decideReservation(r.id, true);
                  if (result.ok) toast.success("Pedido aprovado.");
                  else toast.error(result.message ?? "Erro ao aprovar pedido.");
                }}
              >
                <CheckCircle2 className="mr-1 h-4 w-4" /> Aprovar
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function RejectDialog({ onConfirm }: { onConfirm: (reason: string) => void }) {
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState("");
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline" className="text-destructive">
          <XCircle className="mr-1 h-4 w-4" /> Recusar
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Recusar pedido</DialogTitle>
        </DialogHeader>
        <div className="space-y-2">
          <Label>Motivo (opcional)</Label>
          <Input
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Ex.: capacidade excedida no horário"
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>
            Voltar
          </Button>
          <Button
            variant="destructive"
            onClick={() => {
              onConfirm(reason.trim());
              setOpen(false);
              setReason("");
            }}
          >
            Confirmar recusa
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ---------------- Todas as reservas (tabela) ---------------- */

function AllReservations() {
  const { state, cancelReservation } = useStore();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  const rows = useMemo(() => {
    const q = search.toLowerCase().trim();
    return [...state.reservations]
      .filter((r) => (statusFilter === "all" ? true : r.status === statusFilter))
      .filter((r) =>
        !q
          ? true
          : r.resourceName.toLowerCase().includes(q) ||
            r.userName.toLowerCase().includes(q),
      )
      .sort((a, b) => {
        const ka = `${a.date}-${String(a.startHour).padStart(2, "0")}`;
        const kb = `${b.date}-${String(b.startHour).padStart(2, "0")}`;
        return kb.localeCompare(ka);
      });
  }, [state.reservations, search, statusFilter]);

  const handle = async (label: string, fn: () => Promise<{ ok: boolean; message?: string }>) => {
    const r = await fn();
    if (r.ok) toast.success(label);
    else toast.error(r.message ?? "Operação falhou.");
  };

  return (
    <div className="space-y-4">
      <Card className="shadow-card">
        <CardContent className="flex flex-col gap-3 py-4 sm:flex-row sm:items-center">
          <Input
            placeholder="Buscar por recurso ou usuário…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="sm:max-w-xs"
          />
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="sm:max-w-[240px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todos os status</SelectItem>
              <SelectItem value="pending_approval">Aguardando aprovação</SelectItem>
              <SelectItem value="approved">Aprovadas</SelectItem>
              <SelectItem value="checked_in">Em uso</SelectItem>
              <SelectItem value="completed">Concluídas</SelectItem>
              <SelectItem value="cancelled">Canceladas</SelectItem>
              <SelectItem value="rejected">Recusadas</SelectItem>
              <SelectItem value="no_show">No-show</SelectItem>
              <SelectItem value="expired">Expiradas</SelectItem>
            </SelectContent>
          </Select>
          <span className="text-sm text-muted-foreground sm:ml-auto">
            {rows.length} reserva(s)
          </span>
        </CardContent>
      </Card>

      <Card className="shadow-card">
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Recurso</TableHead>
                <TableHead>Tipo</TableHead>
                <TableHead>Data</TableHead>
                <TableHead>Início</TableHead>
                <TableHead>Fim</TableHead>
                <TableHead>Usuário</TableHead>
                <TableHead className="text-center">Pessoas</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Check-in</TableHead>
                <TableHead>Check-out</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={11}
                    className="py-10 text-center text-sm text-muted-foreground"
                  >
                    Nenhuma reserva encontrada.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((r) => {
                  const fmtTime = (iso?: string) =>
                    iso
                      ? new Date(iso).toLocaleTimeString("pt-BR", {
                          hour: "2-digit",
                          minute: "2-digit",
                        })
                      : "—";
                  const canCancel = [
                    "pending_approval",
                    "approved",
                    "checked_in",
                  ].includes(r.status);
                  return (
                    <TableRow key={r.id}>
                      <TableCell className="font-medium">{r.resourceName}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {kindLabel(r.resourceKind)}
                      </TableCell>
                      <TableCell>{fmtDate(r.date)}</TableCell>
                      <TableCell className="tabular-nums">
                        {String(r.startHour).padStart(2, "0")}:00
                      </TableCell>
                      <TableCell className="tabular-nums">
                        {String(r.startHour + 1).padStart(2, "0")}:00
                      </TableCell>
                      <TableCell>{r.userName}</TableCell>
                      <TableCell className="text-center">{r.people}</TableCell>
                      <TableCell>
                        <span
                          className={`rounded-full border px-2 py-0.5 text-[11px] font-medium ${statusTone[r.status]}`}
                        >
                          {statusLabel[r.status]}
                        </span>
                      </TableCell>
                      <TableCell className="text-xs tabular-nums text-muted-foreground">
                        {fmtTime(r.checkInAt)}
                      </TableCell>
                      <TableCell className="text-xs tabular-nums text-muted-foreground">
                        {fmtTime(r.checkOutAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="inline-flex gap-1">
                          {canCancel && (
                            <Button
                              size="sm"
                              variant="ghost"
                              className="h-7 text-destructive"
                              onClick={() =>
                                handle("Reserva cancelada.", () =>
                                  cancelReservation(r.id, { force: true }),
                                )
                              }
                            >
                              <CalendarX className="h-3.5 w-3.5" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}

/* ---------------- Recursos ---------------- */

function ResourcesPanel() {
  const { state, addResource, removeResource } = useStore();
  const [kind, setKind] = useState<ResourceKind>("room");
  const [name, setName] = useState("");
  const [capacity, setCapacity] = useState(5);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast.error("Informe um nome.");
      return;
    }
    const result = await addResource({ kind, name: name.trim(), capacity: Math.max(1, capacity) });
    if (!result.ok) {
      toast.error(result.message ?? "Erro ao adicionar recurso.");
      return;
    }
    toast.success("Recurso adicionado.");
    setName("");
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_1.4fr]">
      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-base">Adicionar recurso</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2">
              <Label>Tipo</Label>
              <Select
                value={kind}
                onValueChange={(v) => {
                  setKind(v as ResourceKind);
                  setCapacity(v === "room" ? 5 : 2);
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="room">Sala</SelectItem>
                  <SelectItem value="computer">Computador</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Nome</Label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder={kind === "room" ? "Sala de Estudo 5" : "Computador 10"}
              />
            </div>
            <div className="space-y-2">
              <Label>Capacidade (pessoas)</Label>
              <Input
                type="number"
                min={1}
                value={capacity}
                onChange={(e) => setCapacity(Number(e.target.value))}
              />
            </div>
            <Button type="submit" className="w-full bg-primary text-primary-foreground hover:bg-primary-hover">
              <Plus className="mr-1 h-4 w-4" /> Adicionar
            </Button>
          </form>
        </CardContent>
      </Card>

      <div className="space-y-6">
        <ResourceGroup
          title="Salas"
          icon={<Building2 className="h-4 w-4" />}
          items={state.resources.filter((r) => r.kind === "room")}
          onRemove={removeResource}
        />
        <ResourceGroup
          title="Computadores"
          icon={<MonitorSmartphone className="h-4 w-4" />}
          items={state.resources.filter((r) => r.kind === "computer")}
          onRemove={removeResource}
        />
      </div>
    </div>
  );
}

function ResourceGroup({
  title,
  icon,
  items,
  onRemove,
}: {
  title: string;
  icon: React.ReactNode;
  items: { id: string; name: string; capacity: number }[];
  onRemove: (id: string) => Promise<{ ok: boolean; message?: string }>;
}) {
  return (
    <Card className="shadow-card">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          {icon} {title} ({items.length})
        </CardTitle>
      </CardHeader>
      <CardContent>
        {items.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhum cadastrado.</p>
        ) : (
          <ul className="divide-y">
            {items.map((r) => (
              <li
                key={r.id}
                className="flex items-center justify-between py-2 text-sm"
              >
                <span>
                  <span className="font-medium">{r.name}</span>
                  <span className="ml-2 text-muted-foreground">
                    cap. {r.capacity}
                  </span>
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-destructive hover:bg-destructive/10"
                  onClick={async () => {
                    const result = await onRemove(r.id);
                    if (result.ok) toast.success("Recurso removido.");
                    else toast.error(result.message ?? "Erro ao remover recurso.");
                  }}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}

/* ---------------- Usuários ---------------- */

function UsersPanel() {
  const { state, addUser, removeUser, currentUser } = useStore();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<"user" | "admin">("user");

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !email.trim() || password.length < 4) {
      toast.error("Preencha nome, e-mail e senha (≥ 4 caracteres).");
      return;
    }
    const r = await addUser({ name: name.trim(), email: email.trim(), password, role });
    if (!r.ok) {
      toast.error(r.message ?? "Erro ao criar usuário.");
      return;
    }
    toast.success("Usuário criado.");
    setName("");
    setEmail("");
    setPassword("");
    setRole("user");
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_1.4fr]">
      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-base">Criar usuário</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="space-y-4">
            <div className="space-y-2">
              <Label>Nome</Label>
              <Input value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>E-mail</Label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Senha</Label>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Perfil</Label>
              <Select value={role} onValueChange={(v) => setRole(v as "user" | "admin")}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="user">Usuário comum</SelectItem>
                  <SelectItem value="admin">Administrador</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button type="submit" className="w-full bg-primary text-primary-foreground hover:bg-primary-hover">
              <UserPlus className="mr-1 h-4 w-4" /> Criar usuário
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-base">
            Usuários cadastrados ({state.users.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="divide-y">
            {state.users.map((u) => (
              <li key={u.id} className="flex items-center justify-between py-3">
                <div>
                  <p className="text-sm font-medium">{u.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {u.email} ·{" "}
                    <span className="capitalize">
                      {u.role === "admin" ? "administrador" : "usuário"}
                    </span>
                  </p>
                </div>
                {u.id !== currentUser?.id && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-destructive hover:bg-destructive/10"
                    onClick={async () => {
                      const result = await removeUser(u.id);
                      if (result.ok) toast.success("Usuario removido.");
                      else toast.error(result.message ?? "Erro ao remover usuario.");
                    }}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                )}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}

/* ---------------- Relatórios ---------------- */

function ReportsPanel() {
  const { state } = useStore();
  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().slice(0, 10);
  });
  const [to, setTo] = useState(() => new Date().toISOString().slice(0, 10));

  const filtered = useMemo(
    () =>
      state.reservations.filter((r) => r.date >= from && r.date <= to),
    [state.reservations, from, to],
  );

  const total = filtered.length;
  const completed = filtered.filter((r) =>
    ["completed", "checked_in"].includes(r.status),
  ).length;
  const noShows = filtered.filter((r) => r.status === "no_show").length;
  const cancelled = filtered.filter((r) => r.status === "cancelled").length;
  const pending = filtered.filter((r) => r.status === "pending_approval").length;

  // dias no intervalo × slots × recursos = capacidade total de slots
  const days =
    Math.max(
      1,
      Math.round(
        (new Date(to).getTime() - new Date(from).getTime()) / 86400000,
      ) + 1,
    );
  const slotsPerDay = 15; // 07-22
  const totalSlots = days * slotsPerDay * Math.max(1, state.resources.length);
  const usageRate = ((total / totalSlots) * 100).toFixed(1);

  const byResource = useMemo(() => {
    const map = new Map<string, { name: string; kind: string; count: number }>();
    state.resources.forEach((r) =>
      map.set(r.id, { name: r.name, kind: kindLabel(r.kind), count: 0 }),
    );
    filtered.forEach((r) => {
      const e = map.get(r.resourceId);
      if (e) e.count += 1;
    });
    return Array.from(map.values()).sort((a, b) => b.count - a.count);
  }, [filtered, state.resources]);

  const exportCSV = () => {
    const rows = [
      ["Data", "Horário", "Recurso", "Tipo", "Usuário", "Pessoas", "Status"],
      ...filtered.map((r) => [
        r.date,
        fmtSlot(r.startHour),
        r.resourceName,
        kindLabel(r.resourceKind),
        r.userName,
        String(r.people),
        r.status,
      ]),
    ];
    const csv = rows
      .map((row) => row.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(","))
      .join("\n");
    const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `relatorio-biblioteca_${from}_a_${to}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-6">
      <Card className="shadow-card">
        <CardContent className="flex flex-col gap-4 py-5 sm:flex-row sm:items-end sm:justify-between">
          <div className="grid grid-cols-2 gap-3 sm:flex sm:items-end">
            <div className="space-y-2">
              <Label>De</Label>
              <Input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Até</Label>
              <Input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
            </div>
          </div>
          <Button variant="outline" onClick={exportCSV}>
            <ClipboardList className="mr-1 h-4 w-4" /> Exportar CSV
          </Button>
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Stat label="Total de reservas" value={total} />
        <Stat label="Concluídas / em uso" value={completed} tone="success" />
        <Stat label="Cancelamentos" value={cancelled} />
        <Stat label="No-show" value={noShows} tone="destructive" />
        <Stat label="Aguardando aprovação" value={pending} tone="warning" />
      </div>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-base">
            Taxa de ocupação no período
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-4xl font-semibold text-primary">{usageRate}%</p>
          <p className="mt-1 text-xs text-muted-foreground">
            {total} reservas em {totalSlots} slots possíveis
            ({state.resources.length} recursos × {slotsPerDay} slots/dia × {days} dia(s)).
          </p>
        </CardContent>
      </Card>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-base">Uso por recurso</CardTitle>
        </CardHeader>
        <CardContent>
          {byResource.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sem dados.</p>
          ) : (
            <ul className="space-y-3">
              {byResource.map((r) => {
                const pct = total ? Math.round((r.count / total) * 100) : 0;
                return (
                  <li key={r.name}>
                    <div className="mb-1 flex justify-between text-sm">
                      <span>
                        <span className="font-medium">{r.name}</span>{" "}
                        <span className="text-xs text-muted-foreground">
                          · {r.kind}
                        </span>
                      </span>
                      <span className="tabular-nums text-muted-foreground">
                        {r.count} · {pct}%
                      </span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-muted">
                      <div
                        className="h-full bg-primary"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function Stat({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone?: "success" | "destructive" | "warning";
}) {
  const color =
    tone === "success"
      ? "text-success"
      : tone === "destructive"
        ? "text-destructive"
        : tone === "warning"
          ? "text-warning"
          : "text-primary";
  return (
    <Card className="shadow-card">
      <CardContent className="py-5">
        <p className="text-xs uppercase tracking-wider text-muted-foreground">
          {label}
        </p>
        <p className={`mt-1 text-3xl font-semibold ${color}`}>{value}</p>
      </CardContent>
    </Card>
  );
}
