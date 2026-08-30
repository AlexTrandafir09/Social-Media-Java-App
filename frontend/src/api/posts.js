import { apiFetch } from './client'

export function getPosts(page = 0, size = 10, authorIds = null) {
  let query = `/api/posts?page=${page}&size=${size}&sort=createdAt,desc`
  if (authorIds && authorIds.length > 0) {
    query += `&authorIds=${authorIds.join(',')}`
  }
  return apiFetch(query)
}

export function getPost(id) {
  return apiFetch(`/api/posts/${id}`)
}

export function getPostsByAuthor(authorId) {
  return apiFetch(`/api/posts/author/${authorId}`)
}

export function createPost(content, images) {
  return apiFetch('/api/posts', { method: 'POST', body: { content, images } })
}

export function imageUrl(imageId) {
  return `http://localhost:8090/api/posts/images/${imageId}/file`
}

export function updatePost(id, content, keepImageIds, newImages) {
  return apiFetch(`/api/posts/${id}`, { method: 'PUT', body: { content, keepImageIds, newImages } })
}

export function deletePost(id) {
  return apiFetch(`/api/posts/${id}`, { method: 'DELETE' })
}
