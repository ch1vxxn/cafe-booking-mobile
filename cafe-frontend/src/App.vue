<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BottomNav from './components/BottomNav.vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const auth = useAuthStore()

// Hide bottom nav on login page
const showNav = computed(() => !['/login'].includes(route.path))
</script>

<template>
  <!-- Mobile container: max 420px centered, phone-like -->
  <div class="h-[100dvh] w-full max-w-[420px] mx-auto flex flex-col bg-cafe-cream overflow-hidden select-none">
    <!-- Page content -->
    <main class="flex-1 overflow-y-auto overscroll-contain">
      <router-view />
    </main>

    <!-- Bottom nav (main screens only) -->
    <BottomNav v-if="showNav && auth.isLoggedIn" />
  </div>
</template>