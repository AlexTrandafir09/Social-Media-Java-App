import { reactive } from 'vue'
import { decodeToken } from '../api/auth'

function loadUser() {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  try {
    const claims = decodeToken(token)
    return { id: Number(claims.sub), username: claims.username, role: claims.role }
  } catch {
    return null
  }
}

export const authState = reactive({
  user: loadUser(),
})

export function setSession(accessToken) {
  localStorage.setItem('accessToken', accessToken)
  authState.user = loadUser()
}

export function clearSession() {
  localStorage.removeItem('accessToken')
  authState.user = null
}
