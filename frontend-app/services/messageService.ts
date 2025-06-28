import { MessageEvent } from '../entity/Message';
import { ApiResponse } from '../dto/response/ApiResponseDTO';
import { get } from './httpClient';
import { API_BASE_URL } from '../config';

export async function fetchMessages(
  conversationId: string,
  page = 0,
  size = 20
): Promise<ApiResponse<MessageEvent[]>> {
  return get<MessageEvent[]>(
    `${API_BASE_URL}/conversations/${conversationId}/messages?page=${page}&size=${size}`
  );
}

export async function fetchUnreadMessages(
  conversationId: string
): Promise<ApiResponse<MessageEvent[]>> {
  return get<MessageEvent[]>(
    `${API_BASE_URL}/conversations/${conversationId}/messages/unread`
  );
}
