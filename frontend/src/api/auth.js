import { apiFetch } from './client'

export function register(username, email, password) {
  return apiFetch('/api/auth/register', {
    method: 'POST',
    auth: false,
    body: { username, email, password },
  })
}

export function login(username, password) {
  return apiFetch('/api/auth/login', {
    method: 'POST',
    auth: false,
    body: { username, password },
  })
}

export function logout() {
  return apiFetch('/api/auth/logout', { method: 'POST' })
}

// The access token is a JWT: header.payload.signature, base64url-encoded.
// Decoding it client-side (no verification - the backend already verified it
// when it issued it) is enough to read the user id/username/role for the UI.
export function decodeToken(token) {
  const payload = token.split('.')[1]
  const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
  const json = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
      .join(''),
  )
  return JSON.parse(json)
}
