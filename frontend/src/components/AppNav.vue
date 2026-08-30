<script setup>
import { useRouter } from 'vue-router'
import { authState, clearSession } from '../stores/auth'
import { logout } from '../api/auth'
import Icon from './Icon.vue'

const router = useRouter()

async function onLogout() {
  try {
    await logout()
  } catch {
  }
  clearSession()
  router.push({ name: 'login' })
}
</script>

<template>
  <nav class="app-nav">
    <router-link to="/" class="brand">AWBD Social</router-link>
    <div class="app-nav-links">
      <template v-if="authState.user">
        <router-link to="/posts/new" title="New post" aria-label="New post"><Icon name="plus" /></router-link>
        <router-link to="/users" title="Users" aria-label="Users"><Icon name="search" /></router-link>
        <router-link to="/notifications" title="Notifications" aria-label="Notifications"><Icon name="bell" /></router-link>
        <router-link v-if="authState.user.role === 'ADMIN'" to="/admin/activity" title="Activity log" aria-label="Activity log"><Icon name="list" /></router-link>
        <router-link :to="`/users/${authState.user.id}`" :title="authState.user.username" :aria-label="authState.user.username"><Icon name="user" /></router-link>
        <router-link to="/settings" title="Settings" aria-label="Settings"><Icon name="settings" /></router-link>
        <button @click="onLogout" title="Log out" aria-label="Log out"><Icon name="logout" /></button>
      </template>
    </div>
  </nav>
</template>
