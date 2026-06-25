import { createFileRoute, Navigate } from "@tanstack/react-router";
import { useStore } from "@/lib/store";

export const Route = createFileRoute("/")({
  component: Index,
});

function Index() {
  const { currentUser, loading } = useStore();
  if (loading) return null;
  if (!currentUser) return <Navigate to="/login" />;
  return currentUser.role === "admin" ? (
    <Navigate to="/admin" />
  ) : (
    <Navigate to="/app" />
  );
}
