import React from 'react';

type Props = {
  input: string;
  setInput: (value: string) => void;
  onSend: () => void;
  onAskAi?: () => void;
  aiWorking?: boolean;
  onStopAi?: () => void;
  onInteract?: () => void;
};

const MessageInput: React.FC<Props> = ({ input, setInput, onSend, onAskAi, aiWorking, onStopAi, onInteract }) => (
  <div className="p-4 border-t bg-white/75 dark:bg-gray-900/75 backdrop-blur">
    <div className="flex gap-2">
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onFocus={onInteract}
        onClick={onInteract}
        placeholder="Type a message..."
        disabled={aiWorking}
        className="flex-1 border px-4 py-2 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-gray-700 border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 disabled:opacity-50"
      />
      <button
        onClick={onSend}
        disabled={aiWorking}
        className="bg-blue-600 text-white px-4 py-2 rounded-xl hover:bg-blue-700 transition dark:bg-blue-500 dark:hover:bg-blue-600 disabled:opacity-50"
      >
        Send
      </button>
      {onAskAi && (
        <button
          onClick={onAskAi}
          disabled={aiWorking}
          className="bg-purple-600 text-white px-4 py-2 rounded-xl hover:bg-purple-700 transition dark:bg-purple-500 dark:hover:bg-purple-600 disabled:opacity-50"
        >
          Ask AI
        </button>
      )}
      {aiWorking && onStopAi && (
        <button
          onClick={onStopAi}
          className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-700 transition flex items-center justify-center"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            className="h-4 w-4 fill-current"
          >
            <rect x="4" y="4" width="16" height="16" rx="2" />
          </svg>
        </button>
      )}
    </div>
  </div>
);

export default MessageInput;
