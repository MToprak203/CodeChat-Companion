import { useEffect, useRef, useState } from "react";
import { WS_BASE_URL } from "../config";
import { createWebSocket } from "../services/wsClient";

export function useConversationNotifications(
  conversationId: string,
  onNewMessage?: () => void,
  onDeleted?: () => void,
) {
  const [notifications, setNotifications] = useState<string[]>([]);
  const clearNotifications = () => setNotifications([]);
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    const userId = localStorage.getItem("user_id");
    if (!userId) return;

    let attempts = 0;
    let stop = false;

    const connect = () => {
      const ws = createWebSocket(
        `/ws/${userId}/conversations/${conversationId}/notify`,
      );
      wsRef.current = ws;
      ws.onmessage = (e) => {
        setNotifications((prev) => [...prev, e.data]);
        if (e.data === "new-message") {
          onNewMessage?.();
        } else if (e.data === "deleted") {
          onDeleted?.();
        }
      };
      ws.onerror = () => ws.close();
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
  }, [conversationId]);

  useEffect(() => {
    const handler = (e: Event) => {
      const id = (e as CustomEvent<string>).detail;
      if (id === conversationId) {
        onDeleted?.();
      }
    };
    window.addEventListener("conversationDeleted", handler);
    return () => window.removeEventListener("conversationDeleted", handler);
  }, [conversationId, onDeleted]);


  useEffect(() => {
    clearNotifications();
  }, [conversationId]);

  return { notifications, clearNotifications };
}
