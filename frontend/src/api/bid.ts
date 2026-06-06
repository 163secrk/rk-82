import request from '@/utils/request';
import type { Bid } from '@/types/models';

export interface CreateBidRequest {
  price: number;
  deliveryDays: number;
  proposal: string;
}

export function createBid(projectId: number, data: CreateBidRequest): Promise<Bid> {
  return request.post(`/projects/${projectId}/bids`, data);
}

export function getBidsByProject(projectId: number): Promise<Bid[]> {
  return request.get(`/projects/${projectId}/bids`);
}

export function getMyBids(): Promise<Bid[]> {
  return request.get('/bids/my');
}

export function acceptBid(bidId: number): Promise<Bid> {
  return request.post(`/bids/${bidId}/accept`);
}
