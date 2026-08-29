import { apiFetch } from './client'

export function getComments(postId, page = 0) {
  return apiFetch(`/api/comments/post/${postId}?page=${page}&size=20&sort=createdAt,asc`)
}

export function createComment(postId, content) {
  return apiFetch('/api/comments', { method: 'POST', body: { postId, content } })
}

export function updateComment(id, content) {
  return apiFetch(`/api/comments/${id}`, { method: 'PUT', body: { content } })
}

export function deleteComment(id) {
  return apiFetch(`/api/comments/${id}`, { method: 'DELETE' })
}
