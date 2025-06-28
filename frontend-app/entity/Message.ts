export type Recipient = 'USERS' | 'AI';

export type Message = {
  id: string;
  sender: string;
  text: string;
  recipient?: Recipient;
};

export type MessageEvent = {
  messageId: string;
  conversationId: number;
  senderId: number;
  text: string;
  type: string;
  replyToMessageId?: string;
  recipient: Recipient;
  occurredAt: string;
};
