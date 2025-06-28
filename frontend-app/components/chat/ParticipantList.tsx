import React from 'react';
import { User } from '../../entity/User';

import { addParticipant, removeParticipant, fetchParticipants } from '../../services/conversationService';

type Props = {
  participants: User[];
  conversationId: string;
  onUpdate?: (list: User[]) => void;
};

const ParticipantList: React.FC<Props> = ({ participants, conversationId, onUpdate }) => {
  const [newId, setNewId] = React.useState('');

  const handleAdd = async () => {
    if (!newId.trim()) return;
    try {
      await addParticipant(conversationId, parseInt(newId, 10));
      const list = await fetchParticipants(conversationId);
      onUpdate?.(list);
      setNewId('');
    } catch (err) {
      console.error('Failed to add participant', err);
    }
  };

  const handleRemove = async (id: string) => {
    try {
      await removeParticipant(conversationId, parseInt(id, 10));
      const list = await fetchParticipants(conversationId);
      onUpdate?.(list);
    } catch (err) {
      console.error('Failed to remove participant', err);
    }
  };

  return (
    <div
      className="w-48 border-l border-gray-300 dark:border-gray-700 p-4 space-y-2 bg-white dark:bg-gray-900 shadow-inner"
      style={{ maxHeight: '100%' }}
    >
      {participants.length > 0 && (
        <>
          <h3 className="text-xs text-gray-500 dark:text-gray-400 mb-2">
            Participants
          </h3>
          {participants.map((u) => (
            <div
              key={u.id}
              className="flex items-center text-sm text-gray-700 dark:text-gray-200 truncate"
            >
              {u.online && (
                <span className="h-2 w-2 bg-green-500 rounded-full mr-2" />
              )}
              <span className="flex-1">{u.username || 'Unknown'}</span>
            </div>
          ))}
        </>
      )}
    </div>
  );
};

export default ParticipantList;
