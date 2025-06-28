import React, { useEffect, useRef, useState } from 'react';
import ConversationItem from './ConversationItem';
import {
  createProjectConversation,
  fetchProjectConversations,
} from '../../services/projectService';
import { Conversation } from '../../entity/Conversation';
import { createWebSocket } from '../../services/wsClient';

interface Props {
  projectId: number;
  onSelect: (conversation: Conversation) => void;
}

const ProjectConversationSection: React.FC<Props> = ({ projectId, onSelect }) => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const wsRefs = useRef<Record<string, WebSocket>>({});

  const setupNotification = (conversationId: string) => {
    const userId = localStorage.getItem('user_id');
    if (!userId || wsRefs.current[conversationId]) return;

    const ws = createWebSocket(
      `/ws/${userId}/conversations/${conversationId}/notify`
    );

    ws.onmessage = () => {
      /* new message */
    };

    wsRefs.current[conversationId] = ws;
  };

  const load = () =>
    fetchProjectConversations(projectId)
      .then((list) => {
        setConversations(list);
        if (list.length > 0) {
          setSelectedId(list[0].id);
          onSelect(list[0]);
        }
        list.forEach((c) => setupNotification(c.id));
      })
      .catch(() => {});

  useEffect(() => {
    const handler = (e: Event) => {
      const detail = (e as CustomEvent<{ id: string; title: string }>).detail;
      setConversations((prev) =>
        prev.map((c) => (c.id === detail.id ? { ...c, title: detail.title } : c))
      );
    };
    window.addEventListener('conversationTitleUpdated', handler);
    const delHandler = () => {
      load();
    };
    window.addEventListener('conversationDeleted', delHandler);
    window.addEventListener('projectDeleted', delHandler);
    return () => {
      window.removeEventListener('conversationTitleUpdated', handler);
      window.removeEventListener('conversationDeleted', delHandler);
      window.removeEventListener('projectDeleted', delHandler);
    };
  }, []);

  useEffect(() => {
    load();
  }, [projectId]);

  useEffect(() => {
    return () => {
      Object.values(wsRefs.current).forEach((ws) => ws.close());
      wsRefs.current = {};
    };
  }, []);

  const handleCreate = async () => {
    try {
      const c = await createProjectConversation(projectId);
      await load();
      setSelectedId(c.id);
      onSelect(c);
    } catch (err) {
      console.error('Failed to create conversation', err);
    }
  };

  return (
    <section className="mb-4 flex flex-col">
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-200">
          Conversations
        </h3>
        <button
          type="button"
          onClick={handleCreate}
          className="text-blue-600 text-lg leading-none"
        >
          +
        </button>
      </div>
      <div className="space-y-1 styled-scrollbar overflow-y-auto max-h-[40vh]">
        {conversations.length > 0 ? (
          conversations.map((c) => (
            <ConversationItem
              key={c.id}
              title={c.title}
              selected={c.id === selectedId}
              onClick={() => {
                setSelectedId(c.id);
                onSelect(c);
              }}
            />
          ))
        ) : (
          <p className="text-gray-400 dark:text-gray-500 text-sm">No conversations yet.</p>
        )}
      </div>
    </section>
  );
};

export default ProjectConversationSection;
