import request from '@/utils/request';
import type { LoginResponse, UserInfo } from '@/types/models';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  nickname: string;
  role: 'CLIENT' | 'FREELANCER';
}

export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/auth/login', data);
}

export function register(data: RegisterRequest): Promise<UserInfo> {
  return request.post('/auth/register', data);
}

export function getCurrentUser(): Promise<UserInfo> {
  return request.get('/auth/me');
}
