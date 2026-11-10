<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'
import type { RestaurantTable } from '../types'

const route = useRoute()
const router = useRouter()

const preselectTableId = route.query.tableId ? Number(route.query.tableId) : null

// Формируем «завтра» как значение по умолчанию для даты
function tomorrow(): string {
  const d = new Date()
  d.setDate(d.getDate() + 1)
  return d.toISOString().slice(0, 10) // '2026-10-22'
}

const date = ref(tomorrow())
const start = ref('19:00')
const end = ref('21:00')
const guests = ref(2)
const selectedTableId = ref<number | null>(preselectTableId)
const tables = ref<RestaurantTable[]>([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const comment = ref('')

async function search() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    tables.value = await api.availableTables(date.value, start.value, end.value, guests.value)
    // если выбранный стол вдруг занят — сбрасываем выбор
    if (selectedTableId.value && !tables.value.some(t => t.id === selectedTableId.value)) {
      selectedTableId.value = null
    }
    if (tables.value.length === 0) {
      error.value = 'На это время свободных столов нет 😔'
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Ошибка поиска'
  } finally {
    loading.value = false
  }
}

async function book() {
  if (!selectedTableId.value) {
    error.value = 'Выберите стол'
    return
  }
  loading.value = true
  error.value = ''
  try {
    // ISO: 2026-10-22T19:00:00 — как ждёт бэкенд
    const startTime = `${date.value}T${start.value}:00`
    const endTime = `${date.value}T${end.value}:00`
    await api.createBooking({
      tableId: selectedTableId.value,
      startTime,
      endTime,
      guestsCount: guests.value,
      comment: comment.value || undefined,
    })
    success.value = '✅ Стол забронирован! Смотрите «Мои брони».'
    await search() // обновляем список свободных столов
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось забронировать'
  } finally {
    loading.value = false
  }
}

onMounted(search)
</script>

<template>
  <div class="px-4 pt-6 pb-6 space-y-5">
    <h1 class="text-2xl font-bold text-cafe-espresso">Бронирование</h1>

    <!-- Форма: дата, время, гости -->
    <div class="bg-white rounded-2xl shadow-sm p-4 space-y-3">
      <div class="grid grid-cols-2 gap-3">
        <label class="text-xs text-gray-500 font-medium">
          Дата
          <input v-model="date" type="date" class="mt-1 w-full px-1 py-2.5 bg-cafe-cream rounded-xl outline-none text-sm" />
        </label>
        <label class="text-xs text-gray-500 font-medium">
          Гости
          <input v-model.number="guests" type="number" min="1" max="12" class="mt-1 w-full px-1 text-center py-3 bg-cafe-cream rounded-xl outline-none text-sm" />
        </label>
        <label class="text-xs text-gray-500 font-medium">
          Время начала
          <input v-model="start" type="time" class="mt-1 w-full px-1 py-2.5 bg-cafe-cream rounded-xl outline-none text-sm" />
        </label>
        <label class="text-xs text-gray-500 font-medium">
          Время конца
          <input v-model="end" type="time" class="mt-1 w-full px-1 py-2.5 bg-cafe-cream rounded-xl outline-none text-sm" />
        </label>
      </div>

      <button @click="search" :disabled="loading" class="press-scale w-full py-3 rounded-xl bg-cafe-caramel text-white font-semibold disabled:opacity-50">
        {{ loading ? 'Поиск...' : 'Найти свободные столы' }}
      </button>
    </div>

    <!-- Ошибки/успех -->
    <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
    <p v-if="success" class="text-emerald-600 text-sm font-medium">{{ success }}</p>

    <!-- Свободные столы -->
    <section v-if="tables.length > 0">
      <h2 class="text-base font-semibold text-cafe-espresso mb-3">Свободные столы</h2>
      <div class="space-y-3">
        <button
          v-for="t in tables"
          :key="t.id"
          @click="selectedTableId = t.id"
          class="list-item-enter w-full bg-white rounded-2xl shadow-sm p-4 flex items-center justify-between transition-all"
          :class="selectedTableId === t.id ? 'ring-2 ring-cafe-caramel' : ''"
        >
          <div class="text-left">
            <p class="font-semibold text-cafe-espresso">Стол №{{ t.tableNumber }} · {{ t.hall?.name }}</p>
            <p class="text-xs text-gray-500 mt-0.5">👥 до {{ t.capacity }} мест{{ t.location ? ' · ' + t.location : '' }}</p>
          </div>
          <span class="text-xs font-semibold" :class="selectedTableId === t.id ? 'text-cafe-caramel' : 'text-gray-400'">
            {{ selectedTableId === t.id ? '✓ Выбран' : 'Выбрать' }}
          </span>
        </button>
      </div>

      <!-- Комментарий и кнопка брони -->
      <div class="mt-4 bg-white rounded-2xl shadow-sm p-4 space-y-3">
        <input v-model="comment" placeholder="Комментарий (необязательно)" class="w-full px-4 py-3 bg-cafe-cream rounded-xl outline-none text-sm" />
        <button @click="book" :disabled="loading || !selectedTableId" class="press-scale w-full py-3.5 rounded-xl bg-cafe-espresso text-white font-semibold disabled:opacity-50">
          {{ loading ? 'Бронируем...' : 'Забронировать' }}
        </button>
      </div>
    </section>
  </div>
</template>