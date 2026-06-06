import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserInfo } from '@/types/models';
import { login as apiLogin, register as apiRegister, getCurrentUser } from '@/api/auth';
import type { LoginRequest, RegisterRequest } from '@/api/auth';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const user = ref<UserInfo | null>(null);

  const isLoggedIn = computed(() => !!token.value);
  const isClient = computed(() => user.value?.role === 'CLIENT');
  const isFreelancer = computed(() => user.value?.role === 'FREELANCER');

  const login = async (data: LoginRequest) => {
    const response = await apiLogin(data);
    token.value = response.token;
    user.value = response.user;
    localStorage.setItem('token', response.token);
    return response;
  };

  const register = async (data: RegisterRequest) => {
    const response = await apiRegister(data);
    return response;
  };

  const fetchCurrentUser = async () => {
    try {
      const response = await getCurrentUser();
      user.value = response;
      return response;
    } catch (error) {
      logout();
      throw error;
    }
  };

  const logout = () => {
    token.value = null;
    user.value = null;
    localStorage.removeItem('token');
  };

  return {
    token,
    user,
    isLoggedIn,
    isClient,
    isFreelancer,
    login,
    register,
    fetchCurrentUser,
    logout
  };
});
