/**
 * api.ts — ЕДИНАЯ точка обращения фронта к бэкенду.
 *
 * Как это работает (твой вопрос «как соединить API с бэкендом и фронтом»):
 *
 *  1) Фронт (Vite, порт 5173) делает fetch('/api/halls').
 *  2) Vite-прокси (vite.config.ts) САМ пересылает запрос на бэкенд
 *     (порт 8080). Браузер даже не знает про 8080 — всё выглядит
 *     как «свой» сервер. CORS не мешает, потому что для браузера
 *     это один и тот же адрес.
 *  3) Бэкенд отвечает JSON. Мы возвращаем его как есть.
 *
 * Плюс: если пользователь залогинен (в localStorage есть токен),
 * мы добавляем заголовок Authorization: Bearer <токен> к каждому
 * запросу. Так бэкенд понимает, кто мы (см. JwtAuthFilter на Java).
 */

const TOKEN_KEY = 'cafe_token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

/** Базовый fetch с токеном и разбором ошибок. */
async function request<T>(method: string, url: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  })

  // Если сервер вернул ошибку — вытаскиваем { error: "..." } и кидаем её
  if (!res.ok) {
    let message = `Ошибка ${res.status}`
    try {
      const data = await res.json()
      if (data && data.error) message = data.error
    } catch {
      /* тело не JSON — оставляем общее сообщение */
    }
    throw new Error(message)
  }

  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

// ---------- Публичные эндпоинты ----------
export const api = {
  // Зарегистрироваться
  register: (data: { firstName: string; lastName: string; phone: string; password: string }) =>
    request<import('./types').AuthResponse>('POST', '/api/auth/register', data),

  // Войти
  login: (phone: string, password: string) =>
    request<import('./types').AuthResponse>('POST', '/api/auth/login', { phone, password }),

  // Список залов
  halls: () => request<import('./types').Hall[]>('GET', '/api/halls'),

  // Список столов (опц. фильтр по залу)
  tables: (hallId?: number) =>
    request<import('./types').RestaurantTable[]>(
      'GET', hallId ? `/api/tables?hallId=${hallId}` : '/api/tables'),

  // Свободные столы на интервал
  availableTables: (date: string, start: string, end: string, guests: number) =>
    request<import('./types').RestaurantTable[]>(
      'GET', `/api/tables/available?date=${date}&start=${start}&end=${end}&guests=${guests}`),

  // Мои брони
  myBookings: () => request<import('./types').BookingDto[]>('GET', '/api/bookings/my'),

  // Создать бронь
  createBooking: (data: {
    tableId: number
    startTime: string // ISO: 2026-10-22T19:00:00
    endTime: string
    guestsCount: number
    comment?: string
  }) => request<import('./types').BookingDto>('POST', '/api/bookings', data),

  // Отменить бронь
  cancelBooking: (id: number) =>
    request<{ ok: boolean }>('DELETE', `/api/bookings/${id}`),

  // --- Админ ---
  adminBookings: () => request<import('./types').BookingDto[]>('GET', '/api/admin/bookings'),
  changeStatus: (id: number, status: string) =>
    request<import('./types').BookingDto>(
      'PATCH', `/api/admin/bookings/${id}/status`, { status }),
}