import { getToken } from "./http";

// EventSource는 헤더(Authorization)를 넣기 어려워 querystring으로 토큰을 전달합니다.
export function withAccessToken(urlPath: string): string {
  const token = getToken();
  if (!token) return urlPath;
  const u = new URL(urlPath, window.location.origin);
  u.searchParams.set("access_token", token);
  return u.toString();
}
