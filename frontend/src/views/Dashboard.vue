<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import {
  Briefcase,
  Clock,
  CheckCircle,
  Gavel,
  MessageSquare,
  TrendingUp,
  TrendingDown,
  Wallet,
  ArrowRight,
  LineChart
} from 'lucide-vue-next';
import * as echarts from 'echarts';
import { getDashboard, getMonthlyTrend } from '@/api/project';
import { getMyProjects } from '@/api/project';
import { formatMoney, getStatusText, getStatusColor } from '@/utils/format';
import type { Dashboard, Project, MonthlyTrend } from '@/types/models';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

const dashboard = ref<Dashboard | null>(null);
const recentProjects = ref<Project[]>([]);
const monthlyTrend = ref<MonthlyTrend[]>([]);
const loading = ref(true);
const chartRef = ref<HTMLDivElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

const fetchData = async () => {
  try {
    const [dashData, projectsData] = await Promise.all([
      getDashboard(),
      getMyProjects()
    ]);
    dashboard.value = dashData;
    recentProjects.value = projectsData.slice(0, 5);

    try {
      const trendData = await getMonthlyTrend();
      monthlyTrend.value = trendData;
    } catch (trendError) {
      console.error('Failed to fetch monthly trend:', trendError);
    }
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
  } finally {
    loading.value = false;
  }
};

const getStatItems = () => {
  if (!dashboard.value) return [];
  
  if (authStore.isClient) {
    return [
      { icon: Briefcase, label: '已发布项目', value: dashboard.value.publishedProjects, color: 'bg-blue-500' },
      { icon: Clock, label: '进行中项目', value: dashboard.value.inProgressProjects, color: 'bg-indigo-500' },
      { icon: CheckCircle, label: '已完成项目', value: dashboard.value.completedProjects, color: 'bg-green-500' },
      { icon: TrendingDown, label: '累计支出', value: formatMoney(dashboard.value.totalSpent), color: 'bg-orange-500' }
    ];
  } else {
    return [
      { icon: Gavel, label: '待处理竞标', value: dashboard.value.pendingBids, color: 'bg-yellow-500' },
      { icon: Clock, label: '进行中项目', value: dashboard.value.inProgressProjects, color: 'bg-indigo-500' },
      { icon: CheckCircle, label: '已完成项目', value: dashboard.value.completedProjects, color: 'bg-green-500' },
      { icon: TrendingUp, label: '累计收入', value: formatMoney(dashboard.value.totalEarnings), color: 'bg-green-500' }
    ];
  }
};

const initChart = () => {
  if (!chartRef.value) return;

  if (chartInstance) {
    chartInstance.dispose();
  }

  chartInstance = echarts.init(chartRef.value);

  const months = monthlyTrend.value.map(item => item.month);
  const amounts = monthlyTrend.value.map(item => item.amount);
  const isClient = authStore.isClient;
  const lineColor = isClient ? '#f97316' : '#22c55e';
  const areaColor = isClient
    ? new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(249, 115, 22, 0.3)' },
        { offset: 1, color: 'rgba(249, 115, 22, 0.05)' }
      ])
    : new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(34, 197, 94, 0.3)' },
        { offset: 1, color: 'rgba(34, 197, 94, 0.05)' }
      ]);

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: {
        color: '#374151'
      },
      formatter: (params: any) => {
        const data = params[0];
        return `<div class="font-medium">${data.name}</div>
                <div class="text-sm">${isClient ? '支出' : '收入'}：<span class="font-semibold" style="color: ${lineColor}">¥${data.value.toLocaleString()}</span></div>`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 11,
        formatter: (value: string) => {
          const parts = value.split('-');
          return `${parts[0].slice(2)}/${parts[1]}`;
        }
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#6b7280',
        fontSize: 11,
        formatter: (value: number) => {
          if (value >= 10000) {
            return (value / 10000) + 'w';
          }
          return value.toString();
        }
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: isClient ? '支出' : '收入',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: {
          color: lineColor,
          width: 2
        },
        itemStyle: {
          color: lineColor,
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: areaColor as any
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            borderWidth: 3
          }
        },
        data: amounts
      }
    ]
  };

  chartInstance.setOption(option);
};

const handleResize = () => {
  chartInstance?.resize();
};

watch([monthlyTrend, loading], () => {
  if (!loading.value && monthlyTrend.value.length > 0) {
    nextTick(() => {
      initChart();
    });
  }
});

onMounted(() => {
  fetchData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chartInstance) {
    chartInstance.dispose();
    chartInstance = null;
  }
});
</script>

