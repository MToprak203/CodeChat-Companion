import { WS_BASE_URL } from '../config';

export function createWebSocket(url: string, protocols?: string | string[]): WebSocket {
  const token = localStorage.getItem('auth_token');
  const wsUrl = new URL(url.startsWith('ws') ? url : `${WS_BASE_URL}${url}`);
  if (token) {
    wsUrl.searchParams.append('access_token', token);
  }
  return new WebSocket(wsUrl.toString(), protocols);
}
