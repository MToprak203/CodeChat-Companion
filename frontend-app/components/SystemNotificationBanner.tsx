import React from 'react';
import { SystemNotification } from '../hooks/useSystemNotifications';

type Props = {
  notifications: SystemNotification[];
  onClose: (idx: number) => void;
};

const SystemNotificationBanner: React.FC<Props> = ({ notifications, onClose }) => {
  if (notifications.length === 0) return null;
  return (
    <div className="fixed top-0 right-0 m-4 space-y-2 z-50">
      {notifications.map((n, idx) => (
        <div
          key={idx}
          className={`${
            n.type === 'success'
              ? 'bg-green-500'
              : n.type === 'error'
              ? 'bg-red-500'
              : 'bg-blue-500'
          } text-white px-3 py-2 rounded shadow cursor-pointer`}
          onClick={() => onClose(idx)}
        >
          {n.message}
        </div>
      ))}
    </div>
  );
};

export default SystemNotificationBanner;
