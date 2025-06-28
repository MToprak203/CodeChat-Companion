import React from 'react';

type Props = {
  onClick: () => void;
  children: React.ReactNode;
  className?: string;
};

export const Button: React.FC<Props> = ({ onClick, children, className }) => {
  return (
    <button
      onClick={onClick}
      className={`px-4 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700 transition dark:bg-blue-500 dark:hover:bg-blue-600 ${className}`}
    >
      {children}
    </button>
  );
};
