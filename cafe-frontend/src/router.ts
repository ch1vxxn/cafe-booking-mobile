import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

/**
 * Роутер — «навигация» между страницами.
 *
 * Каждая запись = URL -> компонент:
 *   /            -> HomeView (залы + столы)
 *   /booking     -> BookingView (выбор даты/времени, свободные столы)
 *   /my-bookings -> MyBookingsView (мои брони с QR)
 *   /profile     -> ProfileView (профиль, бонусы)
 *   /login       -> LoginView
 *   /admin       -> AdminView (только админ)
 *
 * meta.requiresAuth / meta.requiresAdmin — «охранники» маршрутов:
 *  - если requiresAuth и гость не залогинен -> редирект на /login;
 *  - если requiresAdmin и гость не админ  -> редирект на главную.
 * (Это фронтовая защита; настоящая безопасность — на бэкенде.)
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('./views/HomeView.vue') },
    { path: '/booking', component: () => import('./views/BookingView.vue'), meta: { requiresAuth: true } },
    { path: '/my-bookings', component: () => import('./views/MyBookingsView.vue'), meta: { requiresAuth: true } },
    { path: '/profile', component: () => import('./views/ProfileView.vue'), meta: { requiresAuth: true } },
    { path: '/login', component: () => import('./views/LoginView.vue') },
    { path: '/admin', component: () => import('./views/AdminView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// Глобальный «охранник»: выполняется перед каждым переходом
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { path: '/' }
  }
  return true
})

export default router