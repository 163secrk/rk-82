<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Clock, Loader2 } from 'lucide-vue-next';
import { getMyBids } from '@/api/bid';
import { formatMoney, formatDate, getStatusText, getStatusColor } from '@/utils/format';
import type { Bid } from '@/types/models';

const router = useRouter();

const bids = ref<Bid[]>([]);
const loading = ref(true);
const selectedStatus = ref('ALL');

const statuses = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待处理' },
  { value: 'ACCEPTED', label: '已接受' },
  { value: 'REJECTED', label: '已拒绝' }
];

const filteredBids = computed(() => {
  if (selectedStatus.value === 'ALL') {
    return bids.value;
  }
  return bids.value.filter(b => b.status === selectedStatus.value);
});

const fetchBids = async () => {
  loading.value = true;
  try {
    const data = await getMyBids();
    bids.value = data;
  } catch (error) {
    console.error('Failed to fetch my bids:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchBids();
});
</script>

<template>
  <div class="space-y-6">
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

    <div v-if="loading" class="flex items-center justify-center py-12">
      <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
    </div>

    <div v-else-if="filteredBids.length === 0" class="bg-white rounded-xl p-12 text-center shadow-sm border border-gray-100">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <Clock class="w-8 h-8 text-gray-400" />
      </div>
      <h4 class="text-lg font-medium text-gray-800 mb-2">暂无竞标记录</h4>
      <p class="text-gray-500">您还没有参与任何项目竞标</p>
      <button
        @click="router.push('/projects')"
        class="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
      >
        浏览项目市场
      </button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="bid in filteredBids"
        :key="bid.id"
        class="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3 mb-3">
              <span
                :class="[
                  'px-2.5 py-1 rounded-full text-xs font-medium',
                  getStatusColor(bid.status)
                ]"
              >
                {{ getStatusText(bid.status) }}
              </span>
              <span class="text-sm text-gray-500">{{ formatDate(bid.createdAt) }}</span>
            </div>

            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center">
                <span class="text-indigo-600 font-semibold">
                  {{ bid.freelancerName?.charAt(0) }}
                </span>
              </div>
              <div>
                <p class="text-sm text-gray-500">项目ID</p>
                <p class="font-medium text-gray-800">#{{ bid.projectId }}</p>
              </div>
            </div>

            <div class="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4">
              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">报价</p>
                <p class="text-xl font-bold text-indigo-600">{{ formatMoney(bid.price) }}</p>
              </div>
              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">交付周期</p>
                <p class="text-xl font-bold text-gray-800">{{ bid.deliveryDays }} 天</p>
              </div>
              <div class="p-4 bg-gray-50 rounded-lg">
                <p class="text-sm text-gray-500">竞标时间</p>
                <p class="text-lg font-medium text-gray-800">{{ formatDate(bid.createdAt) }}</p>
              </div>
            </div>

            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-500 mb-2">竞标方案</p>
              <p class="text-gray-600 whitespace-pre-wrap">{{ bid.proposal }}</p>
            </div>
          </div>

          <div class="ml-6">
            <button
              @click="router.push(`/projects/${bid.projectId}`)"
              class="px-4 py-2 bg-indigo-50 text-indigo-600 rounded-lg hover:bg-indigo-100 transition-colors text-sm font-medium"
            >
              查看项目
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
