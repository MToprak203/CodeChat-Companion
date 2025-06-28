import React, { useEffect, useRef, useState } from "react";
import ChatHeader from "./ChatHeader";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";
import ParticipantList from "./ParticipantList";
import { Conversation } from "../../entity/Conversation";
import { useChat } from "../../hooks/useChat";
import { useConversationNotifications } from "../../hooks/useConversationNotifications";
import SelectedFilesPanel from "./SelectedFilesPanel";
import {
  sendSelectedFiles,
  fetchSelectedFiles,
} from "../../services/projectService";
import { WS_BASE_URL } from "../../config";
import { createWebSocket } from "../../services/wsClient";
import {
  updateConversation,
  leaveConversation,
} from "../../services/conversationService";

type Props = {
  conversation: Conversation;
  projectId?: number;
  selectedFiles?: Set<string>;
  onUpdateTitle?: (title: string) => void;
  onDeleted?: () => void;
};

const ChatView: React.FC<Props> = ({
  conversation,
  projectId,
  selectedFiles,
  onUpdateTitle,
  onDeleted,
}) => {
  const {
    messages,
    participants,
    // no active participant tracking
    loadMore,
    sendMessage,
    reloadParticipants,
    stopAi,
    fetchUnread,
    aiWorking,
  } = useChat(conversation.id);
  const {
    notifications,
    clearNotifications,
  } = useConversationNotifications(
    conversation.id,
    fetchUnread,
    () => {
      stopAi();
      onDeleted?.();
    },
  );

  const [input, setInput] = useState("");
  const [visibleFiles, setVisibleFiles] = useState<string[]>([]);
  const scrollRef = useRef<HTMLDivElement>(null);
  const [autoScroll, setAutoScroll] = useState(true);
  const fileWsRef = useRef<WebSocket | null>(null);
  const handleInteraction = () => clearNotifications();

  useEffect(() => {
    if (autoScroll) {
      scrollRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages, notifications, autoScroll]);

  useEffect(() => {
    clearNotifications();
  }, []);

  useEffect(() => {
    if (!projectId) return;
    fetchSelectedFiles(projectId)
      .then(setVisibleFiles)
      .catch(() => {});
    const userId = localStorage.getItem("user_id");
    if (!userId) return;
    const ws = createWebSocket(
      `${WS_BASE_URL}/ws/${userId}/projects/${projectId}/selected-files`,
    );
    fileWsRef.current = ws;
    ws.onmessage = (e) => {
      try {
        const list = JSON.parse(e.data) as string[];
        setVisibleFiles(list);
      } catch (err) {
        console.error("Failed to parse selected files", err);
      }
    };
    return () => {
      ws.close();
      fileWsRef.current = null;
    };
  }, [projectId, conversation.id]);

  useEffect(() => {
    if (selectedFiles) {
      setVisibleFiles(Array.from(selectedFiles));
    }
  }, [selectedFiles]);

  useEffect(() => {
    if (projectId && selectedFiles) {
      sendSelectedFiles(projectId, Array.from(selectedFiles)).catch((e) =>
        console.error("Failed to send selected files", e),
      );
    }
  }, [projectId, selectedFiles]);


  const handleSend = () => {
    if (!input.trim()) return;
    if (projectId && selectedFiles) {
      const list = Array.from(selectedFiles);
      sendSelectedFiles(projectId, list).catch((e) =>
        console.error("Failed to send selected files", e),
      );
      setVisibleFiles(list);
    }
    sendMessage(input);
    setInput("");
  };

  const handleAskAi = () => {
    if (!input.trim()) return;
    if (projectId && selectedFiles) {
      const list = Array.from(selectedFiles);
      sendSelectedFiles(projectId, list).catch((e) =>
        console.error("Failed to send selected files", e),
      );
      setVisibleFiles(list);
    }
    sendMessage(input, true);
    setInput("");
  };

  return (
    <div
      className="relative flex h-full w-full bg-white dark:bg-gray-900 rounded-lg shadow-lg overflow-hidden p-4"
      onClick={handleInteraction}
      onFocus={handleInteraction}
    >
      <div className="flex flex-col flex-1 min-h-0">
        <ChatHeader
          title={conversation.title}
          editable
          onChangeTitle={async (t) => {
            try {
              const updated = await updateConversation(
                conversation.id.toString(),
                t,
              );
              onUpdateTitle?.(updated.title);
              window.dispatchEvent(
                new CustomEvent("conversationTitleUpdated", {
                  detail: { id: conversation.id, title: updated.title },
                }),
              );
            } catch (err) {
              console.error("Failed to update conversation", err);
            }
          }}
          onLeave={async () => {
            if (!window.confirm("Leave this conversation?")) return;
            try {
              stopAi();
              await leaveConversation(conversation.id.toString());
              window.dispatchEvent(
                new CustomEvent("conversationDeleted", {
                  detail: conversation.id.toString(),
                }),
              );
              onDeleted?.();
            } catch (err) {
              console.error("Failed to leave conversation", err);
            }
          }}
        />
        <SelectedFilesPanel files={visibleFiles} />
        <MessageList
          messages={messages}
          notifications={notifications}
          scrollRef={scrollRef}
          onScrollTopReached={loadMore}
          onScroll={setAutoScroll}
          onInteract={handleInteraction}
          participants={participants}
        />
        <MessageInput
          input={input}
          setInput={setInput}
          onSend={handleSend}
          onAskAi={handleAskAi}
          aiWorking={aiWorking}
          onStopAi={aiWorking ? stopAi : undefined}
          onInteract={handleInteraction}
        />
      </div>
      <ParticipantList
        participants={participants}
        conversationId={conversation.id.toString()}
        onUpdate={reloadParticipants}
      />
    </div>
  );
};

export default ChatView;
