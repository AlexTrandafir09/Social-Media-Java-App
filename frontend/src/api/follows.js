import { apiFetch } from './client'

export function getFollowing(userId) {
  return apiFetch(`/api/follows/following/${userId}`)
}

export function getFollowers(userId) {
  return apiFetch(`/api/follows/followers/${userId}`)
}

export function follow(followingId) {
  return apiFetch('/api/follows', { method: 'POST', body: { followingId } })
}

export function unfollow(followingId) {
  return apiFetch(`/api/follows?followingId=${followingId}`, { method: 'DELETE' })
}
