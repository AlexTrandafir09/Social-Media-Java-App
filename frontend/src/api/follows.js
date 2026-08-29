import { apiFetch } from './client'

// Returns the list of Follow rows for people this user follows.
export function getFollowing(userId) {
  return apiFetch(`/api/follows/following/${userId}`)
}

// Returns the list of Follow rows for people who follow this user.
export function getFollowers(userId) {
  return apiFetch(`/api/follows/followers/${userId}`)
}

export function follow(followingId) {
  return apiFetch('/api/follows', { method: 'POST', body: { followingId } })
}

export function unfollow(followingId) {
  return apiFetch(`/api/follows?followingId=${followingId}`, { method: 'DELETE' })
}
