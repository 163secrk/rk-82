<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Eye, EyeOff, Loader2 } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const email = ref('');
const password = ref('');
const showPassword = ref(false);
const loading = ref(false);
const error = ref('');

const handleLogin = async () => {
  if (!email.value || !password.value) {
    error.value = '请输入邮箱和密码';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    await authStore.login({ email: email.value, password: password.value });
    const redirect = (route.query.redirect as string) || '/dashboard';
    router.push(redirect);
  } catch (err: any) {
    error.value = err.message || '登录失败，请检查邮箱和密码';
  } finally {
    loading.value = false;
  }
};

const fillTestAccount = (type: 'client' | 'freelancer') => {
  if (type === 'client') {
    email.value = 'client@test.com';
  } else {
    email.value = 'freelancer@test.com';
  }
  password.value = '123456';
};
</script>

<template>
  <div class="min-h-screen flex">
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-indigo-500 to-purple-600 flex-col justify-center items-center p-12">
      <div class="text-white text-center max-w-md">
        <h1 class="text-4xl font-bold mb-4">自由职聘</h1>
        <p class="text-xl text-indigo-100 mb-8">
          连接优秀企业与专业人才，让项目更高效，让工作更自由
        </p>
        <div class="space-y-4 text-indigo-100">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
              ✓
            </div>
            <span>安全的资金托管保障</span>
          </div>
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
              ✓
            </div>
            <span>实时在线沟通协作</span>
          </div>
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
              ✓
            </div>
            <span>透明的竞标与评价体系</span>
          </div>
        </div>
      </div>
    </div>

    <div class="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white">
      <div class="w-full max-w-md">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-800 mb-2">欢迎回来</h2>
          <p class="text-gray-500">登录您的账户以继续</p>
        </div>

        <div class="mb-6 p-4 bg-gray-50 rounded-lg">
          <p class="text-sm text-gray-600 mb-3">快速登录测试账户：</p>
          <div class="flex space-x-2">
            <button
              @click="fillTestAccount('client')"
              class="flex-1 px-3 py-2 text-sm bg-indigo-100 text-indigo-600 rounded-lg hover:bg-indigo-200 transition-colors"
            >
              发包方
            </button>
            <button
              @click="fillTestAccount('freelancer')"
              class="flex-1 px-3 py-2 text-sm bg-green-100 text-green-600 rounded-lg hover:bg-green-200 transition-colors"
            >
              接包方
            </button>
          </div>
          <p class="text-xs text-gray-500 mt-2">密码均为：123456</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">邮箱地址</label>
            <input
              v-model="email"
              type="email"
              placeholder="请输入邮箱"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition-all"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">密码</label>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition-all pr-12"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <EyeOff v-if="showPassword" class="w-5 h-5" />
                <Eye v-else class="w-5 h-5" />
              </button>
            </div>
          </div>

          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 focus:ring-4 focus:ring-indigo-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center"
          >
            <Loader2 v-if="loading" class="w-5 h-5 animate-spin mr-2" />
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <p class="text-center mt-6 text-gray-600">
          还没有账户？
          <router-link to="/register" class="text-indigo-600 hover:underline font-medium">
            立即注册
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
