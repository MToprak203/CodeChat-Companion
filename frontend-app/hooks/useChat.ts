import { useEffect, useRef, useState } from 'react';
import { fetchMessages, fetchUnreadMessages } from '../services/messageService';
import { fetchParticipants } from '../services/conversationService';
import { stopConversationAi } from '../services/aiService';
import { Message, Recipient, MessageEvent } from '../entity/Message';
import { User } from '../entity/User';
import { WS_BASE_URL } from '../config';
import { createWebSocket } from '../services/wsClient';

const PAGE_SIZE = 20;
const END_TOKEN = '[DONE]';

type SendMessagePayload = {
  messageId: string;
  conversationId: number;
  senderId: number;
  text: string;
  type: string;
  recipient: Recipient;
};

export function useChat(conversationId: string) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [participants, setParticipants] = useState<User[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [aiDraft, setAiDraft] = useState('');
  const [aiWorking, setAiWorking] = useState(false);

  const loadingRef = useRef(false);
  const msgWsRef = useRef<WebSocket | null>(null);
  const tokenWsRef = useRef<WebSocket | null>(null);

  const mapEvents = (events: MessageEvent[]): Message[] =>
    events.map((e) => ({
      id: e.messageId,
      sender: e.senderId.toString(),
      text: e.text,
      recipient: e.recipient,
    }));

  const addMessages = (incoming: Message[], atStart = false) => {
    setMessages((prev) => {
      const ids = new Set(prev.map((m) => m.id));
      const filtered = incoming.filter((m) => !ids.has(m.id));
      return atStart ? [...filtered, ...prev] : [...prev, ...filtered];
    });
  };

  const loadMessages = async (pageToLoad: number, replace = false) => {
    try {
      loadingRef.current = true;
      const res = await fetchMessages(conversationId, pageToLoad, PAGE_SIZE);
      const list = res.data ? mapEvents(res.data).reverse() : [];
      if (replace) {
        setMessages(list);
      } else {
        addMessages(list, true);
      }
      if (res.meta) {
        setPage(res.meta.page);
        setTotalPages(res.meta.totalPages);
      }
    } catch (err) {
      console.error('[chat] Failed to load messages:', err);
      if (replace) setMessages([]);
    } finally {
      loadingRef.current = false;
    }
  };

  const loadMore = async () => {
    if (loadingRef.current || page + 1 >= totalPages) return;
    await loadMessages(page + 1);
  };

  const reloadParticipants = async () => {
    try {
      const list = await fetchParticipants(conversationId);
      setParticipants(list);
    } catch (err) {
      console.error('Failed to fetch participants', err);
    }
  };

  const openTokenWs = () => {
    const userId = localStorage.getItem('user_id');
    if (!userId) return;
    const ws = createWebSocket(
      `${WS_BASE_URL}/ws/${userId}/conversations/${conversationId}/tokens`
    );
    tokenWsRef.current = ws;
    ws.onmessage = (e) => {
      if (e.data === END_TOKEN) {
        tokenWsRef.current?.close();
        tokenWsRef.current = null;
        setAiWorking(false);
        return;
      }
      setAiWorking(true);
      setAiDraft((prev) => prev + e.data);
    };
    ws.onclose = () => setAiWorking(false);
  };

  const stopAi = () => {
    stopConversationAi(conversationId).catch(() => {});
    tokenWsRef.current?.close();
    tokenWsRef.current = null;
    setAiWorking(false);
    if (aiDraft) {
      addMessages([
        { id: crypto.randomUUID(), sender: '-1', text: aiDraft, recipient: 'AI' },
      ]);
      setAiDraft('');
    }
  };

  const sendMessage = (text: string, toAi = false) => {
    if (!text.trim()) return;
    if (toAi) {
      stopAi();
      setAiDraft('');
      openTokenWs();
    }
    const userId = localStorage.getItem('user_id');
    if (!userId || !msgWsRef.current) return;

    const payload: SendMessagePayload = {
      messageId: crypto.randomUUID(),
      conversationId: Number(conversationId),
      senderId: Number(userId),
      text: text.trim(),
      type: 'TEXT',
      recipient: toAi ? 'AI' : 'USERS',
    };

    addMessages([
      {
        id: payload.messageId,
        sender: userId,
        text: payload.text,
        recipient: payload.recipient,
      },
    ]);

    msgWsRef.current.send(JSON.stringify(payload));
  };

  const fetchUnread = async () => {
    try {
      const res = await fetchUnreadMessages(conversationId);
      if (res.data) {
        const list = mapEvents(res.data).reverse();
        addMessages(list);
      }
    } catch (err) {
      console.error('[chat] Failed to fetch unread messages', err);
    }
  };

  useEffect(() => {
    setMessages([]);
    setPage(0);
    setTotalPages(0);
    setAiDraft('');
    setAiWorking(false);
    setParticipants([]);

    loadMessages(0, true);
    fetchUnread();

    const userId = localStorage.getItem('user_id');
    if (userId) {
      const ws = createWebSocket(
        `${WS_BASE_URL}/ws/${userId}/conversations/${conversationId}/messages`
      );
      msgWsRef.current = ws;
      ws.onmessage = (e) => {
        try {
          const msg = JSON.parse(e.data) as MessageEvent;
          addMessages([
            {
              id: msg.messageId,
              sender: msg.senderId.toString(),
              text: msg.text,
              recipient: msg.recipient,
            },
          ]);
          if (msg.senderId === -1) {
            tokenWsRef.current?.close();
            tokenWsRef.current = null;
            setAiDraft('');
            setAiWorking(false);
          }
        } catch (err) {
          console.error('Failed to parse ws message', err);
        }
      };
    }

    reloadParticipants();

    return () => {
      msgWsRef.current?.close();
      tokenWsRef.current?.close();
      msgWsRef.current = null;
      tokenWsRef.current = null;
    };
  }, [conversationId]);

  useEffect(() => {
    if (participants.length > 0) return;
    let attempts = 0;
    const interval = setInterval(() => {
      attempts += 1;
      reloadParticipants();
      if (attempts >= 5) {
        clearInterval(interval);
      }
    }, 1000);
    return () => clearInterval(interval);
  }, [participants.length, conversationId]);

  const displayMessages = aiDraft
    ? [...messages, { id: 'draft', sender: '-1', text: aiDraft, recipient: 'AI' }]
    : messages;

  return {
    messages: displayMessages,
    participants,
    loadMore,
    sendMessage,
    reloadParticipants,
    stopAi,
    fetchUnread,
    aiWorking,
  };
}
