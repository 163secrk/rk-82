<script setup lang="ts">
import { computed } from 'vue';
import { RouterView, RouterLink, useRoute } from 'vue-router';
import {
  Home,
  Briefcase,
  PlusCircle,
  FolderKanban,
  Gavel,
  MessageSquare,
  Wallet,
  User,
  LogOut,
  Menu,
  X,
  Bell
} from 'lucide-vue-next';
import { useAuthStore } from '@/stores/auth';
import { useAppStore } from '@/stores/app';
import { getRoleText } from '@/utils/format';

const authStore = useAuthStore();
const appStore = useAppStore();
const route = useRoute();

const menuItems = computed(() => {
  const items = [
    { icon: Home, label: '控制台', to: '/dashboard', name: 'Dashboard' },
    { icon: Briefcase, label: '项目市场', to: '/projects', name: 'ProjectList' }
  ];

  if (authStore.isClient) {
    items.push(
      { icon: PlusCircle, label: '发布项目', to: '/projects/create', name: 'CreateProject' },
      { icon: FolderKanban, label: '我的项目', to: '/my-projects', name: 'MyProjects' }
    );
  }

  if (authStore.isFreelancer) {
    items.push(
      { icon: Gavel, label: '我的竞标', to: '/my-bids', name: 'MyBids' },
      { icon: FolderKanban, label: '我的项目', to: '/my-projects', name: 'MyProjects' }
    );
  }

  items.push(
    { icon: MessageSquare, label: '消息中心', to: '/chat', name: 'Chat' },
    { icon: Wallet, label: '我的钱包', to: '/wallet', name: 'Wallet' },
    { icon: User, label: '个人中心', to: '/profile', name: 'Profile' }
  );

  return items;
});

const isActive = (name: string) => {
  return route.name === name;
};

const handleLogout = () => {
  authStore.logout();
  window.location.href = '/login';
};
</script>

<template>
  <div class="flex h-screen bg-gray-50">
    <aside
      :class="[
        'bg-white border-r border-gray-200 flex flex-col transition-all duration-300',
        appStore.sidebarCollapsed ? 'w-16' : 'w-64'
      ]"
    >
      <div class="h-16 flex items-center justify-between px-4 border-b border-gray-200">
        <h1 v-if="!appStore.sidebarCollapsed" class="text-xl font-bold text-indigo-600">
          自由职聘
        </h1>
        <button
          @click="appStore.toggleSidebar"
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <Menu v-if="appStore.sidebarCollapsed" class="w-5 h-5" />
          <X v-else class="w-5 h-5" />
        </button>
      </div>

      <nav class="flex-1 py-4 px-2 space-y-1">
        <RouterLink
          v-for="item in menuItems"
          :key="item.name"
          :to="item.to"
          :class="[
            'flex items-center px-3 py-2.5 rounded-lg transition-colors',
            isActive(item.name)
              ? 'bg-indigo-50 text-indigo-600'
              : 'text-gray-700 hover:bg-gray-100'
          ]"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" />
          <span v-if="!appStore.sidebarCollapsed" class="ml-3">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="border-t border-gray-200 p-2">
        <button
          @click="handleLogout"
          class="flex items-center w-full px-3 py-2.5 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
        >
          <LogOut class="w-5 h-5 flex-shrink-0" />
          <span v-if="!appStore.sidebarCollapsed" class="ml-3">退出登录</span>
        </button>
      </div>
    </aside>

    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
        <div class="flex items-center space-x-4">
          <h2 class="text-lg font-semibold text-gray-800">
            {{ menuItems.find(i => isActive(i.name))?.label || '控制台' }}
          </h2>
        </div>

        <div class="flex items-center space-x-4">
          <button class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors">
            <Bell class="w-5 h-5 text-gray-600" />
            <span
              v-if="appStore.notifications.length > 0"
              class="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center"
            >
              {{ appStore.notifications.length }}
            </span>
          </button>

          <div class="flex items-center space-x-3">
            <div class="text-right">
              <p class="text-sm font-medium text-gray-800">
                {{ authStore.user?.nickname }}
              </p>
              <p class="text-xs text-gray-500">
                {{ getRoleText(authStore.user?.role || '') }}
              </p>
            </div>
            <div
              class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center"
            >
              <span class="text-indigo-600 font-semibold">
                {{ authStore.user?.nickname?.charAt(0) }}
              </span>
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-6">
        <RouterView />
      </main>
    </div>
  </div>
</template>
