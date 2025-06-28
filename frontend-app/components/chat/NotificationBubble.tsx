import React from 'react';

type Props = {
  text: string;
};

const NotificationBubble: React.FC<Props> = ({ text }) => (
  <div className="text-center text-xs text-gray-500 my-1">{text}</div>
);

export default NotificationBubble;
