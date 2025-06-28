import React from 'react';

type Props = {
  title: string;
  selected: boolean;
  unread?: boolean;
  onClick: () => void;
};

const ConversationItem: React.FC<Props> = ({
  title,
  selected,
  unread = false,
  onClick,
}) => {
  return (
    <div
      onClick={onClick}
      className={`relative px-3 py-2 rounded-lg cursor-pointer transition truncate
        ${selected ? 'bg-blue-100 text-blue-800 font-medium dark:bg-blue-500 dark:text-white' : 'hover:bg-gray-100 text-gray-700 dark:text-gray-200 dark:hover:bg-gray-700'}`}
    >
      <span className="ml-3">{title}</span>
      {unread && !selected && (
        <span className="absolute right-2 top-1/2 -translate-y-1/2 h-2 w-2 bg-red-500 rounded-full" />
      )}
    </div>
  );
};

export default ConversationItem;
