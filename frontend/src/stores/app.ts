import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false);
  const notifications = ref<any[]>([]);

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  };

  const addNotification = (notification: any) => {
    notifications.value.unshift(notification);
  };

  const clearNotifications = () => {
    notifications.value = [];
  };

  return {
    sidebarCollapsed,
    notifications,
    toggleSidebar,
    addNotification,
    clearNotifications
  };
});
