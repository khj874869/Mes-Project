export function getToken(): string | null {
  return localStorage.getItem("mes_token");
}

export function setToken(token: string | null) {
  if (!token) localStorage.removeItem("mes_token");
  else localStorage.setItem("mes_token", token);
}

function withAuth(headers?: Record<string, string>): Record<string, string> {
  const token = getToken();
  return {
    ...(headers ?? {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  };
}

export async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(path, {
    headers: {
      "Accept": "application/json",
      ...withAuth()
    }
  });
  if (!res.ok) throw new Error(`GET ${path} -> ${res.status}`);
  return res.json() as Promise<T>;
}

export async function postJson<T>(path: string, body: unknown, headers?: Record<string, string>): Promise<T> {
  const res = await fetch(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json",
      ...withAuth(headers)
    },
    body: JSON.stringify(body)
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`${path} -> ${res.status}: ${text}`);
  try { return JSON.parse(text) as T; } catch { return text as unknown as T; }
}
