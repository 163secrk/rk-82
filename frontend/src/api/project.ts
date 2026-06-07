import request from '@/utils/request';
import type { Project, Dashboard, MonthlyTrend, Milestone, CreateMilestoneRequest, UpdateMilestoneRequest } from '@/types/models';

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

export function getMonthlyTrend(): Promise<MonthlyTrend[]> {
  return request.get('/users/monthly-trend');
}

export function createMilestone(projectId: number, data: CreateMilestoneRequest): Promise<Milestone> {
  return request.post(`/projects/${projectId}/milestones`, data);
}

export function getMilestones(projectId: number): Promise<Milestone[]> {
  return request.get(`/projects/${projectId}/milestones`);
}

export function getMilestoneProgress(projectId: number): Promise<number> {
  return request.get(`/projects/${projectId}/milestones/progress`);
}

export function updateMilestone(projectId: number, milestoneId: number, data: UpdateMilestoneRequest): Promise<Milestone> {
  return request.put(`/projects/${projectId}/milestones/${milestoneId}`, data);
}

export function toggleMilestoneCompletion(projectId: number, milestoneId: number): Promise<Milestone> {
  return request.put(`/projects/${projectId}/milestones/${milestoneId}/toggle`);
}

export function deleteMilestone(projectId: number, milestoneId: number): Promise<void> {
  return request.delete(`/projects/${projectId}/milestones/${milestoneId}`);
}
