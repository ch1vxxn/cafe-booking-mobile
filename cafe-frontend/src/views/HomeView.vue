<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useAuthStore } from '../stores/auth'
import type { Hall, RestaurantTable } from '../types'

const auth = useAuthStore()
const router = useRouter()

const halls = ref<Hall[]>([])
const tables = ref<RestaurantTable[]>([])
const selectedHallId = ref<number | null>(null)
const loading = ref(true)
const error = ref('')

async function loadHalls() {
  try {
    halls.value = await api.halls()
    if (halls.value.length > 0) {
      selectedHallId.value = halls.value[0].id
      await loadTables(halls.value[0].id)
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось загрузить залы'
  } finally {
    loading.value = false
  }
}

async function loadTables(hallId: number) {
  try {
    tables.value = await api.tables(hallId)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось загрузить столы'
  }
}

function selectHall(hallId: number) {
  selectedHallId.value = hallId
  loadTables(hallId)
}

onMounted(loadHalls)
</script>

<template>
  <div class="px-4 pt-6 pb-4 space-y-5">
    <!-- Шапка -->
    <header class="flex items-center justify-between">
      <div>
        <p class="text-xs text-gray-500 font-medium">
          {{ auth.isLoggedIn ? `Привет, ${auth.user?.firstName}!` : 'Добро пожаловать' }}
        </p>
        <h1 class="text-2xl font-bold text-cafe-espresso mt-0.5">Наше кафе</h1>
      </div>
      <button
        v-if="!auth.isLoggedIn"
        @click="router.push('/login')"
        class="press-scale text-sm font-medium text-cafe-caramel bg-white px-4 py-2 rounded-xl shadow-sm"
      >
        Войти
      </button>
      <div v-else class="w-10 h-10 bg-cafe-caramel/20 rounded-full flex items-center justify-center text-lg">
        {{ auth.user?.firstName?.[0] ?? '👤' }}
      </div>
    </header>

    <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

    <!-- Загрузка -->
    <div v-if="loading" class="text-center text-gray-400 py-10">Загружаем кафе...</div>

    <template v-else>
      <!-- Список залов (горизонтальная лента) -->
      <div class="flex gap-3 overflow-x-auto no-scrollbar -mx-4 px-4">
        <button
          v-for="hall in halls"
          :key="hall.id"
          @click="selectHall(hall.id)"
          class="press-scale shrink-0 px-4 py-2 rounded-xl text-sm font-medium transition-colors"
          :class="selectedHallId === hall.id ? 'bg-cafe-espresso text-white' : 'bg-white text-gray-600 shadow-sm'"
        >
          {{ hall.name }}
        </button>
      </div>

      <!-- Столы выбранного зала -->
      <section>
        <h2 class="text-base font-semibold text-cafe-espresso mb-3">Столы</h2>
        <div class="space-y-3">
          <div
            v-for="t in tables"
            :key="t.id"
            class="list-item-enter bg-white rounded-2xl shadow-sm p-4 flex items-center justify-between"
          >
            <div>
              <p class="font-semibold text-cafe-espresso">Стол №{{ t.tableNumber }}</p>
              <p class="text-xs text-gray-500 mt-0.5">
                👥 до {{ t.capacity }} мест{{ t.location ? ' · ' + t.location : '' }}
              </p>
            </div>
            <button
              @click="router.push({ path: '/booking', query: { tableId: t.id } })"
              class="press-scale text-xs font-semibold text-cafe-caramel bg-cafe-cream px-4 py-2 rounded-xl"
            >
              Забронировать
            </button>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>