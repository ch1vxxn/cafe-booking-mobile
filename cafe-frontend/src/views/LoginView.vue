<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const isRegister = ref(false)
const firstName = ref('')
const lastName = ref('')
const phone = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    if (isRegister.value) {
      await auth.register({ firstName: firstName.value, lastName: lastName.value, phone: phone.value, password: password.value })
    } else {
      await auth.login(phone.value, password.value)
    }
    // После входа — на страницу, откуда пришли (или на главную)
    const redirect = route.query.redirect as string | undefined
    router.push(redirect ?? '/')
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Не удалось войти'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-full flex flex-col justify-center px-6 py-10">
    <div class="text-center mb-8">
      <div class="text-5xl mb-3">☕</div>
      <h1 class="text-2xl font-bold text-cafe-espresso">Cafe Booking</h1>
      <p class="text-gray-500 text-sm mt-1">Бронируй столы в любимом кафе</p>
    </div>

    <div class="bg-white rounded-2xl shadow-sm p-5 space-y-3">
      <!-- Переключатель: Вход / Регистрация -->
      <div class="flex bg-cafe-cream rounded-xl p-1 text-sm font-medium mb-2">
        <button class="flex-1 py-2 rounded-lg transition-colors" :class="!isRegister ? 'bg-white shadow-sm' : 'text-gray-500'" @click="isRegister = false">Вход</button>
        <button class="flex-1 py-2 rounded-lg transition-colors" :class="isRegister ? 'bg-white shadow-sm' : 'text-gray-500'" @click="isRegister = true">Регистрация</button>
      </div>

      <template v-if="isRegister">
        <input v-model="firstName" placeholder="Имя" class="w-full px-4 py-3 bg-cafe-cream rounded-xl outline-none focus:ring-2 ring-cafe-caramel/50" />
        <input v-model="lastName" placeholder="Фамилия" class="w-full px-4 py-3 bg-cafe-cream rounded-xl outline-none focus:ring-2 ring-cafe-caramel/50" />
      </template>

      <input v-model="phone" type="tel" placeholder="Телефон (+375...)" class="w-full px-4 py-3 bg-cafe-cream rounded-xl outline-none focus:ring-2 ring-cafe-caramel/50" />
      <input v-model="password" type="password" placeholder="Пароль (мин. 4 символа)" class="w-full px-4 py-3 bg-cafe-cream rounded-xl outline-none focus:ring-2 ring-cafe-caramel/50" />

      <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>

      <button @click="submit" :disabled="loading" class="press-scale w-full py-3.5 rounded-xl bg-cafe-caramel text-white font-semibold disabled:opacity-50">
        {{ loading ? 'Подождите...' : isRegister ? 'Создать аккаунт' : 'Войти' }}
      </button>
    </div>

    <p class="text-center text-xs text-gray-400 mt-6">Первый зарегистрированный — администратор</p>
  </div>
</template>