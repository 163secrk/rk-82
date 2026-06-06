<script setup lang="ts">
import { ref, computed } from 'vue';
import { User, Mail, Shield, Star, Edit3, Save, Loader2 } from 'lucide-vue-next';
import { formatMoney, getRoleText } from '@/utils/format';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const editing = ref(false);
const nickname = ref('');
const description = ref('');
const skills = ref('');
const loading = ref(false);

const initForm = () => {
  if (authStore.user) {
    nickname.value = authStore.user.nickname;
    description.value = authStore.user.description || '';
    skills.value = authStore.user.skills || '';
  }
};

const handleEdit = () => {
  initForm();
  editing.value = true;
};

const handleSave = async () => {
  if (!nickname.value.trim()) {
    return;
  }

  loading.value = true;
  try {
    if (authStore.user) {
      authStore.user.nickname = nickname.value.trim();
      authStore.user.description = description.value.trim();
      authStore.user.skills = skills.value.trim();
    }
    editing.value = false;
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  editing.value = false;
};

const skillList = computed(() => {
  if (!skills.value) return [];
  return skills.value.split(',').map(s => s.trim()).filter(s => s);
});

initForm();
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="bg-gradient-to-r from-indigo-500 to-purple-600 h-32"></div>
      <div class="px-8 pb-8">
        <div class="flex items-end justify-between -mt-16">
          <div class="flex items-end space-x-6">
            <div class="w-32 h-32 rounded-full border-4 border-white bg-indigo-100 flex items-center justify-center shadow-lg">
              <span class="text-5xl text-indigo-600 font-bold">
                {{ authStore.user?.nickname?.charAt(0) }}
              </span>
            </div>
            <div class="mb-4">
              <h2 class="text-2xl font-bold text-gray-800">
                {{ authStore.user?.nickname }}
              </h2>
              <div class="flex items-center space-x-4 mt-2">
                <span class="px-3 py-1 bg-indigo-100 text-indigo-600 rounded-full text-sm font-medium">
                  {{ getRoleText(authStore.user?.role || '') }}
                </span>
                <div class="flex items-center text-yellow-500">
                  <Star class="w-4 h-4 fill-current" />
                  <span class="ml-1 text-gray-600 text-sm">
                    {{ authStore.user?.rating?.toFixed(1) || '0.0' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="mb-4">
            <button
              v-if="!editing"
              @click="handleEdit"
              class="px-4 py-2 bg-indigo-50 text-indigo-600 rounded-lg hover:bg-indigo-100 transition-colors flex items-center"
            >
              <Edit3 class="w-4 h-4 mr-2" />
              编辑资料
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-6">
        <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">基本信息</h3>

          <div v-if="editing" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">昵称</label>
              <input
                v-model="nickname"
                type="text"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">个人简介</label>
              <textarea
                v-model="description"
                rows="4"
                placeholder="介绍一下您自己..."
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
              ></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">技能标签（用逗号分隔）</label>
              <input
                v-model="skills"
                type="text"
                placeholder="例如：Vue, React, TypeScript"
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
              />
            </div>
            <div class="flex space-x-4 pt-4">
              <button
                type="button"
                @click="handleCancel"
                class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                取消
              </button>
              <button
                @click="handleSave"
                :disabled="loading"
                class="flex-1 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors flex items-center justify-center"
              >
                <Loader2 v-if="loading" class="w-5 h-5 animate-spin mr-2" />
                {{ loading ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>

          <div v-else class="space-y-4">
            <div class="flex items-start space-x-4">
              <Mail class="w-5 h-5 text-gray-400 mt-0.5" />
              <div>
                <p class="text-sm text-gray-500">邮箱</p>
                <p class="text-gray-800">{{ authStore.user?.email }}</p>
              </div>
            </div>
            <div class="flex items-start space-x-4">
              <Shield class="w-5 h-5 text-gray-400 mt-0.5" />
              <div>
                <p class="text-sm text-gray-500">角色</p>
                <p class="text-gray-800">{{ getRoleText(authStore.user?.role || '') }}</p>
              </div>
            </div>
            <div v-if="authStore.user?.description" class="flex items-start space-x-4">
              <User class="w-5 h-5 text-gray-400 mt-0.5" />
              <div>
                <p class="text-sm text-gray-500">个人简介</p>
                <p class="text-gray-800">{{ authStore.user.description }}</p>
              </div>
            </div>
            <div v-if="skillList.length > 0" class="flex items-start space-x-4">
              <Star class="w-5 h-5 text-gray-400 mt-0.5" />
              <div>
                <p class="text-sm text-gray-500">技能标签</p>
                <div class="flex flex-wrap gap-2 mt-1">
                  <span
                    v-for="skill in skillList"
                    :key="skill"
                    class="px-3 py-1 bg-indigo-100 text-indigo-600 rounded-full text-sm"
                  >
                    {{ skill }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-6">
        <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">财务概览</h3>
          <div class="space-y-4">
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-500">账户余额</p>
              <p class="text-2xl font-bold text-indigo-600">
                {{ formatMoney(authStore.user?.balance || 0) }}
              </p>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="p-4 bg-green-50 rounded-lg">
                <p class="text-sm text-gray-500">累计收入</p>
                <p class="text-lg font-semibold text-green-600">
                  {{ formatMoney(0) }}
                </p>
              </div>
              <div class="p-4 bg-red-50 rounded-lg">
                <p class="text-sm text-gray-500">累计支出</p>
                <p class="text-lg font-semibold text-red-600">
                  {{ formatMoney(0) }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">信用评分</h3>
          <div class="text-center">
            <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-yellow-100 mb-4">
              <div class="text-center">
                <p class="text-3xl font-bold text-yellow-600">
                  {{ authStore.user?.rating?.toFixed(1) || '0.0' }}
                </p>
              </div>
            </div>
            <div class="flex justify-center mb-2">
              <Star
                v-for="i in 5"
                :key="i"
                :class="[
                  'w-6 h-6',
                  i <= Math.floor(authStore.user?.rating || 0)
                    ? 'text-yellow-400 fill-current'
                    : 'text-gray-200'
                ]"
              />
            </div>
            <p class="text-sm text-gray-500">基于项目评价计算</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