<template>
  <div v-if="!loading" class="space-y-6">
    <div class="mb-8">
      <h3 class="text-xl font-semibold text-gray-800 mb-2">
        你好，{{ authStore.user?.nickname }}！
      </h3>
      <p class="text-gray-500">
        {{ authStore.isClient ? '找到合适的人才，顺利完成您的项目' : '发现优质项目，开启您的自由职业之旅' }}
      </p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div
        v-for="(stat, index) in getStatItems()"
        :key="index"
        class="bg-white rounded-xl p-6 shadow-sm border border-gray-100"
      >
        <div class="flex items-center justify-between mb-4">
          <div :class="['w-12 h-12 rounded-lg flex items-center justify-center', stat.color]">
            <component :is="stat.icon" class="w-6 h-6 text-white" />
          </div>
        </div>
        <p class="text-3xl font-bold text-gray-800">{{ stat.value }}</p>
        <p class="text-sm text-gray-500 mt-1">{{ stat.label }}</p>
      </div>

      <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 bg-purple-500 rounded-lg flex items-center justify-center">
            <Wallet class="w-6 h-6 text-white" />
          </div>
        </div>
        <p class="text-3xl font-bold text-gray-800">{{ formatMoney(dashboard?.balance || 0) }}</p>
        <p class="text-sm text-gray-500 mt-1">账户余额</p>
      </div>

      <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 bg-red-500 rounded-lg flex items-center justify-center">
            <MessageSquare class="w-6 h-6 text-white" />
          </div>
        </div>
        <p class="text-3xl font-bold text-gray-800">{{ dashboard?.unreadMessages || 0 }}</p>
        <p class="text-sm text-gray-500 mt-1">未读消息</p>
      </div>
    </div>

    <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center">
            <LineChart class="w-5 h-5 text-white" />
          </div>
          <div>
            <h4 class="text-lg font-semibold text-gray-800">
              {{ authStore.isClient ? '按月支出趋势' : '按月收入趋势' }}
            </h4>
            <p class="text-sm text-gray-500">最近12个月</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span :class="[
            'w-3 h-3 rounded-full',
            authStore.isClient ? 'bg-orange-500' : 'bg-green-500'
          ]"></span>
          <span class="text-sm text-gray-500">{{ authStore.isClient ? '支出' : '收入' }}</span>
        </div>
      </div>
      <div ref="chartRef" class="w-full h-72"></div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-6">
          <h4 class="text-lg font-semibold text-gray-800">最近项目</h4>
          <button
            @click="router.push('/my-projects')"
            class="text-sm text-indigo-600 hover:text-indigo-700 flex items-center"
          >
            查看全部 <ArrowRight class="w-4 h-4 ml-1" />
          </button>
        </div>

        <div v-if="recentProjects.length === 0" class="text-center py-8 text-gray-500">
          暂无项目
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="project in recentProjects"
            :key="project.id"
            class="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
            @click="router.push(`/projects/${project.id}`)"
          >
            <div class="flex-1">
              <h5 class="font-medium text-gray-800">{{ project.title }}</h5>
              <p class="text-sm text-gray-500 mt-1">
                预算：{{ formatMoney(project.budgetMin) }} - {{ formatMoney(project.budgetMax) }}
              </p>
            </div>
            <span
              :class="[
                'px-3 py-1 rounded-full text-xs font-medium',
                getStatusColor(project.status)
              ]"
            >
              {{ getStatusText(project.status) }}
            </span>
          </div>
        </div>
      </div>

      <div class="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl p-6 text-white">
        <h4 class="text-lg font-semibold mb-4">
          {{ authStore.isClient ? '准备发布新项目？' : '寻找新项目？' }}
        </h4>
        <p class="text-indigo-100 mb-6">
          {{
            authStore.isClient
              ? '发布您的项目需求，从众多专业人才中找到最合适的接包方'
              : '浏览项目市场，找到适合您技能和兴趣的优质项目'
          }}
        </p>
        <button
          @click="router.push(authStore.isClient ? '/projects/create' : '/projects')"
          class="w-full py-3 bg-white text-indigo-600 font-medium rounded-lg hover:bg-indigo-50 transition-colors"
        >
          {{ authStore.isClient ? '立即发布项目' : '浏览项目市场' }}
        </button>
      </div>
    </div>
  </div>

  <div v-else class="flex items-center justify-center h-64">
    <div class="animate-spin rounded-full h-12 w-12 border-4 border-indigo-200 border-t-indigo-600"></div>
  </div>
</template>
