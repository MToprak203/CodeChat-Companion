import { useEffect, useRef, useState } from 'react';
import { createWebSocket } from '../services/wsClient';

export type SystemNotification = {
  message: string;
  type: 'success' | 'error' | 'info';
};

function inferType(msg: string): 'success' | 'error' | 'info' {
  const lower = msg.toLowerCase();
  if (lower.includes('fail') || lower.includes('error')) return 'error';
  if (lower.includes('success') || lower.includes('completed')) return 'success';
  return 'info';
}

export function useSystemNotifications() {
  const [notifications, setNotifications] = useState<SystemNotification[]>([]);
  const wsRef = useRef<WebSocket | null>(null);

  const removeNotification = (idx: number) => {
    setNotifications((prev) => prev.filter((_, i) => i !== idx));
  };

  useEffect(() => {
    const userId = localStorage.getItem('user_id');
    if (!userId) return;

    let attempts = 0;
    let stop = false;

    const connect = () => {
      const ws = createWebSocket(`/ws/${userId}/notifications`);
      wsRef.current = ws;
      ws.onmessage = (e) => {
        if (e.data.startsWith('Conversation deleted:')) {
          const id = e.data.split(':')[1]?.trim();
          if (id) {
            window.dispatchEvent(
              new CustomEvent('conversationDeleted', { detail: id }),
            );
          }
          return;
        }
        if (e.data.startsWith('Project deleted:')) {
          const id = e.data.split(':')[1]?.trim();
          if (id) {
            window.dispatchEvent(new CustomEvent('projectDeleted', { detail: id }));
          }
          return;
        }
        if (e.data === 'Message Processed') return;
        setNotifications((prev) => [
          ...prev,
          { message: e.data, type: inferType(e.data) },
        ]);
      };
      ws.onerror = () => {
        ws.close();
      };
      ws.onclose = (e) => {
        if (stop || e.code === 1000) return;
        const delay = Math.min(10000, 1000 * 2 ** attempts);
        attempts += 1;
        setTimeout(connect, delay);
      };
    };

    connect();

    return () => {
      stop = true;
      wsRef.current?.close();
    };
  }, []);

  return [notifications, removeNotification] as const;
}
