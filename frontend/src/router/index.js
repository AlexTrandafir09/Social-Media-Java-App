import { createRouter, createWebHistory } from 'vue-router'
import { authState } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import FeedView from '../views/FeedView.vue'
import CreatePostView from '../views/CreatePostView.vue'
import EditPostView from '../views/EditPostView.vue'
import UsersListView from '../views/UsersListView.vue'
import ProfileView from '../views/ProfileView.vue'
import SettingsView from '../views/SettingsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import ActivityLogView from '../views/ActivityLogView.vue'
import NotFoundView from '../views/NotFoundView.vue'

const routes = [
  { path: '/', name: 'feed', component: FeedView, meta: { requiresAuth: true } },
  { path: '/posts/new', name: 'create-post', component: CreatePostView, meta: { requiresAuth: true } },
  { path: '/posts/:id/edit', name: 'edit-post', component: EditPostView, meta: { requiresAuth: true } },
  { path: '/users', name: 'users', component: UsersListView, meta: { requiresAuth: true } },
  { path: '/users/:id', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/settings', name: 'settings', component: SettingsView, meta: { requiresAuth: true } },
  { path: '/notifications', name: 'notifications', component: NotificationsView, meta: { requiresAuth: true } },
  { path: '/admin/activity', name: 'activity', component: ActivityLogView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterView },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authState.user) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && authState.user?.role !== 'ADMIN') {
    return { name: 'feed' }
  }
})

export default router
