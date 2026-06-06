<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Eye, EyeOff, Loader2, User, Briefcase } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const nickname = ref('');
const role = ref<'CLIENT' | 'FREELANCER'>('FREELANCER');
const showPassword = ref(false);
const loading = ref(false);
const error = ref('');

const handleRegister = async () => {
  if (!email.value || !password.value || !nickname.value) {
    error.value = '请填写所有必填字段';
    return;
  }

  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致';
    return;
  }

  if (password.value.length < 6) {
    error.value = '密码长度至少6位';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    await authStore.register({
      email: email.value,
      password: password.value,
      nickname: nickname.value,
      role: role.value
    });
    router.push('/login?registered=1');
  } catch (err: any) {
    error.value = err.message || '注册失败，请重试';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen flex">
    <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-indigo-500 to-purple-600 flex-col justify-center items-center p-12">
      <div class="text-white text-center max-w-md">
        <h1 class="text-4xl font-bold mb-4">自由职聘</h1>
        <p class="text-xl text-indigo-100 mb-8">
          开启您的自由职业之旅，连接无限可能
        </p>
      </div>
    </div>

    <div class="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white">
      <div class="w-full max-w-md">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-800 mb-2">创建账户</h2>
          <p class="text-gray-500">加入我们，开始新的旅程</p>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-3">我是</label>
          <div class="grid grid-cols-2 gap-3">
            <button
              type="button"
              @click="role = 'CLIENT'"
              :class="[
                'flex flex-col items-center justify-center p-4 rounded-lg border-2 transition-all',
                role === 'CLIENT'
                  ? 'border-indigo-500 bg-indigo-50 text-indigo-600'
                  : 'border-gray-200 hover:border-gray-300'
              ]"
            >
              <Briefcase class="w-8 h-8 mb-2" />
              <span class="font-medium">发包方</span>
              <span class="text-xs opacity-75">发布项目需求</span>
            </button>
            <button
              type="button"
              @click="role = 'FREELANCER'"
              :class="[
                'flex flex-col items-center justify-center p-4 rounded-lg border-2 transition-all',
                role === 'FREELANCER'
                  ? 'border-indigo-500 bg-indigo-50 text-indigo-600'
                  : 'border-gray-200 hover:border-gray-300'
              ]"
            >
              <User class="w-8 h-8 mb-2" />
              <span class="font-medium">接包方</span>
              <span class="text-xs opacity-75">承接项目工作</span>
            </button>
          </div>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">昵称</label>
            <input
              v-model="nickname"
              type="text"
              placeholder="请输入昵称"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition-all"
            />
          </div>

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
                placeholder="请输入密码（至少6位）"
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

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">确认密码</label>
            <input
              v-model="confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition-all"
            />
          </div>

          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 focus:ring-4 focus:ring-indigo-200 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center"
          >
            <Loader2 v-if="loading" class="w-5 h-5 animate-spin mr-2" />
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <p class="text-center mt-6 text-gray-600">
          已有账户？
          <router-link to="/login" class="text-indigo-600 hover:underline font-medium">
            立即登录
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
