import { apiFetch } from './client'

export function getNotifications(userId) {
  return apiFetch(`/api/notifications/user/${userId}`)
}

export function markRead(id) {
  return apiFetch(`/api/notifications/${id}/read`, { method: 'PATCH' })
}

export function deleteNotification(id) {
  return apiFetch(`/api/notifications/${id}`, { method: 'DELETE' })
}
