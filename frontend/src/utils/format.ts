export function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatMoney(amount: number): string {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(amount);
}

export function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '招标中',
    BIDDING: '已选标',
    IN_PROGRESS: '进行中',
    DELIVERED: '已交付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    PENDING: '待处理',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝'
  };
  return statusMap[status] || status;
}

export function getStatusColor(status: string): string {
  const colorMap: Record<string, string> = {
    DRAFT: 'bg-gray-100 text-gray-800',
    PUBLISHED: 'bg-blue-100 text-blue-800',
    BIDDING: 'bg-yellow-100 text-yellow-800',
    IN_PROGRESS: 'bg-indigo-100 text-indigo-800',
    DELIVERED: 'bg-orange-100 text-orange-800',
    COMPLETED: 'bg-green-100 text-green-800',
    CANCELLED: 'bg-red-100 text-red-800',
    PENDING: 'bg-yellow-100 text-yellow-800',
    ACCEPTED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800'
  };
  return colorMap[status] || 'bg-gray-100 text-gray-800';
}

export function getRoleText(role: string): string {
  const roleMap: Record<string, string> = {
    CLIENT: '发包方',
    FREELANCER: '接包方',
    ADMIN: '管理员'
  };
  return roleMap[role] || role;
}
