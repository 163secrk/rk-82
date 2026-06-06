<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Wallet, Plus, Minus, ArrowUpRight, ArrowDownLeft, Loader2 } from 'lucide-vue-next';
import { getPaymentHistory, deposit, withdraw } from '@/api/payment';
import { formatMoney, formatDate } from '@/utils/format';
import type { Payment } from '@/types/models';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

const payments = ref<Payment[]>([]);
const loading = ref(true);
const showDepositModal = ref(false);
const showWithdrawModal = ref(false);
const amount = ref('');
const submitting = ref(false);
const error = ref('');

const fetchData = async () => {
  try {
    const data = await getPaymentHistory();
    payments.value = data;
    if (authStore.user) {
      const lastPayment = data.find(p => p.status === 'COMPLETED');
      if (lastPayment && authStore.user) {
        if (lastPayment.payerId === authStore.user.id) {
          authStore.user.balance -= lastPayment.amount;
        } else {
          authStore.user.balance += lastPayment.amount;
        }
      }
    }
  } catch (error) {
    console.error('Failed to fetch payment history:', error);
  } finally {
    loading.value = false;
  }
};

const handleDeposit = async () => {
  if (!amount.value) {
    error.value = '请输入充值金额';
    return;
  }

  const amt = Number(amount.value);
  if (amt <= 0) {
    error.value = '充值金额必须大于0';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await deposit(amt);
    showDepositModal.value = false;
    amount.value = '';
    if (authStore.user) {
      authStore.user.balance += amt;
    }
    fetchData();
  } catch (err: any) {
    error.value = err.message || '充值失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const handleWithdraw = async () => {
  if (!amount.value) {
    error.value = '请输入提现金额';
    return;
  }

  const amt = Number(amount.value);
  if (amt <= 0) {
    error.value = '提现金额必须大于0';
    return;
  }

  if (authStore.user && amt > authStore.user.balance) {
    error.value = '余额不足';
    return;
  }

  submitting.value = true;
  error.value = '';

  try {
    await withdraw(amt);
    showWithdrawModal.value = false;
    amount.value = '';
    if (authStore.user) {
      authStore.user.balance -= amt;
    }
    fetchData();
  } catch (err: any) {
    error.value = err.message || '提现失败，请重试';
  } finally {
    submitting.value = false;
  }
};

const getPaymentIcon = (payment: Payment) => {
  if (payment.payerId === authStore.user?.id) {
    return ArrowUpRight;
  }
  return ArrowDownLeft;
};

const getPaymentColor = (payment: Payment) => {
  if (payment.payerId === authStore.user?.id) {
    return 'text-red-500';
  }
  return 'text-green-500';
};

const getPaymentAmount = (payment: Payment) => {
  if (payment.payerId === authStore.user?.id) {
    return `-${formatMoney(payment.amount)}`;
  }
  return `+${formatMoney(payment.amount)}`;
};

const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    ESCROW: '资金托管',
    RELEASE: '项目付款',
    REFUND: '退款',
    DEPOSIT: '账户充值',
    WITHDRAW: '账户提现'
  };
  return typeMap[type] || type;
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="space-y-6">
    <div class="bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl p-8 text-white">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-indigo-100 mb-2">账户余额</p>
          <p class="text-4xl font-bold">{{ formatMoney(authStore.user?.balance || 0) }}</p>
        </div>
        <div class="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center">
          <Wallet class="w-8 h-8" />
        </div>
      </div>
      <div class="flex space-x-4 mt-6">
        <button
          @click="showDepositModal = true"
          class="flex-1 py-3 bg-white text-indigo-600 font-medium rounded-lg hover:bg-indigo-50 transition-colors flex items-center justify-center"
        >
          <Plus class="w-5 h-5 mr-2" />
          充值
        </button>
        <button
          @click="showWithdrawModal = true"
          class="flex-1 py-3 bg-white/20 text-white font-medium rounded-lg hover:bg-white/30 transition-colors flex items-center justify-center"
        >
          <Minus class="w-5 h-5 mr-2" />
          提现
        </button>
      </div>
    </div>

    <div class="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
      <h3 class="text-lg font-semibold text-gray-800 mb-6">交易记录</h3>

      <div v-if="loading" class="flex items-center justify-center py-12">
        <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
      </div>

      <div v-else-if="payments.length === 0" class="text-center py-12">
        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <Wallet class="w-8 h-8 text-gray-400" />
        </div>
        <h4 class="text-lg font-medium text-gray-800 mb-2">暂无交易记录</h4>
        <p class="text-gray-500">您还没有任何交易记录</p>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="payment in payments"
          :key="payment.id"
          class="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
        >
          <div class="flex items-center space-x-4">
            <div
              :class="[
                'w-10 h-10 rounded-lg flex items-center justify-center',
                payment.payerId === authStore.user?.id ? 'bg-red-100' : 'bg-green-100'
              ]"
            >
              <component
                :is="getPaymentIcon(payment)"
                :class="['w-5 h-5', getPaymentColor(payment)]"
              />
            </div>
            <div>
              <p class="font-medium text-gray-800">
                {{ getTypeText(payment.type) }}
                <span v-if="payment.projectTitle" class="text-gray-500 font-normal">
                  - {{ payment.projectTitle }}
                </span>
              </p>
              <p class="text-sm text-gray-500">{{ formatDate(payment.createdAt) }}</p>
            </div>
          </div>
          <div class="text-right">
            <p :class="['font-semibold', getPaymentColor(payment)]">
              {{ getPaymentAmount(payment) }}
            </p>
            <p class="text-xs text-gray-400">{{ payment.status }}</p>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showDepositModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showDepositModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-md mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-6">账户充值</h3>
        <form @submit.prevent="handleDeposit" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">充值金额（元）</label>
            <input
              v-model="amount"
              type="number"
              min="0"
              step="0.01"
              placeholder="请输入充值金额"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showDepositModal = false"
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
              {{ submitting ? '处理中...' : '确认充值' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showWithdrawModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="showWithdrawModal = false"
    >
      <div class="bg-white rounded-xl p-8 w-full max-w-md mx-4">
        <h3 class="text-xl font-bold text-gray-800 mb-2">账户提现</h3>
        <p class="text-gray-500 mb-6">
          可用余额：{{ formatMoney(authStore.user?.balance || 0) }}
        </p>
        <form @submit.prevent="handleWithdraw" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">提现金额（元）</label>
            <input
              v-model="amount"
              type="number"
              min="0"
              step="0.01"
              :max="authStore.user?.balance"
              placeholder="请输入提现金额"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
            />
          </div>
          <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
          <div class="flex space-x-4">
            <button
              type="button"
              @click="showWithdrawModal = false"
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
              {{ submitting ? '处理中...' : '确认提现' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
