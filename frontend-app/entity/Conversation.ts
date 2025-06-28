export type ConversationType = 'PRIVATE' | 'GROUP';

export type Conversation = {
  id: string;
  title: string;
  type: ConversationType;
  projectId?: number;
};
