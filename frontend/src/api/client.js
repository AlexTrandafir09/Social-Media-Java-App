const BASE_URL = 'http://localhost:8090'

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

function readToken() {
  return localStorage.getItem('accessToken')
}

async function parseErrorMessage(response) {
  let body
  try {
    body = await response.json()
  } catch {
    return response.statusText || `Request failed with status ${response.status}`
  }
  if (body.message) {
    return body.message
  }
  const fieldErrors = Object.entries(body).map(([field, msg]) => `${field}: ${msg}`)
  if (fieldErrors.length > 0) {
    return fieldErrors.join(', ')
  }
  return response.statusText || `Request failed with status ${response.status}`
}

export async function apiFetch(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = readToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    credentials: 'include',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(await parseErrorMessage(response), response.status)
  }
  if (response.status === 204) {
    return null
  }
  return response.json()
}
