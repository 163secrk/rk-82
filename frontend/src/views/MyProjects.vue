<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, User, Gavel, Loader2 } from 'lucide-vue-next';
import { getMyProjects } from '@/api/project';
import { formatMoney, formatDate, getStatusText, getStatusColor } from '@/utils/format';
import type { Project } from '@/types/models';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const projects = ref<Project[]>([]);
const loading = ref(true);
const selectedStatus = ref('ALL');

const statuses = [
  { value: 'ALL', label: '全部' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '招标中' },
  { value: 'BIDDING', label: '已选标' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'DELIVERED', label: '已交付' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' }
];

const filteredProjects = computed(() => {
  if (selectedStatus.value === 'ALL') {
    return projects.value;
  }
  return projects.value.filter(p => p.status === selectedStatus.value);
});

const fetchProjects = async () => {
  loading.value = true;
  try {
    const data = await getMyProjects();
    projects.value = data;
  } catch (error) {
    console.error('Failed to fetch my projects:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchProjects();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div class="flex items-center space-x-2">
        <span class="text-sm text-gray-600">筛选：</span>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="status in statuses"
            :key="status.value"
            @click="selectedStatus = status.value"
            :class="[
              'px-3 py-1.5 rounded-full text-sm transition-colors',
              selectedStatus === status.value
                ? 'bg-indigo-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ status.label }}
          </button>
        </div>
      </div>

      <button
        v-if="authStore.isClient"
        @click="router.push('/projects/create')"
        class="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium"
      >
        + 发布新项目
      </button>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-12">
      <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
    </div>

    <div v-else-if="filteredProjects.length === 0" class="bg-white rounded-xl p-12 text-center shadow-sm border border-gray-100">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <Gavel class="w-8 h-8 text-gray-400" />
      </div>
      <h4 class="text-lg font-medium text-gray-800 mb-2">暂无项目</h4>
      <p class="text-gray-500">
        {{ authStore.isClient ? '您还没有发布任何项目' : '您还没有参与任何项目' }}
      </p>
      <button
        v-if="authStore.isClient"
        @click="router.push('/projects/create')"
        class="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
      >
        发布第一个项目
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="project in filteredProjects"
        :key="project.id"
        class="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow cursor-pointer"
        @click="router.push(`/projects/${project.id}`)"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3 mb-3">
              <h3 class="text-lg font-semibold text-gray-800 hover:text-indigo-600">
                {{ project.title }}
              </h3>
              <span
                :class="[
                  'px-2.5 py-1 rounded-full text-xs font-medium',
                  getStatusColor(project.status)
                ]"
              >
                {{ getStatusText(project.status) }}
              </span>
              <span class="px-2.5 py-1 bg-gray-100 text-gray-600 rounded-full text-xs">
                {{ project.category }}
              </span>
            </div>

            <p class="text-gray-600 line-clamp-2 mb-4">{{ project.description }}</p>

            <div class="flex flex-wrap items-center gap-4 text-sm text-gray-500">
              <div class="flex items-center space-x-1">
                <User class="w-4 h-4" />
                <span>
                  {{
                    authStore.isClient
                      ? `接包方：${project.freelancerName || '暂无'}`
                      : `发包方：${project.clientName}`
                  }}
                </span>
              </div>
              <div class="flex items-center space-x-1">
                <Calendar class="w-4 h-4" />
                <span>截止：{{ formatDate(project.deadline) }}</span>
              </div>
              <div class="flex items-center space-x-1">
                <Gavel class="w-4 h-4" />
                <span>{{ project.bidCount }} 人竞标</span>
              </div>
            </div>
          </div>

          <div class="text-right ml-8">
            <p class="text-2xl font-bold text-indigo-600">
              {{ formatMoney(project.budgetMin) }}
              <span class="text-sm font-normal text-gray-400">起</span>
            </p>
            <p class="text-sm text-gray-500">
              上限 {{ formatMoney(project.budgetMax) }}
            </p>
            <p v-if="project.agreedPrice" class="text-sm font-medium text-green-600 mt-1">
              成交：{{ formatMoney(project.agreedPrice) }}
            </p>
            <button
              @click.stop="router.push(`/projects/${project.id}`)"
              class="mt-3 px-4 py-2 bg-indigo-50 text-indigo-600 rounded-lg hover:bg-indigo-100 transition-colors text-sm font-medium"
            >
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
