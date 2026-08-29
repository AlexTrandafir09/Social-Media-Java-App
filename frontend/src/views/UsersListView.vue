<script setup>
import { onMounted, ref } from 'vue'
import { getAllUsers } from '../api/users'

const users = ref([])
const apiError = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    users.value = await getAllUsers()
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <h1>Users</h1>
  <p v-if="apiError" class="error-banner">{{ apiError }}</p>
  <p v-else-if="loading">Loading...</p>
  <ul v-else class="user-list">
    <li v-for="user in users" :key="user.id">
      <router-link :to="`/users/${user.id}`">{{ user.username }}</router-link>
    </li>
  </ul>
</template>
