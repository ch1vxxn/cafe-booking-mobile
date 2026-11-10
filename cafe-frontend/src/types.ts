/**
 * Типы — «контракт» между фронтом и бэкендом.
 * Эти типы повторяют DTO из Java (см. dto/ на бэкенде).
 * Если бэкенд вернёт не такие поля — TS подсветит ошибку.
 */

export type Role = 'GUEST' | 'ADMIN'

export type BookingStatus =
  | 'NEW'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW'

export interface Hall {
  id: number
  name: string
  description: string | null
  imageUrl: string | null
}

export interface RestaurantTable {
  id: number
  tableNumber: number
  capacity: number
  location: string | null
  imageUrl: string | null
  hall: Hall | null
}

export interface UserDto {
  id: number
  firstName: string
  lastName: string
  phone: string
  role: Role
  bonusPoints: number
}

export interface AuthResponse {
  token: string
  user: UserDto
}

export interface BookingDto {
  id: number
  userId: number
  userName: string
  userPhone: string
  tableId: number
  tableNumber: number
  hallName: string | null
  startTime: string
  endTime: string
  guestsCount: number
  comment: string | null
  status: BookingStatus
  qrCode: string
}