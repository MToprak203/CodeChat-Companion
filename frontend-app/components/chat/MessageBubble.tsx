import React from 'react';
import MarkdownRenderer from '../MarkdownRenderer';
import 'highlight.js/styles/atom-one-dark.css';

type Props = {
  name: string;
  text: string;
  isYou?: boolean;
  isDraft?: boolean;
};

const MessageBubble: React.FC<Props> = ({
  name,
  text,
  isYou = false,
  isDraft = false,
}) => {
  return (
    <div className={`flex flex-col ${isYou ? 'items-end' : 'items-start'}`}>
      <span className="text-xs text-gray-500 mb-1">{name}</span>
      <div
        className={`max-w-md px-4 py-2 rounded-2xl shadow ${
          isYou
            ? 'bg-blue-600 text-white self-end ml-auto dark:bg-blue-500'
            : 'bg-gray-200 text-gray-800 self-start mr-auto dark:bg-gray-700 dark:text-gray-100'
        }`}
      >
        
          <MarkdownRenderer
            text={text}
            className="text-sm whitespace-pre-wrap break-words"
          />
        
      </div>
    </div>
  );
};

export default MessageBubble;
