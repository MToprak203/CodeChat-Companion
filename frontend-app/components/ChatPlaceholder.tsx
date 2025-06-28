import React from 'react';

const ChatPlaceholder: React.FC = () => {
  return (
    <div className="text-center text-gray-500 dark:text-gray-400">
      <h2 className="text-2xl font-semibold">Start a new conversation</h2>
      <p className="mt-2">Select a chat or create a new one from the sidebar.</p>
    </div>
  );
};

export default ChatPlaceholder;
