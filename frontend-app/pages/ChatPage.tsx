import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import ChatPlaceholder from '../components/ChatPlaceholder';
import ChatView from '../components/chat/ChatView';
import { Conversation } from '../entity/Conversation';
import { logout } from '../services/authService';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ChatPage: React.FC = () => {
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null);
  const navigate = useNavigate();
  const { setLoggedIn } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      setLoggedIn(false);
      navigate('/login');
    }
  };

  return (
    <div className="flex h-screen bg-gradient-to-br from-gray-100 to-gray-50 dark:from-gray-900 dark:to-gray-800 text-gray-900 dark:text-gray-100">
      <Sidebar
        onSelectConversation={(c) => setActiveConversation(c)}
        onLogout={handleLogout}
      />

      <div className="flex-1 flex p-4 overflow-hidden">
        {activeConversation ? (
          <ChatView
            conversation={activeConversation}
            onUpdateTitle={(t) =>
              setActiveConversation({ ...activeConversation, title: t })
            }
            onDeleted={() => setActiveConversation(null)}
          />
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <ChatPlaceholder />
          </div>
        )}
      </div>
    </div>
  );
};

export default ChatPage;
