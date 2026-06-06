import request from '@/utils/request';
import type { Project, Dashboard } from '@/types/models';

export interface CreateProjectRequest {
  title: string;
  description: string;
  category: string;
  budgetMin: number;
  budgetMax: number;
  deadline: string;
  requirements: string;
}

export interface ProjectListParams {
  page?: number;
  size?: number;
  status?: string;
  category?: string;
  keyword?: string;
}

export function createProject(data: CreateProjectRequest): Promise<Project> {
  return request.post('/projects', data);
}

export function getProjects(params: ProjectListParams): Promise<{ content: Project[]; totalElements: number }> {
  return request.get('/projects', { params });
}

export function getProjectDetail(id: number): Promise<Project> {
  return request.get(`/projects/${id}`);
}

export function getMyProjects(): Promise<Project[]> {
  return request.get('/projects/my');
}

export function updateProjectStatus(id: number, status: string): Promise<Project> {
  return request.put(`/projects/${id}/status?status=${status}`);
}

export function deliverProject(id: number): Promise<Project> {
  return request.put(`/projects/${id}/deliver`);
}

export function getDashboard(): Promise<Dashboard> {
  return request.get('/users/dashboard');
}
