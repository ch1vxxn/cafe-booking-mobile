<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from './components/BottomNav.vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const auth = useAuthStore()

// Показываем нижнюю навигацию не на всех экранах (например, на логине не надо)
const showNav = computed(() => !['/login'].includes(route.path))
</script>

<template>
  <!-- Мобильный контейнер: максимум 420px по центру, как телефон -->
  <div class="h-[100dvh] w-full max-w-[420px] mx-auto flex flex-col bg-cafe-cream overflow-hidden select-none">
    <!-- Контент страниц -->
    <main class="flex-1 overflow-y-auto overscroll-contain">
      <router-view />
    </main>

    <!-- Нижняя навигация (только на основных экранах) -->
    <BottomNav v-if="showNav && auth.isLoggedIn" />
  </div>
</template>