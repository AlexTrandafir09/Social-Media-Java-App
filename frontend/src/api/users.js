import { apiFetch } from './client'

export function getUser(id) {
  return apiFetch(`/api/users/${id}`)
}

export function getAllUsers() {
  return apiFetch('/api/users')
}

export function updateUser(id, bio) {
  return apiFetch(`/api/users/${id}`, { method: 'PUT', body: { bio } })
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

export function updateAvatar(id, contentType, data) {
  return apiFetch(`/api/users/${id}/avatar`, { method: 'PUT', body: { contentType, data } })
}

export function avatarUrl(id) {
  return `http://localhost:8090/api/users/${id}/avatar/file`
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
