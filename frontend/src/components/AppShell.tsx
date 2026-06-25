import { Link, useNavigate, useRouterState } from "@tanstack/react-router";
import { BookOpen, LogOut } from "lucide-react";
import type { ReactNode } from "react";
import { useEffect } from "react";
import { useStore } from "@/lib/store";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

interface NavItem {
  to: string;
  label: string;
}

interface AppShellProps {
  children: ReactNode;
  nav: NavItem[];
  title: string;
}

export function AppShell({ children, nav, title }: AppShellProps) {
  const { currentUser, logout, sweepNoShows } = useStore();
  const navigate = useNavigate();
  const { location } = useRouterState();

  useEffect(() => {
    sweepNoShows();
    const id = setInterval(sweepNoShows, 60_000);
    return () => clearInterval(id);
  }, [sweepNoShows]);

  if (!currentUser) return null;

  const handleLogout = () => {
    logout();
    navigate({ to: "/login" });
  };

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-30 border-b bg-card/90 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="grid h-9 w-9 place-items-center rounded-md bg-primary text-primary-foreground">
              <BookOpen className="h-5 w-5" />
            </div>
            <div className="leading-tight">
              <p className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
                SENAI
              </p>
              <p className="text-sm font-semibold">{title}</p>
            </div>
          </div>

          <nav className="hidden items-center gap-1 md:flex">
            {nav.map((item) => {
              const active =
                location.pathname === item.to ||
                (item.to !== "/" && location.pathname.startsWith(item.to));
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={cn(
                    "rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    active
                      ? "bg-primary/10 text-primary"
                      : "text-muted-foreground hover:bg-accent hover:text-accent-foreground",
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>

          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium">{currentUser.name}</p>
              <p className="text-xs text-muted-foreground">
                {currentUser.role === "admin" ? "Administrador" : "Usuário"}
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={handleLogout}
              className="gap-2"
            >
              <LogOut className="h-4 w-4" />
              <span className="hidden sm:inline">Sair</span>
            </Button>
          </div>
        </div>

        <nav className="border-t md:hidden">
          <div className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-2 py-2">
            {nav.map((item) => {
              const active =
                location.pathname === item.to ||
                (item.to !== "/" && location.pathname.startsWith(item.to));
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={cn(
                    "shrink-0 rounded-md px-3 py-1.5 text-sm font-medium transition-colors",
                    active
                      ? "bg-primary/10 text-primary"
                      : "text-muted-foreground hover:bg-accent",
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </div>
        </nav>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
