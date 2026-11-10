<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { api } from '../api'
import type { BookingDto } from '../types'

const bookings = ref<BookingDto[]>([])
const loading = ref(true)
const error = ref('')
const showQrFor = ref<number | null>(null)

async function load() {
  loading.value = true
  try {
    bookings.value = await api.myBookings()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось загрузить брони'
  } finally {
    loading.value = false
  }
}

async function cancel(id: number) {
  try {
    await api.cancelBooking(id)
    await load()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось отменить'
  }
}

// QR-код генерируем через бесплатный API qrserver.com по qrCode из бэкенда.
// (На защите можно сказать: «QR можно генерировать и на бэкенде
//  библиотекой ZXing — нагляднее; здесь для простоты внешний сервис».)
function qrUrl(code: string): string {
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(code)}`
}

// Читаемый формат даты: 22.10 в 19:00
function fmt(dt: string): string {
  return new Date(dt).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// Цвет статуса
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

const active = computed(() => bookings.value.filter(b => b.status === 'NEW' || b.status === 'CONFIRMED'))
const history = computed(() => bookings.value.filter(b => !['NEW', 'CONFIRMED'].includes(b.status)))

onMounted(load)
</script>

<template>
  <div class="px-4 pt-6 pb-6 space-y-5">
    <h1 class="text-2xl font-bold text-cafe-espresso">Мои брони</h1>

    <p v-if="loading" class="text-center text-gray-400 py-10">Загружаем...</p>
    <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

    <template v-if="!loading">
      <!-- Активные брони -->
      <section v-if="active.length">
        <h2 class="text-sm font-semibold text-gray-500 mb-2">Активные</h2>
        <div v-for="b in active" :key="b.id" class="list-item-enter bg-white rounded-2xl shadow-sm p-4 space-y-2">
          <div class="flex items-center justify-between">
            <p class="font-semibold text-cafe-espresso">Стол №{{ b.tableNumber }} · {{ b.hallName }}</p>
            <span class="text-xs font-medium px-2 py-1 rounded-full" :class="statusColor[b.status]">{{ statusLabel[b.status] }}</span>
          </div>
          <p class="text-sm text-gray-600">📅 {{ fmt(b.startTime) }} — {{ fmt(b.endTime) }}</p>
          <p class="text-xs text-gray-500">👥 {{ b.guestsCount }} гостя</p>

          <div class="flex gap-2 pt-1">
            <button @click="showQrFor = showQrFor === b.id ? null : b.id" class="press-scale flex-1 py-2 rounded-xl bg-cafe-cream text-cafe-espresso text-sm font-medium">
              {{ showQrFor === b.id ? 'Скрыть QR' : 'Показать QR' }}
            </button>
            <button @click="cancel(b.id)" class="press-scale px-4 py-2 rounded-xl bg-red-50 text-red-500 text-sm font-medium">
              Отменить
            </button>
          </div>

          <!-- QR-код -->
          <div v-if="showQrFor === b.id" class="flex flex-col items-center pt-2">
            <img :src="qrUrl(b.qrCode)" alt="QR" class="w-40 h-40 rounded-xl" />
            <p class="text-[10px] text-gray-400 mt-1">Покажите этот QR на входе</p>
          </div>
        </div>
      </section>

      <!-- История -->
      <section v-if="history.length">
        <h2 class="text-sm font-semibold text-gray-500 mb-2">История</h2>
        <div v-for="b in history" :key="b.id" class="bg-white/60 rounded-2xl p-4 space-y-1 opacity-80">
          <div class="flex items-center justify-between">
            <p class="font-semibold text-cafe-espresso">Стол №{{ b.tableNumber }}</p>
            <span class="text-xs font-medium px-2 py-1 rounded-full" :class="statusColor[b.status]">{{ statusLabel[b.status] }}</span>
          </div>
          <p class="text-xs text-gray-500">📅 {{ fmt(b.startTime) }}</p>
        </div>
      </section>

      <p v-if="!active.length && !history.length" class="text-center text-gray-400 py-10">Пока нет броней. Забронируйте стол!</p>
    </template>
  </div>
</template>