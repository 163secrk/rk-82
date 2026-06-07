<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft,
  Calendar,
  User,
  MessageSquare,
  Gavel,
  Shield,
  CheckCircle,
  Clock,
  Loader2,
  Star,
  Flag,
  Plus,
  Trash2,
  Edit2,
  X
} from 'lucide-vue-next';
import { getProjectDetail, createMilestone, updateMilestone, toggleMilestoneCompletion, deleteMilestone } from '@/api/project';
import { getBidsByProject, createBid, acceptBid } from '@/api/bid';
import { escrow, release } from '@/api/payment';
import { deliverProject } from '@/api/project';
import { createReview } from '@/api/review';
import type { Review, Milestone, CreateMilestoneRequest, UpdateMilestoneRequest } from '@/types/models';
import { formatMoney, formatDate, getStatusText, getStatusColor } from '@/utils/format';
import type { Project, Bid } from '@/types/models';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const projectId = computed(() => Number(route.params.id));
const project = ref<Project | null>(null);
const bids = ref<Bid[]>([]);
const loading = ref(true);
const submitting = ref(false);

const showBidModal = ref(false);
const bidPrice = ref('');
const bidDays = ref('');
const bidProposal = ref('');

const showEscrowModal = ref(false);
const escrowAmount = ref('');

const showReleaseModal = ref(false);
const releaseAmount = ref('');

const showReviewModal = ref(false);
const reviewRating = ref(5);
const reviewComment = ref('');
const hoverRating = ref(0);
const reviews = ref<Review[]>([]);

const error = ref('');

const showMilestoneModal = ref(false);
const editingMilestone = ref<Milestone | null>(null);
const milestoneName = ref('');
const milestoneDescription = ref('');
const milestoneExpectedDate = ref('');

const milestones = computed(() => project.value?.milestones || []);

const completedMilestones = computed(() => {
  return milestones.value.filter(m => m.completed).length;
});

const openCreateMilestone = () => {
  editingMilestone.value = null;
  milestoneName.value = '';
  milestoneDescription.value = '';
  milestoneExpectedDate.value = '';
  showMilestoneModal.value = true;
};

const openEditMilestone = (milestone: Milestone) => {
  editingMilestone.value = milestone;
  milestoneName.value = milestone.name;
  milestoneDescription.value = milestone.description || '';
  milestoneExpectedDate.value = milestone.expectedDate.slice(0, 16);
  showMilestoneModal.value = true;
};

