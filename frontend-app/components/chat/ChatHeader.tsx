import React, { useState, useEffect } from 'react';

type Props = {
  title: string;
  editable?: boolean;
  onChangeTitle?: (title: string) => void;
  onLeave?: () => void;
};

const ChatHeader: React.FC<Props> = ({ title, editable = false, onChangeTitle, onLeave }) => {
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState(title);

  useEffect(() => setValue(title), [title]);

  const finish = () => {
    setEditing(false);
    const trimmed = value.trim();
    if (trimmed && trimmed !== title) {
      onChangeTitle?.(trimmed);
    } else {
      setValue(title);
    }
  };

  return (
    <div className="p-4 border-b bg-white dark:bg-gray-900 flex items-center justify-between font-semibold text-lg shadow-sm">
      <div className="flex-1">
        {editing ? (
          <input
            className="bg-transparent border-b border-gray-300 focus:outline-none w-full"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            onBlur={finish}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                finish();
              } else if (e.key === 'Escape') {
                setValue(title);
                setEditing(false);
              }
            }}
            autoFocus
          />
        ) : (
          <span
            className={editable ? 'cursor-pointer' : undefined}
            onClick={() => editable && setEditing(true)}
          >
            {title}
          </span>
        )}
      </div>
      {onLeave && (
        <button
          onClick={onLeave}
          className="text-red-600 text-sm ml-2 hover:text-red-800"
        >
          Leave
        </button>
      )}
    </div>
  );
};

export default ChatHeader;
