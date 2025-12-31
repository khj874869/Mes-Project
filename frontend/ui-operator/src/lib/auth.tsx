import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { CORE } from "./api";
import { getJson, postJson, setToken, getToken } from "./http";

export type Role = "ADMIN" | "USER";
export type Me = { username: string; displayName: string; role: Role };

type AuthState = {
  me: Me | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthCtx = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [me, setMe] = useState<Me | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = async () => {
    const token = getToken();
    if (!token) { setMe(null); return; }
    try {
      const m = await getJson<Me>(`${CORE}/auth/me`);
      setMe(m);
    } catch {
      setToken(null);
      setMe(null);
    }
  };

  useEffect(() => {
    setLoading(true);
    refreshMe().finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const login = async (username: string, password: string) => {
    const res = await postJson<{ token: string; username: string; displayName: string; role: Role }>(
      `${CORE}/auth/login`,
      { username, password }
    );
    setToken(res.token);
    setMe({ username: res.username, displayName: res.displayName, role: res.role });
  };

  const logout = () => {
    setToken(null);
    setMe(null);
  };

  const value = useMemo(() => ({ me, loading, login, logout }), [me, loading]);
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthCtx);
  if (!ctx) throw new Error("AuthProvider missing");
  return ctx;
}
