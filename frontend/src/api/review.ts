import request from '@/utils/request';
import type { Review } from '@/types/models';

export interface CreateReviewRequest {
  projectId: number;
  rating: number;
  comment?: string;
}

export function createReview(data: CreateReviewRequest): Promise<Review> {
  return request.post('/reviews', data);
}

export function getReviewsByUserId(userId: number): Promise<Review[]> {
  return request.get(`/reviews/user/${userId}`);
}

export function getUserRatingSummary(userId: number): Promise<{ averageRating: number; reviewCount: number }> {
  return request.get(`/reviews/user/${userId}/summary`);
}

export function hasReviewed(projectId: number): Promise<boolean> {
  return request.get(`/reviews/project/${projectId}/has-reviewed`);
}
