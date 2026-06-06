<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Filter, Calendar, User, Gavel, Loader2 } from 'lucide-vue-next';
import { getProjects } from '@/api/project';
import { formatMoney, formatDate, getStatusText, getStatusColor } from '@/utils/format';
import type { Project } from '@/types/models';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const projects = ref<Project[]>([]);
const loading = ref(true);
const total = ref(0);
const page = ref(0);
const size = ref(10);

const keyword = ref('');
const selectedCategory = ref('');
const selectedStatus = ref('PUBLISHED');

const categories = ['全部', '网站开发', '移动应用', 'UI设计', '后端开发', '数据分析', '营销推广', '其他'];
const statuses = [
  { value: 'PUBLISHED', label: '招标中' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' }
];

const filteredCategories = computed(() => {
  return selectedCategory.value === '全部' ? '' : selectedCategory.value;
});

const fetchProjects = async () => {
  loading.value = true;
  try {
    const response = await getProjects({
      page: page.value,
      size: size.value,
      status: selectedStatus.value,
      category: filteredCategories.value,
      keyword: keyword.value
    });
    projects.value = response.content;
    total.value = response.totalElements;
  } catch (error) {
    console.error('Failed to fetch projects:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  page.value = 0;
  fetchProjects();
};

const handleCategoryChange = (category: string) => {
  selectedCategory.value = category;
  page.value = 0;
  fetchProjects();
};

const handleStatusChange = (status: string) => {
  selectedStatus.value = status;
  page.value = 0;
  fetchProjects();
};

const totalPages = computed(() => Math.ceil(total.value / size.value));

onMounted(() => {
  fetchProjects();
});
</script>

<template>
  <div class="space-y-6">
    <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <div class="flex flex-col lg:flex-row gap-4">
        <div class="flex-1 relative">
          <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="keyword"
            @keyup.enter="handleSearch"
            type="text"
            placeholder="搜索项目名称或描述..."
            class="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
          />
        </div>
        <button
          @click="handleSearch"
          class="px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors"
        >
          搜索
        </button>
      </div>

      <div class="mt-6 space-y-4">
        <div class="flex items-center space-x-2">
          <Filter class="w-4 h-4 text-gray-500" />
          <span class="text-sm text-gray-600">分类：</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="cat in categories"
              :key="cat"
              @click="handleCategoryChange(cat)"
              :class="[
                'px-3 py-1.5 rounded-full text-sm transition-colors',
                selectedCategory === cat
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
            >
              {{ cat }}
            </button>
          </div>
        </div>

        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-600">状态：</span>
          <div class="flex gap-2">
            <button
              v-for="status in statuses"
              :key="status.value"
              @click="handleStatusChange(status.value)"
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
      </div>
    </div>

    <div class="space-y-4">
      <div class="flex items-center justify-between">
        <p class="text-gray-600">
          共找到 <span class="font-semibold text-indigo-600">{{ total }}</span> 个项目
        </p>
      </div>

      <div v-if="loading" class="flex items-center justify-center py-12">
        <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
      </div>

      <div v-else-if="projects.length === 0" class="bg-white rounded-xl p-12 text-center shadow-sm border border-gray-100">
        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <Search class="w-8 h-8 text-gray-400" />
        </div>
        <h4 class="text-lg font-medium text-gray-800 mb-2">暂无项目</h4>
        <p class="text-gray-500">没有找到符合条件的项目，请尝试调整筛选条件</p>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="project in projects"
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
                  <span>{{ project.clientName }}</span>
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
                预算上限 {{ formatMoney(project.budgetMax) }}
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

      <div
        v-if="totalPages > 1"
        class="flex items-center justify-center space-x-2 mt-8"
      >
        <button
          @click="page > 0 && (page--, fetchProjects())"
          :disabled="page === 0"
          class="px-4 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          上一页
        </button>
        <span class="text-gray-600">
          第 {{ page + 1 }} 页 / 共 {{ totalPages }} 页
        </span>
        <button
          @click="page < totalPages - 1 && (page++, fetchProjects())"
          :disabled="page >= totalPages - 1"
          class="px-4 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>
