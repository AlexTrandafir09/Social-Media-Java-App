<script setup>
import { ref, watch } from 'vue'
import { searchUsers, avatarUrl } from '../api/users'
import { DEFAULT_AVATAR } from '../lib/defaultAvatar'
import { authState } from '../stores/auth'

const query = ref('')
const users = ref([])
const apiError = ref('')
const loading = ref(false)
let debounceTimer = null

watch(query, (value) => {
  clearTimeout(debounceTimer)
  const trimmed = value.trim()
  if (!trimmed) {
    users.value = []
    loading.value = false
    return
  }
  loading.value = true
  debounceTimer = setTimeout(async () => {
    apiError.value = ''
    try {
      const results = await searchUsers(trimmed)
      users.value = results.filter((u) => u.id !== authState.user.id)
    } catch (err) {
      apiError.value = err.message
    } finally {
      loading.value = false
    }
  }, 300)
})

function onAvatarError(event) {
  event.target.src = DEFAULT_AVATAR
}
</script>

<template>
  <h1>Users</h1>
  <input v-model="query" type="text" class="search-bar" placeholder="Search users..." />

  <p v-if="apiError" class="error-banner">{{ apiError }}</p>
  <p v-else-if="loading">Searching...</p>
  <p v-else-if="query.trim() && users.length === 0">No users found.</p>

  <ul class="user-list">
    <li v-for="user in users" :key="user.id">
      <router-link :to="`/users/${user.id}`" class="post-author">
        <img :src="avatarUrl(user.id)" class="avatar-small" @error="onAvatarError" />
        {{ user.username }}
      </router-link>
    </li>
  </ul>
</template>
