import { apiFetch } from './client'

export function getUser(id) {
  return apiFetch(`/api/users/${id}`)
}

export function getAllUsers() {
  return apiFetch('/api/users')
}

export function updateUser(id, bio, avatarUrl) {
  return apiFetch(`/api/users/${id}`, { method: 'PUT', body: { bio, avatarUrl } })
}

export function changeEmail(id, newEmail) {
  return apiFetch(`/api/users/${id}/email`, { method: 'PATCH', body: { newEmail } })
}

export function changePassword(id, currentPassword, newPassword) {
  return apiFetch(`/api/users/${id}/password`, {
    method: 'PATCH',
    body: { currentPassword, newPassword },
  })
}

export function deleteUser(id) {
  return apiFetch(`/api/users/${id}`, { method: 'DELETE' })
}

export function getPreferences(userId) {
  return apiFetch(`/api/users/${userId}/preferences`)
}

export function updatePreferences(userId, notifyOnLike, notifyOnComment, notifyOnFollow) {
  return apiFetch(`/api/users/${userId}/preferences`, {
    method: 'PUT',
    body: { notifyOnLike, notifyOnComment, notifyOnFollow },
  })
}
