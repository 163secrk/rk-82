import request from '@/utils/request';
import type { Message } from '@/types/models';

export function getConversations(): Promise<Message[]> {
  return request.get('/messages/conversations');
}

export function getMessages(projectId: number, page = 0, size = 50): Promise<{ content: Message[] }> {
  return request.get(`/messages/${projectId}?page=${page}&size=${size}`);
}

export function markAsRead(messageId: number): Promise<void> {
  return request.put(`/messages/${messageId}/read`);
}
