<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api } from '../api'
import type { BookingDto } from '../types'

const bookings = ref<BookingDto[]>([])
const loading = ref(true)
const error = ref('')
const filter = ref('ALL')

async function load() {
  loading.value = true
  try {
    bookings.value = await api.adminBookings()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось загрузить брони'
  } finally {
    loading.value = false
  }
}

async function changeStatus(b: BookingDto, status: string) {
  try {
    await api.changeStatus(b.id, status)
    await load()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось обновить статус'
  }
}

// Фильтр на фронте (для скорости; на бэкенде фильтр по ?status= тоже есть)
const filtered = computed(() =>
  filter.value === 'ALL' ? bookings.value : bookings.value.filter(b => b.status === filter.value),
)

const statusColor: Record<string, string> = {
  NEW: 'bg-amber-100 text-amber-700',
  CONFIRMED: 'bg-emerald-100 text-emerald-700',
  COMPLETED: 'bg-blue-100 text-blue-700',
  CANCELLED: 'bg-red-100 text-red-500',
  NO_SHOW: 'bg-gray-200 text-gray-500',
}

const statusLabel: Record<string, string> = {
  NEW: 'Новая',
  CONFIRMED: 'Подтверждена',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
  NO_SHOW: 'Не пришёл',
}

// Кнопки действий — «кафеский путь»
const nextActions: Record<string, { label: string; status: string; color: string }[]> = {
  NEW: [
    { label: '✅ Подтвердить', status: 'CONFIRMED', color: 'bg-emerald-50 text-emerald-700' },
    { label: '❌ Отменить', status: 'CANCELLED', color: 'bg-red-50 text-red-500' },
  ],
  CONFIRMED: [
    { label: '🏁 Гость пришёл', status: 'COMPLETED', color: 'bg-blue-50 text-blue-700' },
    { label: '🚫 Не пришёл', status: 'NO_SHOW', color: 'bg-gray-100 text-gray-500' },
  ],
}

onMounted(load)
</script>

<template>
  <div class="px-4 pt-6 pb-6 space-y-4">
    <div class="flex items-center justify-between">
      <h1 class="text-2xl font-bold text-cafe-espresso">Админ</h1>
      <select v-model="filter" class="text-sm bg-white rounded-xl px-3 py-2 shadow-sm outline-none">
        <option value="ALL">Все статусы</option>
        <option value="NEW">Новые</option>
        <option value="CONFIRMED">Подтверждённые</option>
        <option value="COMPLETED">Завершённые</option>
        <option value="CANCELLED">Отменённые</option>
        <option value="NO_SHOW">Не пришли</option>
      </select>
    </div>

    <p v-if="loading" class="text-center text-gray-400 py-10">Загружаем...</p>
    <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

    <div v-if="!loading && filtered.length === 0" class="text-center text-gray-400 py-10">Броней нет</div>

    <div v-for="b in filtered" :key="b.id" class="list-item-enter bg-white rounded-2xl shadow-sm p-4 space-y-2">
      <div class="flex items-center justify-between">
        <p class="font-semibold text-cafe-espresso">Стол №{{ b.tableNumber }} · {{ b.hallName }}</p>
        <span class="text-xs font-medium px-2 py-1 rounded-full" :class="statusColor[b.status]">{{ statusLabel[b.status] }}</span>
      </div>

      <p class="text-sm text-gray-700">👤 {{ b.userName }} <span class="text-gray-400">· {{ b.userPhone }}</span></p>
      <p class="text-xs text-gray-500">📅 {{ new Date(b.startTime).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) }} — {{ new Date(b.endTime).toLocaleString('ru-RU', { hour: '2-digit', minute: '2-digit' }) }}</p>
      <p class="text-xs text-gray-500">👥 {{ b.guestsCount }} · QR: <code class="text-[10px]">{{ b.qrCode.slice(0, 12) }}…</code></p>

      <!-- Кнопки действия по статусу -->
      <div v-if="nextActions[b.status]" class="flex gap-2 pt-1">
        <button
          v-for="act in nextActions[b.status]"
          :key="act.status"
          @click="changeStatus(b, act.status)"
          class="press-scale flex-1 py-2 rounded-xl text-sm font-medium"
          :class="act.color"
        >
          {{ act.label }}
        </button>
      </div>
    </div>
  </div>
</template>