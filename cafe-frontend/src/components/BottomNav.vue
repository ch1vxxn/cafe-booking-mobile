<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

/**
 * Вкладки нижней навигации.
 * icons — это inline-SVG (не эмодзи!), цвет управляется через currentColor,
 * поэтому иконка сама становится тёмной/серой в зависимости от :class кнопки.
 */
const tabs = [
  { id: 'home', label: 'Кафе', path: '/', icon: 'home' },
  { id: 'booking', label: 'Бронь', path: '/booking', icon: 'calendar' },
  { id: 'my', label: 'Мои', path: '/my-bookings', icon: 'ticket' },
  { id: 'profile', label: 'Профиль', path: '/profile', icon: 'user' },
]

// Если админ — добавляем вкладку «Админ»
if (auth.isAdmin) {
  tabs.push({ id: 'admin', label: 'Админ', path: '/admin', icon: 'gear' })
}

// SVG-пути иконок (иконки stroke-стиля, как в iOS)
const paths: Record<string, string> = {
  home: 'M3 10.5 12 3l9 7.5M5 9.5V21h14V9.5',
  calendar: 'M8 2v4M16 2v4M3 8h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z',
  ticket: 'M2 9a3 3 0 0 0 0 6v3a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1v-3a3 3 0 0 1 0-6V6a1 1 0 0 0-1-1H3a1 1 0 0 0-1 1v3Z M13 5v2M13 17v2M13 11v2',
  user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM4 21a8 8 0 0 1 16 0',
  gear: 'M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm7.4-3a7.4 7.4 0 0 0-.1-1.2l2-1.5-2-3.4-2.3 1a7.6 7.6 0 0 0-2-1.2L14.6 3h-4l-.4 2.7a7.6 7.6 0 0 0-2 1.2l-2.3-1-2 3.4 2 1.5a7.4 7.4 0 0 0 0 2.4l-2 1.5 2 3.4 2.3-1a7.6 7.6 0 0 0 2 1.2l.4 2.7h4l.4-2.7a7.6 7.6 0 0 0 2-1.2l2.3 1 2-3.4-2-1.5c.1-.4.1-.8.1-1.2Z',
}
</script>

<template>
  <nav class="bg-white/90 rounded-full backdrop-blur-xl border-t border-black/5 px-2 pb-safe flex-shrink-0">
    <div class="flex items-center justify-around py-2">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="router.push(tab.path)"
        class="press-scale flex flex-col items-center gap-0.5 px-3 py-1 rounded-full transition-colors min-w-0"
        :class="route.path === tab.path ? 'text-cafe-espresso bg-cafe-cream font-semibold' : 'text-gray-400'"
      >
        <!-- Inline SVG-иконка: цвет наследуется от currentColor кнопки -->
        <svg
          class="w-6 h-6"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.8"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path :d="paths[tab.icon]" />
        </svg>
        <span class="text-[10px] font-medium leading-tight whitespace-nowrap">{{ tab.label }}</span>
      </button>
    </div>
  </nav>
</template>