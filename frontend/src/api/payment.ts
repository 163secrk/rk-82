import request from '@/utils/request';
import type { Payment } from '@/types/models';

export interface EscrowRequest {
  projectId: number;
  amount: number;
}

export interface ReleaseRequest {
  projectId: number;
  amount: number;
}

export function escrow(data: EscrowRequest): Promise<Payment> {
  return request.post('/payments/escrow', data);
}

export function release(data: ReleaseRequest): Promise<Payment> {
  return request.post('/payments/release', data);
}

export function deposit(amount: number): Promise<Payment> {
  return request.post(`/payments/deposit?amount=${amount}`);
}

export function withdraw(amount: number): Promise<Payment> {
  return request.post(`/payments/withdraw?amount=${amount}`);
}

export function getPaymentHistory(): Promise<Payment[]> {
  return request.get('/payments/history');
}
