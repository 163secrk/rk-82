<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Send, MessageSquare, User, ArrowLeft } from 'lucide-vue-next';
import { getConversations, getMessages } from '@/api/message';
import { formatDate } from '@/utils/format';
import { useWebSocket } from '@/composables/useWebSocket';
import { useAuthStore } from '@/stores/auth';
import type { Message } from '@/types/models';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const { connect, subscribeToChat, sendMessage } = useWebSocket();

const conversations = ref<Message[]>([]);
const messages = ref<Message[]>([]);
const selectedProjectId = ref<number | null>(null);
const newMessage = ref('');
const loading = ref(true);
const messagesContainer = ref<HTMLElement | null>(null);

const currentProjectId = computed(() => {
  const id = route.params.projectId;
  return id ? Number(id) : null;
});

const selectedConversation = computed(() => {
  return conversations.value.find(c => c.projectId === selectedProjectId.value);
});

const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const fetchConversations = async () => {
  try {
    const data = await getConversations();
    conversations.value = data;
  } catch (error) {
    console.error('Failed to fetch conversations:', error);
  } finally {
    loading.value = false;
  }
};

const fetchMessages = async (projectId: number) => {
  try {
    const data = await getMessages(projectId);
    messages.value = data.content;
    scrollToBottom();
  } catch (error) {
    console.error('Failed to fetch messages:', error);
  }
};

const handleSelectConversation = (projectId: number) => {
  selectedProjectId.value = projectId;
  router.push(`/chat/${projectId}`);
  fetchMessages(projectId);

  if (authStore.token) {
    connect(authStore.token);
    setTimeout(() => {
      subscribeToChat(projectId, (message) => {
        messages.value.push(message);
        scrollToBottom();
      });
    }, 1000);
  }
};

const handleSendMessage = () => {
  if (!newMessage.value.trim() || !selectedProjectId.value) return;

  const receiverId = selectedConversation.value?.senderId === authStore.user?.id
    ? selectedConversation.value.receiverId
    : selectedConversation.value?.senderId;

  if (!receiverId) return;

  sendMessage({
    projectId: selectedProjectId.value,
    senderId: authStore.user!.id,
    receiverId,
    content: newMessage.value.trim(),
    type: 'TEXT'
  });

  messages.value.push({
    id: Date.now(),
    projectId: selectedProjectId.value,
    senderId: authStore.user!.id,
    senderName: authStore.user!.nickname,
    senderAvatar: authStore.user!.avatar,
    receiverId,
    content: newMessage.value.trim(),
    type: 'TEXT',
    isRead: false,
    createdAt: new Date().toISOString()
  });

  newMessage.value = '';
  scrollToBottom();
};

const isMyMessage = (message: Message) => {
  return message.senderId === authStore.user?.id;
};

watch(currentProjectId, (newId) => {
  if (newId) {
    handleSelectConversation(newId);
  }
});

onMounted(async () => {
  await fetchConversations();
  if (currentProjectId.value) {
    handleSelectConversation(currentProjectId.value);
  } else if (conversations.value.length > 0) {
    handleSelectConversation(conversations.value[0].projectId);
  }
});
</script>

<template>
  <div class="h-[calc(100vh-160px)] flex bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
    <div class="w-80 border-r border-gray-100 flex flex-col">
      <div class="p-4 border-b border-gray-100">
        <h3 class="font-semibold text-gray-800">消息列表</h3>
      </div>

      <div class="flex-1 overflow-auto">
        <div v-if="loading" class="flex items-center justify-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-4 border-indigo-200 border-t-indigo-600"></div>
        </div>

        <div v-else-if="conversations.length === 0" class="p-8 text-center">
          <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-500">暂无消息</p>
          <p class="text-sm text-gray-400 mt-1">参与项目后即可开始沟通</p>
        </div>

        <div v-else>
          <div
            v-for="conv in conversations"
            :key="conv.projectId"
            @click="handleSelectConversation(conv.projectId)"
            :class="[
              'p-4 border-b border-gray-50 cursor-pointer transition-colors',
              selectedProjectId === conv.projectId ? 'bg-indigo-50' : 'hover:bg-gray-50'
            ]"
          >
            <div class="flex items-start space-x-3">
              <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
                <span class="text-indigo-600 font-semibold">
                  {{ (conv.senderId === authStore.user?.id ? conv.receiverId : conv.senderId)?.toString().charAt(0) }}
                </span>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between mb-1">
                  <p class="font-medium text-gray-800 truncate">
                    项目 #{{ conv.projectId }}
                  </p>
                  <span class="text-xs text-gray-400">
                    {{ formatDate(conv.createdAt) }}
                  </span>
                </div>
                <p class="text-sm text-gray-500 truncate">{{ conv.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex-1 flex flex-col">
      <div v-if="selectedProjectId" class="flex-1 flex flex-col">
        <div class="p-4 border-b border-gray-100 flex items-center space-x-4">
          <button
            @click="router.push('/my-projects')"
            class="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
          >
            <ArrowLeft class="w-5 h-5 text-gray-600" />
          </button>
          <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center">
            <User class="w-5 h-5 text-indigo-600" />
          </div>
          <div>
            <p class="font-medium text-gray-800">项目 #{{ selectedProjectId }}</p>
            <p class="text-sm text-gray-500">在线沟通</p>
          </div>
        </div>

        <div
          ref="messagesContainer"
          class="flex-1 overflow-auto p-6 bg-gray-50 space-y-4"
        >
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['flex', isMyMessage(msg) ? 'justify-end' : 'justify-start']"
          >
            <div
              :class="[
                'max-w-xs lg:max-w-md px-4 py-3 rounded-2xl',
                isMyMessage(msg)
                  ? 'bg-indigo-600 text-white rounded-br-md'
                  : 'bg-white text-gray-800 rounded-bl-md shadow-sm'
              ]"
            >
              <p class="break-words">{{ msg.content }}</p>
              <p
                :class="[
                  'text-xs mt-1',
                  isMyMessage(msg) ? 'text-indigo-200' : 'text-gray-400'
                ]"
              >
                {{ formatDate(msg.createdAt) }}
              </p>
            </div>
          </div>
        </div>

        <div class="p-4 border-t border-gray-100">
          <div class="flex items-center space-x-4">
            <input
              v-model="newMessage"
              @keyup.enter="handleSendMessage"
              type="text"
              placeholder="输入消息..."
              class="flex-1 px-4 py-3 border border-gray-300 rounded-full focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
            <button
              @click="handleSendMessage"
              :disabled="!newMessage.trim()"
              class="w-12 h-12 bg-indigo-600 text-white rounded-full flex items-center justify-center hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <Send class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <div v-else class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <MessageSquare class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-800 mb-2">选择一个会话</h3>
          <p class="text-gray-500">从左侧列表选择一个项目开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>
