import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/ProjectList.vue')
      },
      {
        path: 'projects/create',
        name: 'CreateProject',
        component: () => import('@/views/CreateProject.vue'),
        meta: { roles: ['CLIENT'] }
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/ProjectDetail.vue')
      },
      {
        path: 'my-projects',
        name: 'MyProjects',
        component: () => import('@/views/MyProjects.vue')
      },
      {
        path: 'my-bids',
        name: 'MyBids',
        component: () => import('@/views/MyBids.vue')
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue')
      },
      {
        path: 'chat/:projectId',
        name: 'ChatDetail',
        component: () => import('@/views/Chat.vue')
      },
      {
        path: 'wallet',
        name: 'Wallet',
        component: () => import('@/views/Wallet.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth !== false && !authStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }

  if (to.meta.requiresAuth && authStore.isLoggedIn && !authStore.user) {
    try {
      await authStore.fetchCurrentUser();
    } catch (error) {
      next({ path: '/login' });
      return;
    }
  }

  if (to.meta.roles && authStore.user) {
    const roles = to.meta.roles as string[];
    if (!roles.includes(authStore.user.role)) {
      next({ path: '/dashboard' });
      return;
    }
  }

  next();
});

export default router;
