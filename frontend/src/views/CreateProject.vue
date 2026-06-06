<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Loader2 } from 'lucide-vue-next';
import { createProject } from '@/api/project';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const title = ref('');
const description = ref('');
const category = ref('网站开发');
const budgetMin = ref('');
const budgetMax = ref('');
const deadline = ref('');
const requirements = ref('');

const loading = ref(false);
const error = ref('');

const categories = ['网站开发', '移动应用', 'UI设计', '后端开发', '数据分析', '营销推广', '其他'];

const handleSubmit = async () => {
  if (!title.value || !description.value || !budgetMin.value || !budgetMax.value || !deadline.value) {
    error.value = '请填写所有必填字段';
    return;
  }

  const min = Number(budgetMin.value);
  const max = Number(budgetMax.value);

  if (min <= 0 || max <= 0) {
    error.value = '预算金额必须大于0';
    return;
  }

  if (min > max) {
    error.value = '最低预算不能大于最高预算';
    return;
  }

  if (new Date(deadline.value) < new Date()) {
    error.value = '截止日期不能早于今天';
    return;
  }

  loading.value = true;
  error.value = '';

  try {
    const project = await createProject({
      title: title.value,
      description: description.value,
      category: category.value,
      budgetMin: min,
      budgetMax: max,
      deadline: deadline.value,
      requirements: requirements.value
    });
    router.push(`/projects/${project.id}`);
  } catch (err: any) {
    error.value = err.message || '发布失败，请重试';
  } finally {
    loading.value = false;
  }
};

const today = new Date().toISOString().split('T')[0];
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <button
      @click="router.back()"
      class="flex items-center text-gray-600 hover:text-indigo-600 mb-6 transition-colors"
    >
      <ArrowLeft class="w-5 h-5 mr-2" />
      返回
    </button>

    <div class="bg-white rounded-xl p-8 shadow-sm border border-gray-100">
      <h2 class="text-2xl font-bold text-gray-800 mb-2">发布新项目</h2>
      <p class="text-gray-500 mb-8">填写项目详情，找到合适的专业人才</p>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            项目名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="title"
            type="text"
            placeholder="请输入项目名称"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            项目分类 <span class="text-red-500">*</span>
          </label>
          <select
            v-model="category"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none bg-white"
          >
            <option v-for="cat in categories" :key="cat" :value="cat">
              {{ cat }}
            </option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            项目描述 <span class="text-red-500">*</span>
          </label>
          <textarea
            v-model="description"
            rows="5"
            placeholder="详细描述您的项目需求、目标和期望..."
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
          ></textarea>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            技术要求
          </label>
          <textarea
            v-model="requirements"
            rows="3"
            placeholder="请描述所需的技术栈、技能要求等..."
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
          ></textarea>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              最低预算（元）<span class="text-red-500">*</span>
            </label>
            <input
              v-model="budgetMin"
              type="number"
              min="0"
              placeholder="请输入最低预算"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              最高预算（元）<span class="text-red-500">*</span>
            </label>
            <input
              v-model="budgetMax"
              type="number"
              min="0"
              placeholder="请输入最高预算"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            截止日期 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="deadline"
            type="date"
            :min="today"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
          />
        </div>

        <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

        <div class="flex space-x-4 pt-4">
          <button
            type="button"
            @click="router.back()"
            class="flex-1 py-3 border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors"
          >
            取消
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="flex-1 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center"
          >
            <Loader2 v-if="loading" class="w-5 h-5 animate-spin mr-2" />
            {{ loading ? '发布中...' : '发布项目' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
