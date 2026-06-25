import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type {
  Reservation,
  Resource,
  User,
} from "./types";

const API_URL = (import.meta.env.VITE_API_URL ?? "").replace(/\/$/, "");
const SESSION_KEY = "senai-biblioteca-session-v2";

interface State {
  users: User[];
  resources: Resource[];
  reservations: Reservation[];
}

interface Session {
  token: string;
  user: User;
}

const emptyState: State = {
  users: [],
  resources: [],
  reservations: [],
};

type Result<T = unknown> = { ok: boolean; message?: string } & T;

async function readError(response: Response) {
  const text = await response.text();
  if (!text) return "Operacao falhou.";
  try {
    const json = JSON.parse(text);
    return json.message ?? json.detail ?? json.error ?? text;
  } catch {
    return text;
  }
}

interface Ctx {
  state: State;
  currentUser: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<User | null>;
  logout: () => void;
  addResource: (input: Omit<Resource, "id" | "active">) => Promise<Result>;
  removeResource: (id: string) => Promise<Result>;
  addUser: (input: Omit<User, "id">) => Promise<Result>;
  removeUser: (id: string) => Promise<Result>;
  createReservation: (input: {
    resourceId: string;
    date: string;
    startHour: number;
    people: number;
    userId?: string;
  }) => Promise<Result<{ reservation?: Reservation }>>;
  cancelReservation: (id: string, opts?: { force?: boolean }) => Promise<Result>;
  decideReservation: (id: string, approve: boolean, reason?: string) => Promise<Result>;
  confirmReservation: (id: string) => Promise<Result>;
  checkIn: (id: string, opts?: { force?: boolean }) => Promise<Result>;
  checkOut: (id: string) => Promise<Result>;
  sweepNoShows: () => Promise<void>;
}

const StoreContext = createContext<Ctx | null>(null);

export function StoreProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<State>(emptyState);
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);

  const currentUser = session?.user ?? null;

  const request = useCallback(
    async <T,>(path: string, init: RequestInit = {}) => {
      const response = await fetch(`${API_URL}${path}`, {
        ...init,
        headers: {
          "Content-Type": "application/json",
          ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
          ...(init.headers ?? {}),
        },
      });

      if (!response.ok) {
        throw new Error(await readError(response));
      }

      if (response.status === 204) return undefined as T;
      return (await response.json()) as T;
    },
    [session?.token],
  );

  const refresh = useCallback(async () => {
    const data = await request<State>("/api/bootstrap");
    setState(data);
  }, [request]);

  useEffect(() => {
    const raw = window.localStorage.getItem(SESSION_KEY);
    if (!raw) {
      setLoading(false);
      return;
    }

    try {
      const parsed = JSON.parse(raw) as Session;
      setSession(parsed);
    } catch {
      window.localStorage.removeItem(SESSION_KEY);
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!session?.token) return;
    refresh()
      .catch(() => {
        window.localStorage.removeItem(SESSION_KEY);
        setSession(null);
      })
      .finally(() => setLoading(false));
  }, [session?.token, refresh]);

  const login: Ctx["login"] = useCallback(async (email, password) => {
    try {
      const data = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, senha: password }),
      });
      if (!data.ok) return null;

      const payload = (await data.json()) as { token: string; user: User };
      const nextSession = { token: payload.token, user: payload.user };
      setSession(nextSession);
      window.localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession));
      return payload.user;
    } catch {
      return null;
    }
  }, []);

  const logout = useCallback(() => {
    setSession(null);
    setState(emptyState);
    window.localStorage.removeItem(SESSION_KEY);
  }, []);

  const run = useCallback(
    async <T,>(fn: () => Promise<T>, after = true): Promise<Result<{ value?: T }>> => {
      try {
        const value = await fn();
        if (after) await refresh();
        return { ok: true, value };
      } catch (error) {
        return {
          ok: false,
          message: error instanceof Error ? error.message : "Operacao falhou.",
        };
      }
    },
    [refresh],
  );

  const addResource: Ctx["addResource"] = (input) =>
    run(() => request("/api/resources", { method: "POST", body: JSON.stringify(input) }));

  const removeResource: Ctx["removeResource"] = (id) =>
    run(() => request(`/api/resources/${id}`, { method: "DELETE" }));

  const addUser: Ctx["addUser"] = (input) =>
    run(() => request("/api/usuarios", { method: "POST", body: JSON.stringify(input) }));

  const removeUser: Ctx["removeUser"] = (id) =>
    run(() => request(`/api/usuarios/${id}`, { method: "DELETE" }));

  const createReservation: Ctx["createReservation"] = async (input) => {
    const result = await run<Reservation>(
      () => request("/api/reservations", { method: "POST", body: JSON.stringify(input) }),
      true,
    );
    return { ok: result.ok, message: result.message, reservation: result.value };
  };

  const cancelReservation: Ctx["cancelReservation"] = (id, opts) =>
    run(() => request(`/api/reservations/${id}/cancel?force=${Boolean(opts?.force)}`, { method: "POST" }));

  const decideReservation: Ctx["decideReservation"] = (id, approve, reason) =>
    run(() =>
      request(`/api/reservations/${id}/decision`, {
        method: "POST",
        body: JSON.stringify({ approve, reason }),
      }),
    );

  const confirmReservation: Ctx["confirmReservation"] = (id) =>
    run(() => request(`/api/reservations/${id}/confirm`, { method: "POST" }));

  const checkIn: Ctx["checkIn"] = (id, opts) =>
    run(() => request(`/api/reservations/${id}/check-in?force=${Boolean(opts?.force)}`, { method: "POST" }));

  const checkOut: Ctx["checkOut"] = (id) =>
    run(() => request(`/api/reservations/${id}/check-out`, { method: "POST" }));

  const sweepNoShows: Ctx["sweepNoShows"] = async () => {
    await run(() => request("/api/reservations/sweep", { method: "POST" }));
  };

  const value = useMemo<Ctx>(
    () => ({
      state,
      currentUser,
      loading,
      login,
      logout,
      addResource,
      removeResource,
      addUser,
      removeUser,
      createReservation,
      cancelReservation,
      decideReservation,
      confirmReservation,
      checkIn,
      checkOut,
      sweepNoShows,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [state, currentUser, loading],
  );

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>;
}

export function useStore() {
  const ctx = useContext(StoreContext);
  if (!ctx) throw new Error("useStore deve ser usado dentro de StoreProvider");
  return ctx;
}
