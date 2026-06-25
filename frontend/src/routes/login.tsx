import { createFileRoute, useNavigate, Navigate } from "@tanstack/react-router";
import { useState } from "react";
import { useStore } from "@/lib/store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { BookOpen, ShieldCheck, User as UserIcon } from "lucide-react";

export const Route = createFileRoute("/login")({
  component: LoginPage,
});

function LoginPage() {
  const { login, currentUser, loading: sessionLoading } = useStore();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  if (sessionLoading) return null;
  if (currentUser)
    return <Navigate to={currentUser.role === "admin" ? "/admin" : "/app"} />;

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const u = await login(email, password);
    setLoading(false);
    if (!u) {
      toast.error("Credenciais inválidas.");
      return;
    }
    toast.success(`Bem-vindo(a), ${u.name}.`);
    navigate({ to: u.role === "admin" ? "/admin" : "/app" });
  };

  const fill = (e: string, p: string) => {
    setEmail(e);
    setPassword(p);
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <aside className="relative hidden flex-col justify-between overflow-hidden bg-primary p-12 text-primary-foreground lg:flex">
        <div className="flex items-center gap-3">
          <div className="grid h-11 w-11 place-items-center rounded-lg bg-primary-foreground/15 ring-1 ring-primary-foreground/30">
            <BookOpen className="h-6 w-6" />
          </div>
          <div>
            <p className="text-sm uppercase tracking-widest opacity-80">SENAI</p>
            <p className="text-lg font-semibold">Biblioteca · Reservas</p>
          </div>
        </div>
        <div className="space-y-4">
          <h1 className="text-4xl font-semibold leading-tight">
            Reserve salas e computadores
            <br />
            de forma simples e organizada.
          </h1>
          <p className="max-w-md text-base opacity-90">
            Slots de 1 hora, das 07:00 às 22:00. Reservas em grupo passam por
            aprovação do administrador.
          </p>
        </div>
        <div className="text-xs opacity-70">
          © {new Date().getFullYear()} SENAI · Todos os direitos reservados.
        </div>
        <div
          aria-hidden
          className="pointer-events-none absolute -right-32 -top-32 h-[480px] w-[480px] rounded-full bg-primary-foreground/10 blur-3xl"
        />
      </aside>

      <main className="flex items-center justify-center bg-background p-6">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden">
            <div className="flex items-center gap-2 text-primary">
              <BookOpen className="h-6 w-6" />
              <span className="text-lg font-semibold">Biblioteca SENAI</span>
            </div>
          </div>

          <h2 className="text-2xl font-semibold">Entrar na sua conta</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Use suas credenciais institucionais.
          </p>

          <form onSubmit={submit} className="mt-8 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">E-mail</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seuemail@senai.br"
                required
                autoComplete="email"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Senha</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
            </div>
            <Button
              type="submit"
              className="h-11 w-full bg-primary text-primary-foreground hover:bg-primary-hover"
              disabled={loading}
            >
              {loading ? "Entrando..." : "Entrar"}
            </Button>
          </form>

          <div className="mt-8 rounded-lg border bg-card p-4 shadow-card">
            <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
              Contas de teste
            </p>
            <div className="mt-3 grid gap-2">
              <button
                onClick={() => fill("admin@senai.br", "admin123")}
                className="flex items-center justify-between rounded-md border bg-background px-3 py-2 text-left text-sm transition-colors hover:bg-accent"
              >
                <span className="flex items-center gap-2">
                  <ShieldCheck className="h-4 w-4 text-primary" />
                  <span className="font-medium">Administrador</span>
                </span>
                <code className="text-xs text-muted-foreground">
                  admin@senai.br · admin123
                </code>
              </button>
              <button
                onClick={() => fill("aluno@senai.br", "aluno123")}
                className="flex items-center justify-between rounded-md border bg-background px-3 py-2 text-left text-sm transition-colors hover:bg-accent"
              >
                <span className="flex items-center gap-2">
                  <UserIcon className="h-4 w-4 text-primary" />
                  <span className="font-medium">Usuário comum</span>
                </span>
                <code className="text-xs text-muted-foreground">
                  aluno@senai.br · aluno123
                </code>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
