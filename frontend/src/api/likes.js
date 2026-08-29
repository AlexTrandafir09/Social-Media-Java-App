import { apiFetch } from './client'

export function likePost(postId) {
  return apiFetch('/api/likes', { method: 'POST', body: { postId } })
}

export function unlikePost(postId) {
  return apiFetch(`/api/likes?postId=${postId}`, { method: 'DELETE' })
}

export function getLikesForPost(postId) {
  return apiFetch(`/api/likes/post/${postId}`)
}

export function countLikes(postId) {
  return apiFetch(`/api/likes/post/${postId}/count`)
}
