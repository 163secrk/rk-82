export type UserRole = 'CLIENT' | 'FREELANCER' | 'ADMIN';
export type ProjectStatus = 'DRAFT' | 'PUBLISHED' | 'BIDDING' | 'IN_PROGRESS' | 'DELIVERED' | 'COMPLETED' | 'CANCELLED';
export type BidStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';
export type PaymentType = 'ESCROW' | 'RELEASE' | 'REFUND' | 'DEPOSIT' | 'WITHDRAW';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED';
export type MessageType = 'TEXT' | 'FILE';

export interface UserInfo {
  id: number;
  email: string;
  nickname: string;
  role: UserRole;
  avatar: string;
  balance: number;
  rating: number;
  description: string;
  skills: string;
}

export interface LoginResponse {
  token: string;
  user: UserInfo;
}

export interface Project {
  id: number;
  title: string;
  description: string;
  category: string;
  budgetMin: number;
  budgetMax: number;
  deadline: string;
  requirements: string;
  status: ProjectStatus;
  clientId: number;
  clientName: string;
  clientAvatar: string;
  freelancerId?: number;
  freelancerName?: string;
  freelancerAvatar?: string;
  agreedPrice?: number;
  startDate?: string;
  endDate?: string;
  createdAt: string;
  updatedAt: string;
  bidCount: number;
  clientReviewed?: boolean;
  freelancerReviewed?: boolean;
  milestoneProgress?: number;
  milestones?: Milestone[];
}

export interface Review {
  id: number;
  projectId: number;
  projectTitle: string;
  reviewerId: number;
  reviewerName: string;
  reviewerAvatar: string;
  revieweeId: number;
  revieweeName: string;
  revieweeAvatar: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface Bid {
  id: number;
  projectId: number;
  freelancerId: number;
  freelancerName: string;
  freelancerAvatar: string;
  price: number;
  deliveryDays: number;
  proposal: string;
  status: BidStatus;
  createdAt: string;
}

export interface Message {
  id: number;
  projectId: number;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  receiverId: number;
  content: string;
  type: MessageType;
  fileUrl?: string;
  isRead: boolean;
  createdAt: string;
}

export interface Payment {
  id: number;
  projectId?: number;
  projectTitle?: string;
  payerId: number;
  payerName: string;
  payeeId: number;
  payeeName: string;
  amount: number;
  type: PaymentType;
  status: PaymentStatus;
  remark: string;
  createdAt: string;
}

export interface Milestone {
  id: number;
  projectId: number;
  name: string;
  description?: string;
  expectedDate: string;
  actualDate?: string;
  completed: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMilestoneRequest {
  name: string;
  description?: string;
  expectedDate: string;
  sortOrder?: number;
}

export interface UpdateMilestoneRequest {
  name: string;
  description?: string;
  expectedDate: string;
  sortOrder?: number;
}

export interface Dashboard {
  publishedProjects: number;
  inProgressProjects: number;
  completedProjects: number;
  pendingBids: number;
  unreadMessages: number;
  totalEarnings: number;
  totalSpent: number;
  balance: number;
}

export interface MonthlyTrend {
  month: string;
  amount: number;
}

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
