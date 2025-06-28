import React from 'react';
import ConversationSection from './sidebar/ConversationSection';
import ProjectSection from './sidebar/ProjectSection';
import { Conversation } from '../entity/Conversation';

type Props = {
  onSelectConversation: (conversation: Conversation) => void;
  onLogout: () => void;
};

const Sidebar: React.FC<Props> = ({
  onSelectConversation,
  onLogout,
}) => {
  return (
    <div className="w-72 h-full bg-white dark:bg-gray-900 border-r dark:border-gray-700 flex flex-col p-4 space-y-4 shadow-lg overflow-y-auto styled-scrollbar">
      <ConversationSection onSelect={onSelectConversation} />
      <ProjectSection />
      <button
        onClick={onLogout}
        className="mt-auto px-4 py-2 rounded-lg text-sm font-semibold bg-red-600 text-white hover:bg-red-700 transition-colors shadow-md dark:shadow-none"
      >
        Logout
      </button>
    </div>
  );
};


export default Sidebar;
