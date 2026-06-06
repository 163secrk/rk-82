import { ref, onUnmounted } from 'vue';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import type { Message as ChatMessage } from '@/types/models';

export function useWebSocket() {
  const stompClient = ref<Stomp.Client | null>(null);
  const isConnected = ref(false);
  const messages = ref<ChatMessage[]>([]);

  const connect = (token: string) => {
    if (isConnected.value) return;

    const socket = new SockJS('/ws/chat');
    stompClient.value = Stomp.over(socket);
    stompClient.value.debug = () => {};

    stompClient.value.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        isConnected.value = true;
        console.log('WebSocket connected');
      },
      (error: any) => {
        isConnected.value = false;
        console.error('WebSocket error:', error);
      }
    );
  };

  const subscribeToChat = (projectId: number, callback: (message: ChatMessage) => void) => {
    if (!stompClient.value || !isConnected.value) return;

    stompClient.value.subscribe(`/topic/chat/${projectId}`, (message: Stomp.Message) => {
      const chatMessage: ChatMessage = JSON.parse(message.body);
      messages.value.push(chatMessage);
      callback(chatMessage);
    });
  };

  const subscribeToNotifications = (userId: number, callback: (message: ChatMessage) => void) => {
    if (!stompClient.value || !isConnected.value) return;

    stompClient.value.subscribe(`/queue/notification/${userId}`, (message: Stomp.Message) => {
      const notification: ChatMessage = JSON.parse(message.body);
      callback(notification);
    });
  };

  const sendMessage = (payload: {
    projectId: number;
    senderId: number;
    receiverId: number;
    content: string;
    type: string;
    fileUrl?: string;
  }) => {
    if (!stompClient.value || !isConnected.value) return;
    stompClient.value.send('/app/chat.send', {}, JSON.stringify(payload));
  };

  const disconnect = () => {
    if (stompClient.value && isConnected.value) {
      stompClient.value.disconnect(() => {
        isConnected.value = false;
        console.log('WebSocket disconnected');
      });
    }
  };

  onUnmounted(() => {
    disconnect();
  });

  return {
    isConnected,
    messages,
    connect,
    disconnect,
    subscribeToChat,
    subscribeToNotifications,
    sendMessage
  };
}
