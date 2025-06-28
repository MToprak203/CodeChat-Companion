import { get, post, del, put } from './httpClient';
import { Conversation } from '../entity/Conversation';
import { PaginatedMeta } from '../dto/meta/PaginatedMeta';
import { API_BASE_URL } from '../config';

export type ConversationPageResponse = {
  items: Conversation[];
  meta: PaginatedMeta;
};


export async function fetchConversations(page: number, size: number): Promise<ConversationPageResponse> {
  const url = `${API_BASE_URL}/conversations?page=${page}&size=${size}`;
  const response = await get<Conversation[]>(url);

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Failed to fetch conversations');
  }

  return {
    items: response.data,
    meta: response.meta as PaginatedMeta,
  };
}

export async function createConversation(): Promise<Conversation> {
  const url = `${API_BASE_URL}/conversations`;
  const response = await post<void, Conversation>(url);

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Failed to create conversation');
  }

  return response.data;
}

export async function updateConversation(
  conversationId: string,
  title: string
): Promise<Conversation> {
  const url = `${API_BASE_URL}/conversations/${conversationId}`;
  const response = await put<{ title: string }, Conversation>(url, { title });

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Failed to update conversation');
  }

  return response.data;
}


import { User } from '../entity/User';

export async function fetchParticipants(conversationId: string): Promise<User[]> {
  const url = `${API_BASE_URL}/conversations/${conversationId}/participants`;
  const response = await get<User[]>(url);

  if (!response.success || !response.data) {
    throw new Error(response.error?.message || 'Failed to fetch participants');
  }

  return response.data;
}

export async function addParticipant(
  conversationId: string,
  userId: number
): Promise<void> {
  const url = `${API_BASE_URL}/conversations/${conversationId}/participants?userId=${userId}`;
  const res = await post<void, void>(url);
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to add participant');
  }
}

export async function removeParticipant(
  conversationId: string,
  userId: number
): Promise<void> {
  const url = `${API_BASE_URL}/conversations/${conversationId}/participants/${userId}`;
  const res = await del<void>(url);
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to remove participant');
  }
}

export async function leaveConversation(conversationId: string): Promise<void> {
  const url = `${API_BASE_URL}/conversations/${conversationId}`;
  const res = await del<void>(url);
  if (!res.success) {
    throw new Error(res.error?.message || 'Failed to leave conversation');
  }
}

