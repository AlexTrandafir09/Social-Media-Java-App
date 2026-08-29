import { apiFetch } from './client'

export function getActivity(page = 0) {
  return apiFetch(`/api/activity?page=${page}&size=20&sort=createdAt,desc`)
}