const handleSaveMilestone = async () => {
  if (!milestoneName.value.trim()) {
    error.value = '里程碑名称不能为空';
    return;
  }
  if (!milestoneExpectedDate.value) {
    error.value = '请选择预计完成时间';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    const data: CreateMilestoneRequest | UpdateMilestoneRequest = {
      name: milestoneName.value.trim(),
      description: milestoneDescription.value.trim() || undefined,
      expectedDate: new Date(milestoneExpectedDate.value).toISOString()
    };

    if (editingMilestone.value) {
      await updateMilestone(projectId.value, editingMilestone.value.id, data as UpdateMilestoneRequest);
    } else {
      await createMilestone(projectId.value, data);
    }

    showMilestoneModal.value = false;
    fetchData();
  } catch (err: any) {
    error.value = err.message || '操作失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const handleToggleMilestone = async (milestoneId: number) => {
  try {
    await toggleMilestoneCompletion(projectId.value, milestoneId);
    fetchData();
  } catch (err: any) {
    alert(err.message || '操作失败');
  }
};

const handleDeleteMilestone = async (milestoneId: number) => {
  if (!confirm('确定要删除这个里程碑吗？')) return;

  try {
    await deleteMilestone(projectId.value, milestoneId);
    fetchData();
  } catch (err: any) {
    alert(err.message || '删除失败');
  }
};

const fetchData = async () => {
  try {
    const [projectData, bidsData] = await Promise.all([
      getProjectDetail(projectId.value),
      getBidsByProject(projectId.value)
    ]);
    project.value = projectData;
    bids.value = bidsData;
  } catch (error) {
    console.error('Failed to fetch project detail:', error);
  } finally {
    loading.value = false;
  }
};

const isOwner = computed(() => {
  return authStore.user?.id === project.value?.clientId;
});

const isFreelancer = computed(() => {
  return authStore.user?.id === project.value?.freelancerId;
});

const canBid = computed(() => {
  return authStore.isFreelancer &&
    project.value?.status === 'PUBLISHED' &&
    !bids.value.some(b => b.freelancerId === authStore.user?.id);
});

const canReview = computed(() => {
  if (!project.value || project.value.status !== 'COMPLETED') return false;
  if (isOwner.value && !project.value.clientReviewed) return true;
  if (isFreelancer.value && !project.value.freelancerReviewed) return true;
  return false;
});

const canChat = computed(() => {
  if (!project.value || project.value.status === 'DRAFT') return false;
  if (isFreelancer.value) {
    return project.value.freelancerId === authStore.user?.id;
  }
  if (isOwner.value) {
    return project.value.status === 'IN_PROGRESS' ||
           project.value.status === 'DELIVERED' ||
           project.value.status === 'COMPLETED';
  }
  return false;
});

const reviewTarget = computed(() => {
  if (isOwner.value) return project.value?.freelancerName;
  if (isFreelancer.value) return project.value?.clientName;
  return '';
});

const handleBid = async () => {
  if (!bidPrice.value || !bidDays.value || !bidProposal.value) {
    error.value = '请填写所有竞标信息';
    return;
  }

  const price = Number(bidPrice.value);
  const days = Number(bidDays.value);

  if (price <= 0 || days <= 0) {
    error.value = '请输入有效的金额和天数';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await createBid(projectId.value, {
      price,
      deliveryDays: days,
      proposal: bidProposal.value
    });
    showBidModal.value = false;
    bidPrice.value = '';
    bidDays.value = '';
    bidProposal.value = '';
    fetchData();
  } catch (err: any) {
    error.value = err.message || '竞标失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const handleAcceptBid = async (bidId: number) => {
  if (!confirm('确定要接受此竞标吗？接受后将无法更改。')) return;

  try {
    await acceptBid(bidId);
    fetchData();
  } catch (err: any) {
    alert(err.message || '操作失败');
  }
};

const handleEscrow = async () => {
  if (!escrowAmount.value) {
    error.value = '请输入托管金额';
    return;
  }

  const amount = Number(escrowAmount.value);
  if (amount <= 0) {
    error.value = '托管金额必须大于0';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await escrow({ projectId: projectId.value, amount });
    showEscrowModal.value = false;
    escrowAmount.value = '';
    fetchData();
  } catch (err: any) {
    error.value = err.message || '托管失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const handleDeliver = async () => {
  if (!confirm('确定要提交交付吗？')) return;

  try {
    await deliverProject(projectId.value);
    fetchData();
  } catch (err: any) {
    alert(err.message || '交付失败');
  }
};

const handleRelease = async () => {
  if (!releaseAmount.value) {
    error.value = '请输入释放金额';
    return;
  }

  const amount = Number(releaseAmount.value);
  if (amount <= 0) {
    error.value = '释放金额必须大于0';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await release({ projectId: projectId.value, amount });
    showReleaseModal.value = false;
    releaseAmount.value = '';
    fetchData();
  } catch (err: any) {
    error.value = err.message || '释放失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const handleChat = () => {
  router.push(`/chat/${projectId.value}`);
};

const handleReview = async () => {
  if (reviewRating.value < 1 || reviewRating.value > 5) {
    error.value = '请选择有效的评分';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await createReview({
      projectId: projectId.value,
      rating: reviewRating.value,
      comment: reviewComment.value.trim() || undefined
    });
    showReviewModal.value = false;
    reviewRating.value = 5;
    reviewComment.value = '';
    fetchData();
  } catch (err: any) {
    error.value = err.message || '评价失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const setRating = (rating: number) => {
  reviewRating.value = rating;
};

const setHoverRating = (rating: number) => {
  hoverRating.value = rating;
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div v-if="!loading && project" class="space-y-6">
    <button
      @click="router.back()"
      class="flex items-center text-gray-600 hover:text-indigo-600 transition-colors"
    >
      <ArrowLeft class="w-5 h-5 mr-2" />
      返回
    </button>

    <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <div class="flex items-start justify-between mb-6">
        <div>
          <div class="flex items-center space-x-3 mb-3">
            <h1 class="text-2xl font-bold text-gray-800">{{ project.title }}</h1>
            <span
              :class="[
                'px-3 py-1 rounded-full text-sm font-medium',
                getStatusColor(project.status)
              ]"
            >
              {{ getStatusText(project.status) }}
            </span>
            <span class="px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-sm">
              {{ project.category }}
            </span>
          </div>

          <div class="flex flex-wrap items-center gap-6 text-sm text-gray-500">
            <div class="flex items-center space-x-2">
              <User class="w-4 h-4" />
              <span>发包方：{{ project.clientName }}</span>
            </div>
            <div class="flex items-center space-x-2">
              <Calendar class="w-4 h-4" />
              <span>截止日期：{{ formatDate(project.deadline) }}</span>
            </div>
            <div class="flex items-center space-x-2">
              <Gavel class="w-4 h-4" />
              <span>{{ bids.length }} 人竞标</span>
            </div>
          </div>
        </div>

        <div class="text-right">
          <p class="text-3xl font-bold text-indigo-600">
            {{ formatMoney(project.budgetMin) }} - {{ formatMoney(project.budgetMax) }}
          </p>
          <p class="text-sm text-gray-500 mt-1">预算范围</p>
          <p v-if="project.agreedPrice" class="text-lg font-semibold text-green-600 mt-2">
            成交价：{{ formatMoney(project.agreedPrice) }}
          </p>
        </div>
      </div>

      <div class="border-t border-gray-100 pt-6">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">项目描述</h3>
        <p class="text-gray-600 leading-relaxed whitespace-pre-wrap">{{ project.description }}</p>
      </div>

      <div class="border-t border-gray-100 pt-6 mt-6">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center space-x-2">
            <Flag class="w-5 h-5 text-indigo-600" />
            <h3 class="text-lg font-semibold text-gray-800">项目里程碑</h3>
            <span class="text-sm text-gray-500">
              ({{ completedMilestones }}/{{ milestones.length }})
            </span>
          </div>
          <button
            v-if="isOwner"
            @click="openCreateMilestone"
            class="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors flex items-center"
          >
            <Plus class="w-4 h-4 mr-1" />
            添加里程碑
          </button>
        </div>

        <div v-if="milestones.length > 0" class="mb-6">
          <div class="flex items-center justify-between text-sm mb-2">
            <span class="text-gray-600">总体进度</span>
            <span class="font-semibold text-indigo-600">{{ project.milestoneProgress || 0 }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div
              class="bg-indigo-600 h-3 rounded-full transition-all duration-500"
              :style="{ width: `${project.milestoneProgress || 0}%` }"
            ></div>
          </div>
        </div>

        <div v-if="milestones.length > 0" class="relative">
          <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>
          <div class="space-y-6">
            <div
              v-for="(milestone, index) in milestones"
              :key="milestone.id"
              class="relative pl-12"
            >
              <div
                :class="[
                  'absolute left-0 w-8 h-8 rounded-full flex items-center justify-center border-4',
                  milestone.completed
                    ? 'bg-green-500 border-green-100'
                    : 'bg-white border-gray-300'
                ]"
              >
                <CheckCircle
                  v-if="milestone.completed"
                  class="w-4 h-4 text-white"
                />
                <span v-else class="text-sm font-semibold text-gray-500">
                  {{ index + 1 }}
                </span>
              </div>

              <div
                :class="[
                  'p-4 rounded-xl border transition-all',
                  milestone.completed
                    ? 'bg-green-50 border-green-200'
                    : 'bg-white border-gray-200 hover:shadow-md'
                ]"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <div class="flex items-center space-x-2">
                      <h4
                        :class="[
                          'font-medium',
                          milestone.completed ? 'text-green-800 line-through' : 'text-gray-800'
                        ]"
                      >
                        {{ milestone.name }}
                      </h4>
                      <span
                        :class="[
                          'px-2 py-0.5 rounded text-xs font-medium',
                          milestone.completed
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-100 text-gray-600'
                        ]"
                      >
                        {{ milestone.completed ? '已完成' : '进行中' }}
                      </span>
                    </div>
                    <p v-if="milestone.description" class="text-sm text-gray-500 mt-1">
                      {{ milestone.description }}
                    </p>
                    <div class="flex items-center space-x-4 mt-2 text-xs text-gray-400">
                      <div class="flex items-center space-x-1">
                        <Clock class="w-3 h-3" />
                        <span>预计：{{ formatDate(milestone.expectedDate) }}</span>
                      </div>
                      <div v-if="milestone.actualDate" class="flex items-center space-x-1">
                        <CheckCircle class="w-3 h-3" />
                        <span>完成：{{ formatDate(milestone.actualDate) }}</span>
                      </div>
                    </div>
                  </div>

                  <div v-if="isOwner" class="flex items-center space-x-1 ml-4">
                    <button
                      @click="handleToggleMilestone(milestone.id)"
                      :class="[
                        'p-2 rounded-lg transition-colors',
                        milestone.completed
                          ? 'text-gray-400 hover:text-gray-600 hover:bg-gray-100'
                          : 'text-green-600 hover:bg-green-50'
                      ]"
                      :title="milestone.completed ? '取消完成' : '标记完成'"
                    >
                      <CheckCircle class="w-4 h-4" />
                    </button>
                    <button
                      @click="openEditMilestone(milestone)"
                      class="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="编辑"
                    >
                      <Edit2 class="w-4 h-4" />
                    </button>
                    <button
                      @click="handleDeleteMilestone(milestone.id)"
                      class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="删除"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 bg-gray-50 rounded-xl">
          <Flag class="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p class="text-gray-500 mb-2">暂无里程碑</p>
          <p v-if="isOwner" class="text-sm text-gray-400">点击上方"添加里程碑"按钮创建项目阶段</p>
          <p v-else class="text-sm text-gray-400">发包方尚未设置项目里程碑</p>
        </div>
      </div>

      <div v-if="project.freelancerId" class="border-t border-gray-100 pt-6 mt-6">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">接包方</h3>
        <div class="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg">
          <div class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center">
            <span class="text-indigo-600 font-semibold">
              {{ project.freelancerName?.charAt(0) }}
            </span>
          </div>
          <div>
            <p class="font-medium text-gray-800">{{ project.freelancerName }}</p>
            <p class="text-sm text-gray-500">接包方</p>
          </div>
        </div>
      </div>

      <div class="flex flex-wrap gap-4 mt-8 pt-6 border-t border-gray-100">
        <button
          v-if="canChat"
          @click="handleChat"
          class="px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors flex items-center"
        >
          <MessageSquare class="w-5 h-5 mr-2" />
          在线沟通
        </button>

        <button
          v-if="canBid"
          @click="showBidModal = true"
          class="px-6 py-3 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors flex items-center"
        >
          <Gavel class="w-5 h-5 mr-2" />
          参与竞标
        </button>

        <button
          v-if="isOwner && project.status === 'BIDDING'"
          @click="showEscrowModal = true"
          class="px-6 py-3 bg-yellow-500 text-white font-medium rounded-lg hover:bg-yellow-600 transition-colors flex items-center"
        >
          <Shield class="w-5 h-5 mr-2" />
          资金托管
        </button>

        <button
          v-if="isFreelancer && project.status === 'IN_PROGRESS'"
          @click="handleDeliver"
          class="px-6 py-3 bg-orange-500 text-white font-medium rounded-lg hover:bg-orange-600 transition-colors flex items-center"
        >
          <CheckCircle class="w-5 h-5 mr-2" />
          提交交付
        </button>

        <button
          v-if="isOwner && project.status === 'DELIVERED'"
          @click="showReleaseModal = true"
          class="px-6 py-3 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors flex items-center"
        >
          <CheckCircle class="w-5 h-5 mr-2" />
          确认验收并付款
        </button>

        <button
          v-if="canReview"
          @click="showReviewModal = true"
          class="px-6 py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors flex items-center"
        >
          <Star class="w-5 h-5 mr-2" />
          评价{{ reviewTarget }}
        </button>

        <div
          v-if="project.status === 'COMPLETED' && !canReview && (isOwner || isFreelancer)"
          class="px-6 py-3 bg-gray-100 text-gray-500 font-medium rounded-lg flex items-center"
        >
          <CheckCircle class="w-5 h-5 mr-2" />
          已完成评价
        </div>
      </div>
    </div>

    <div v-if="bids.length > 0" class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <h3 class="text-lg font-semibold text-gray-800 mb-6">竞标列表（{{ bids.length }}）</h3>
      <div class="space-y-4">
        <div
          v-for="bid in bids"
          :key="bid.id"
          class="p-6 border border-gray-100 rounded-xl hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start space-x-4">
              <div class="w-12 h-12 rounded-full bg-indigo-100 flex items-center justify-center">
                <span class="text-indigo-600 font-semibold">
                  {{ bid.freelancerName?.charAt(0) }}
                </span>
              </div>
              <div>
                <div class="flex items-center space-x-3">
                  <h4 class="font-medium text-gray-800">{{ bid.freelancerName }}</h4>
                  <span
                    :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      getStatusColor(bid.status)
                    ]"
                  >
                    {{ getStatusText(bid.status) }}
                  </span>
                </div>
                <p class="text-sm text-gray-500 mt-1">
                  报价：<span class="font-semibold text-indigo-600">{{ formatMoney(bid.price) }}</span>
                  <span class="mx-2">·</span>
                  交付周期：{{ bid.deliveryDays }} 天
                </p>
                <p class="text-sm text-gray-600 mt-3 whitespace-pre-wrap">{{ bid.proposal }}</p>
                <p class="text-xs text-gray-400 mt-2">{{ formatDate(bid.createdAt) }}</p>
              </div>
            </div>

            <div v-if="isOwner && project.status === 'PUBLISHED' && bid.status === 'PENDING'">
              <button
                @click="handleAcceptBid(bid.id)"
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
              >
                接受竞标
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="project.status === 'PUBLISHED'" class="bg-white rounded-xl p-8 text-center shadow-sm border border-gray-100">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <Gavel class="w-8 h-8 text-gray-400" />
      </div>
      <h4 class="text-lg font-medium text-gray-800 mb-2">暂无竞标</h4>
      <p class="text-gray-500">等待专业人才竞标您的项目</p>
    </div>

    <div
      v-if="showBidModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showBidModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-lg mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-6">参与竞标</h3>
        <form @submit.prevent="handleBid" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">报价金额（元）</label>
            <input
              v-model="bidPrice"
              type="number"
              min="0"
              placeholder="请输入您的报价"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">交付天数</label>
            <input
              v-model="bidDays"
              type="number"
              min="1"
              placeholder="预计交付天数"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">竞标方案</label>
            <textarea
              v-model="bidProposal"
              rows="4"
              placeholder="介绍您的经验、方案和优势..."
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
            ></textarea>
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showBidModal = false"
              class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors flex items-center justify-center"
            >
              <Loader2 v-if="submitting" class="w-5 h-5 animate-spin mr-2" />
              {{ submitting ? '提交中...' : '提交竞标' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showEscrowModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showEscrowModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-lg mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-2">资金托管</h3>
        <p class="text-gray-500 mb-6">托管资金将保证接包方的权益，项目完成后释放</p>
        <form @submit.prevent="handleEscrow" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">托管金额（元）</label>
            <input
              v-model="escrowAmount"
              type="number"
              min="0"
              :placeholder="`建议金额：${project?.agreedPrice || 0}`"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
            <p class="text-sm text-gray-500 mt-2">
              可用余额：{{ formatMoney(authStore.user?.balance || 0) }}
            </p>
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showEscrowModal = false"
              class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 py-3 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 disabled:opacity-50 transition-colors flex items-center justify-center"
            >
              <Loader2 v-if="submitting" class="w-5 h-5 animate-spin mr-2" />
              {{ submitting ? '处理中...' : '确认托管' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showReleaseModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showReleaseModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-lg mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-2">确认验收</h3>
        <p class="text-gray-500 mb-6">确认项目已完成，释放托管资金给接包方</p>
        <form @submit.prevent="handleRelease" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">释放金额（元）</label>
            <input
              v-model="releaseAmount"
              type="number"
              min="0"
              :placeholder="`成交价：${project?.agreedPrice || 0}`"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showReleaseModal = false"
              class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors flex items-center justify-center"
            >
              <Loader2 v-if="submitting" class="w-5 h-5 animate-spin mr-2" />
              {{ submitting ? '处理中...' : '确认付款' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showReviewModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showReviewModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-lg mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-2">评价{{ reviewTarget }}</h3>
        <p class="text-gray-500 mb-6">项目已完成，请为对方的服务进行评价</p>
        <form @submit.prevent="handleReview" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-3">评分</label>
            <div class="flex items-center space-x-2">
              <button
                v-for="i in 5"
                :key="i"
                type="button"
                @click="setRating(i)"
                @mouseenter="setHoverRating(i)"
                @mouseleave="setHoverRating(0)"
                class="focus:outline-none transition-transform hover:scale-110"
              >
                <Star
                  :class="[
                    'w-10 h-10 transition-colors',
                    (hoverRating || reviewRating) >= i
                      ? 'text-yellow-400 fill-current'
                      : 'text-gray-200'
                  ]"
                />
              </button>
              <span class="ml-4 text-2xl font-bold text-gray-700">
                {{ reviewRating }}.0
              </span>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">评价内容（可选）</label>
            <textarea
              v-model="reviewComment"
              rows="4"
              placeholder="分享您的合作体验..."
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
            ></textarea>
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showReviewModal = false"
              class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 transition-colors flex items-center justify-center"
            >
              <Loader2 v-if="submitting" class="w-5 h-5 animate-spin mr-2" />
              {{ submitting ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showMilestoneModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showMilestoneModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-lg mx-4">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-800">
            {{ editingMilestone ? '编辑里程碑' : '添加里程碑' }}
          </h3>
          <button
            @click="showMilestoneModal = false"
            class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X class="w-5 h-5" />
          </button>
        </div>
        <form @submit.prevent="handleSaveMilestone" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">里程碑名称 *</label>
            <input
              v-model="milestoneName"
              type="text"
              placeholder="例如：需求分析完成"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">描述（可选）</label>
            <textarea
              v-model="milestoneDescription"
              rows="3"
              placeholder="描述这个里程碑需要完成的内容..."
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none resize-none"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">预计完成时间 *</label>
            <input
              v-model="milestoneExpectedDate"
              type="datetime-local"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showMilestoneModal = false"
              class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="flex-1 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors flex items-center justify-center"
            >
              <Loader2 v-if="submitting" class="w-5 h-5 animate-spin mr-2" />
              {{ submitting ? '保存中...' : (editingMilestone ? '保存修改' : '创建里程碑') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <div v-else class="flex items-center justify-center h-64">
    <div class="animate-spin rounded-full h-12 w-12 border-4 border-indigo-200 border-t-indigo-600"></div>
  </div>
</template>
