import React, { useRef, useEffect } from 'react';
import MessageBubble from './MessageBubble';
import NotificationBubble from './NotificationBubble';
import { Message } from '../../entity/Message';
import { User } from '../../entity/User';
import { AI_USER_ID, AI_USER_NAME } from '../../config';

type Props = {
  messages: Message[];
  notifications?: string[];
  scrollRef: React.RefObject<HTMLDivElement>;
  onScrollTopReached?: () => void;
  onScroll?: (atBottom: boolean) => void;
  onInteract?: () => void;
  participants: User[];
};

const MessageList: React.FC<Props> = ({
  messages,
  notifications = [],
  scrollRef,
  onScrollTopReached,
  onScroll,
  onInteract,
  participants,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);

  const currentUserId = localStorage.getItem('user_id');

  const isCurrentUser = (id: string) => id === currentUserId;

  const getName = (id: string) => {
    if (id === currentUserId) return 'You';
    if (id === AI_USER_ID.toString()) return AI_USER_NAME;
    const user = participants.find((p) => p.id === id);
    return user?.username || 'Unknown';
  };

  useEffect(() => {
    const div = containerRef.current;
    if (!div) return;

    let prevTop = div.scrollTop;

    const handleScroll = () => {
      if (onScrollTopReached && div.scrollTop < 50) {
        onScrollTopReached();
      }
      onInteract?.();
      if (onScroll) {
        if (div.scrollTop < prevTop) {
          onScroll(false);
        } else {
          const atBottom =
            div.scrollHeight - div.scrollTop - div.clientHeight <= 1;
          onScroll(atBottom);
        }
      }
      prevTop = div.scrollTop;
    };

    const handleClick = () => onInteract?.();

    div.addEventListener('scroll', handleScroll);
    div.addEventListener('click', handleClick);
    return () => {
      div.removeEventListener('scroll', handleScroll);
      div.removeEventListener('click', handleClick);
    };
  }, [onScrollTopReached, onInteract, onScroll]);

  return (
    <div
      ref={containerRef}
      className="flex-1 overflow-y-auto styled-scrollbar p-4 space-y-2 bg-gray-50 dark:bg-gray-800"
    >
      {messages.map((msg, idx) => (
        <MessageBubble
          key={`${msg.id}-${idx}`}
          name={getName(msg.sender)}
          text={msg.text}
          isYou={isCurrentUser(msg.sender)}
          isDraft={msg.id === 'draft'}
        />
      ))}
      {notifications
        .filter((n, idx, arr) => arr.indexOf(n) === idx)
        .map((n, idx) => (
          <NotificationBubble key={`${n}-${idx}`} text={n} />
        ))}
      <div ref={scrollRef} />
    </div>
  );
};

export default MessageList;
