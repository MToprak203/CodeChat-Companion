import React, { useEffect, useRef, useState } from 'react';
import ConversationItem from './ConversationItem';
import {
  fetchConversations,
  createConversation,
} from '../../services/conversationService';
import { Conversation } from '../../entity/Conversation';
import { createWebSocket } from '../../services/wsClient';

const PAGE_SIZE = 20;

type Props = {
  onSelect: (conversation: Conversation) => void;
};

const ConversationSection: React.FC<Props> = ({ onSelect }) => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState<number | null>(null);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [unreadIds, setUnreadIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(false);
  const loadingRef = useRef(false);
  const listRef = useRef<HTMLDivElement>(null);
  const wsRefs = useRef<Record<string, WebSocket>>({});

  const setupNotification = (conversationId: string) => {
    const userId = localStorage.getItem('user_id');
    if (!userId || wsRefs.current[conversationId]) return;

    const ws = createWebSocket(
      `/ws/${userId}/conversations/${conversationId}/notify`
    );

    ws.onmessage = (e) => {
      if (e.data === 'deleted') {
        ws.close();
        delete wsRefs.current[conversationId];
        refresh();
      } else {
        setUnreadIds((prev) => new Set(prev).add(conversationId));
      }
    };

    wsRefs.current[conversationId] = ws;
  };

  useEffect(() => {
    loadMore();
  }, []);

  // Ensure at least one visible conversation is loaded without requiring
  // user interaction. If the current page yielded no results and more
  // pages are available, automatically fetch the next page.
  useEffect(() => {
    if (
      conversations.length === 0 &&
      totalPages !== null &&
      page < totalPages &&
      !loadingRef.current
    ) {
      loadMore();
    }
  }, [conversations.length, page, totalPages]);

  // If the list isn't scrollable but more pages are available, automatically
  // fetch additional pages so the user has something to scroll through.
  useEffect(() => {
    const el = listRef.current;
    if (
      el &&
      el.scrollHeight <= el.clientHeight &&
      totalPages !== null &&
      page < totalPages &&
      !loadingRef.current
    ) {
      loadMore();
    }
  }, [conversations.length, page, totalPages]);

  useEffect(() => {
    const handler = (e: Event) => {
      const detail = (e as CustomEvent<{ id: string; title: string }>).detail;
      setConversations((prev) =>
        prev.map((c) => (c.id === detail.id ? { ...c, title: detail.title } : c))
      );
    };
    window.addEventListener('conversationTitleUpdated', handler);
    const delHandler = () => {
      refresh();
    };
    const projectDelHandler = () => {
      refresh();
    };
    window.addEventListener('conversationDeleted', delHandler);
    window.addEventListener('projectDeleted', projectDelHandler);
    return () => {
      window.removeEventListener('conversationTitleUpdated', handler);
      window.removeEventListener('conversationDeleted', delHandler);
      window.removeEventListener('projectDeleted', projectDelHandler);
    };
  }, []);


  useEffect(() => {
    return () => {
      Object.values(wsRefs.current).forEach((ws) => ws.close());
      wsRefs.current = {};
    };
  }, []);

  const loadMore = async () => {
    if (loadingRef.current || (totalPages !== null && page >= totalPages)) return;

    loadingRef.current = true;
    setLoading(true);
    try {
      const { items, meta } = await fetchConversations(page, PAGE_SIZE);
      const filtered = items.filter((c) => !c.projectId);
      setConversations((prev) => [...prev, ...filtered]);
      if (selectedId === null && filtered.length > 0) {
        setSelectedId(filtered[0].id);
        onSelect(filtered[0]);
      }
      filtered.forEach((c) => setupNotification(c.id));
      setPage((prev) => prev + 1);
      setTotalPages(meta.totalPages);
    } catch (err) {
      console.error('Failed to load conversations', err);
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  };

  const refresh = async (newlyCreated?: Conversation) => {
    try {
      const { items, meta } = await fetchConversations(0, PAGE_SIZE);
      const filtered = items.filter((c) => !c.projectId);
      setConversations(filtered);
      filtered.forEach((c) => setupNotification(c.id));
      setTotalPages(meta.totalPages);
      setPage(1);

      if (newlyCreated) {
        setSelectedId(newlyCreated.id);
        onSelect(newlyCreated);
      }
    } catch (err) {
      console.error('Failed to refresh conversations', err);
    }
  };

  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
    if (scrollTop + clientHeight >= scrollHeight - 50 && !loadingRef.current) {
      loadMore();
    }
  };

  const handleStartNewChat = async () => {
    try {
      const newConversation = await createConversation();
      await refresh(newConversation);
    } catch (err) {
      console.error('Failed to create conversation', err);
    }
  };

  return (
    <section className="flex flex-col">
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-200">Conversations</h3>
        <button
          onClick={handleStartNewChat}
          className="text-blue-600 text-lg leading-none"
        >
          +
        </button>
      </div>

      <div
        ref={listRef}
        className="space-y-1 styled-scrollbar overflow-y-auto max-h-[40vh]"
        onScroll={handleScroll}
      >
        {conversations.map((c) => (
          <ConversationItem
            key={c.id}
            title={c.title}
            selected={c.id === selectedId}
            unread={unreadIds.has(c.id)}
            onClick={() => {
              setSelectedId(c.id);
              setUnreadIds((prev) => {
                const next = new Set(prev);
                next.delete(c.id);
                return next;
              });
              onSelect(c);
            }}
          />
        ))}

        {loading ? (
          <p className="text-center text-xs text-gray-400 dark:text-gray-500 py-2">Loading...</p>
        ) : conversations.length === 0 && totalPages !== null && page >= totalPages ? (
          <p className="text-gray-400 dark:text-gray-500 text-sm">No conversations yet.</p>
        ) : null}
      </div>
    </section>
  );
};

export default ConversationSection;
