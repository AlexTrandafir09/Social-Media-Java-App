<script setup>
import { useRouter } from 'vue-router'
import { authState, clearSession } from '../stores/auth'
import { logout } from '../api/auth'

const router = useRouter()

async function onLogout() {
  try {
    await logout()
  } catch {
    // Even if the server call fails, still clear the local session.
  }
  clearSession()
  router.push({ name: 'login' })
}
</script>

<template>
  <nav class="app-nav">
    <router-link to="/" class="brand">Ripple</router-link>
    <div class="app-nav-links">
      <template v-if="authState.user">
        <router-link to="/users">Users</router-link>
        <router-link to="/notifications">Notifications</router-link>
        <router-link v-if="authState.user.role === 'ADMIN'" to="/admin/activity">Activity log</router-link>
        <router-link :to="`/users/${authState.user.id}`">{{ authState.user.username }}</router-link>
        <router-link to="/settings">Settings</router-link>
        <button @click="onLogout">Log out</button>
      </template>
      <template v-else>
        <router-link to="/login">Log in</router-link>
        <router-link to="/register">Register</router-link>
      </template>
    </div>
  </nav>
</template>
