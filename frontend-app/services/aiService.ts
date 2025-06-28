import { post } from './httpClient';
import { API_BASE_URL } from '../config';

export async function stopConversationAi(conversationId: string): Promise<void> {
  const res = await post<void, void>(
    `${API_BASE_URL}/conversations/${conversationId}/stop`
  );
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to stop AI');
  }
}
