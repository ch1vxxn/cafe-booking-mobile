<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="px-4 pt-6 pb-6 space-y-5">
    <h1 class="text-2xl font-bold text-cafe-espresso">Профиль</h1>

    <div class="bg-white rounded-2xl shadow-sm p-5 space-y-4">
      <!-- Аватар -->
      <div class="flex items-center gap-4">
        <div class="w-16 h-16 rounded-full bg-cafe-caramel/20 flex items-center justify-center text-2xl font-bold text-cafe-espresso">
          {{ auth.user?.firstName?.[0] }}{{ auth.user?.lastName?.[0] }}
        </div>
        <div>
          <p class="text-lg font-bold text-cafe-espresso">{{ auth.user?.firstName }} {{ auth.user?.lastName }}</p>
          <p class="text-sm text-gray-500">{{ auth.user?.phone }}</p>
          <span class="inline-block mt-1 text-[10px] font-semibold px-2 py-0.5 rounded-full" :class="auth.isAdmin ? 'bg-cafe-espresso text-white' : 'bg-cafe-cream text-cafe-espresso'">
            {{ auth.isAdmin ? 'Администратор' : 'Гость' }}
          </span>
        </div>
      </div>

      <!-- Бонусы (лояльность!) -->
      <div class="bg-cafe-cream rounded-xl p-4 flex items-center justify-between">
        <div>
          <p class="text-xs text-gray-500 font-medium">Бонусные баллы</p>
          <p class="text-2xl font-bold text-cafe-espresso">{{ auth.user?.bonusPoints ?? 0 }}</p>
        </div>
        <span class="text-3xl">⭐</span>
      </div>
      <p class="text-[11px] text-gray-400 -mt-2">+50 баллов за каждый завершённый визит</p>

      <!-- Если админ — кнопка в админку -->
      <button
        v-if="auth.isAdmin"
        @click="router.push('/admin')"
        class="press-scale w-full py-3 rounded-xl bg-cafe-espresso text-white font-semibold"
      >
        ⚙️ Панель администратора
      </button>

      <button @click="logout" class="press-scale w-full py-3 rounded-xl bg-red-50 text-red-500 font-semibold">
        Выйти
      </button>
    </div>
  </div>
</template>