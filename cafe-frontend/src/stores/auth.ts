import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api, getToken, setToken } from '../api'
import type { UserDto } from '../types'

/**
 * auth — глобальное хранилище «кто я».
 *
 * Pinia — это «общий кошелёк» для всех экранов:
 *  - после логина кладём сюда токен и пользователя;
 *  - любой компонент может спросить: auth.user, auth.isAdmin.
 *
 * Данные дублируются в localStorage (через api.setToken),
 * чтобы при перезагрузке страницы не терять вход.
 */
export const useAuthStore = defineStore('auth', () => {
  // Начальное состояние: токен из localStorage (если был)
  const token = ref<string | null>(getToken())
  const user = ref<UserDto | null>(null)

  // Это удобные «производные» значения
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(phone: string, password: string) {
    const res = await api.login(phone, password)
    token.value = res.token
    user.value = res.user
    setToken(res.token)
  }

  async function register(data: { firstName: string; lastName: string; phone: string; password: string }) {
    const res = await api.register(data)
    token.value = res.token
    user.value = res.user
    setToken(res.token)
  }

  function logout() {
    token.value = null
    user.value = null
    setToken(null)
  }

  return { token, user, isLoggedIn, isAdmin, login, register, logout }
})