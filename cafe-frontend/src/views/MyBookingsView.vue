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
    error.value = e instanceof Error ? e.message : 'Failed to load bookings'
  } finally {
    loading.value = false
  }
}

async function cancel(id: number) {
  try {
    await api.cancelBooking(id)
    await load()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to cancel'
  }
}

// Generate QR code via the free qrserver.com API using the backend's qrCode
// (On defense you could say: "QR can be generated on the backend
// with the ZXing library — more visual; here we use an external service for simplicity".)
function qrUrl(code: string): string {
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(code)}`
}

// Human-readable date format: 22.10 19:00
function fmt(dt: string): string {
  return new Date(dt).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// Status colour
const statusColor: Record<string, string> = {
  NEW: 'bg-amber-100 text-amber-700',
  CONFIRMED: 'bg-emerald-100 text-emerald-700',
  COMPLETED: 'bg-blue-100 text-blue-700',
  CANCELLED: 'bg-red-100 text-red-500',
  NO_SHOW: 'bg-gray-200 text-gray-500',
}

const statusLabel: Record<string, string> = {
  NEW: 'New',
  CONFIRMED: 'Confirmed',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_SHOW: 'No-show',
}

const active = computed(() => bookings.value.filter(b => b.status === 'NEW' || b.status === 'CONFIRMED'))
const history = computed(() => bookings.value.filter(b => !['NEW', 'CONFIRMED'].includes(b.status)))

onMounted(load)
</script>

<template>
  <div class="px-4 pt-6 pb-6 space-y-5">
    <h1 class="text-2xl font-bold text-cafe-espresso">My bookings</h1>

    <p v-if="loading" class="text-center text-gray-400 py-10">Loading...</p>
    <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

    <template v-if="!loading">
      <!-- Active bookings -->
      <section v-if="active.length">
        <h2 class="text-sm font-semibold text-gray-500 mb-2">Active</h2>
        <div v-for="b in active" :key="b.id" class="list-item-enter bg-white rounded-2xl shadow-sm p-4 space-y-2">
          <div class="flex items-center justify-between">
            <p class="font-semibold text-cafe-espresso">Table №{{ b.tableNumber }} · {{ b.hallName }}</p>
            <span class="text-xs font-medium px-2 py-1 rounded-full" :class="statusColor[b.status]">{{ statusLabel[b.status] }}</span>
          </div>
          <p class="text-sm text-gray-600">📅 {{ fmt(b.startTime) }} – {{ fmt(b.endTime) }}</p>
          <p class="text-xs text-gray-500">👥 {{ b.guestsCount }} guest</p>

          <div class="flex gap-2 pt-1">
            <button @click="showQrFor = showQrFor === b.id ? null : b.id" class="press-scale flex-1 py-2 rounded-xl bg-cafe-cream text-cafe-espresso text-sm font-medium">
              {{ showQrFor === b.id ? 'Hide QR' : 'Show QR' }}
            </button>
            <button @click="cancel(b.id)" class="press-scale px-4 py-2 rounded-xl bg-red-50 text-red-500 text-sm font-medium">
              Cancel
            </button>
          </div>

          <!-- QR code -->
          <div v-if="showQrFor === b.id" class="flex flex-col items-center pt-2">
            <img :src="qrUrl(b.qrCode)" alt="QR" class="w-40 h-40 rounded-xl" />
            <p class="text-[10px] text-gray-400 mt-1">Show this QR code at the entrance</p>
          </div>
        </div>
      </section>

      <!-- History -->
      <section v-if="history.length">
        <h2 class="text-sm font-semibold text-gray-500 mb-2">History</h2>
        <div v-for="b in history" :key="b.id" class="bg-white/60 rounded-2xl p-4 space-y-1 opacity-80">
          <div class="flex items-center justify-between">
            <p class="font-semibold text-cafe-espresso">Table №{{ b.tableNumber }}</p>
            <span class="text-xs font-medium px-2 py-1 rounded-full" :class="statusColor[b.status]">{{ statusLabel[b.status] }}</span>
          </div>
          <p class="text-xs text-gray-500">📅 {{ fmt(b.startTime) }}</p>
        </div>
      </section>

      <p v-if="!active.length && !history.length" class="text-center text-gray-400 py-10">No bookings yet. Book a table!</p>
    </template>
  </div>
</template>