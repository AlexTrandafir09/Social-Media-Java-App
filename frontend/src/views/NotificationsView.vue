<script setup>
import { onMounted, ref } from 'vue'
import { getNotifications, markRead, deleteNotification } from '../api/notifications'
import { authState } from '../stores/auth'

const notifications = ref([])
const apiError = ref('')
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    notifications.value = await getNotifications(authState.user.id)
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function onMarkRead(id) {
  try {
    await markRead(id)
    const n = notifications.value.find((x) => x.id === id)
    if (n) n.read = true
  } catch (err) {
    apiError.value = err.message
  }
}

async function onDelete(id) {
  try {
    await deleteNotification(id)
    notifications.value = notifications.value.filter((n) => n.id !== id)
  } catch (err) {
    apiError.value = err.message
  }
}

function describe(n) {
  const verb = { LIKE: 'liked', COMMENT: 'commented on', FOLLOW: 'followed' }[n.type] || n.type
  return n.type === 'FOLLOW' ? `User #${n.actorId} followed you` : `User #${n.actorId} ${verb} your post #${n.referencePostId}`
}
</script>

<template>
  <h1>Notifications</h1>
  <p v-if="apiError" class="error-banner">{{ apiError }}</p>
  <p v-else-if="loading">Loading...</p>
  <p v-else-if="notifications.length === 0">No notifications yet.</p>
  <ul v-else class="notification-list">
    <li v-for="n in notifications" :key="n.id" :class="{ unread: !n.read }">
      <span>{{ describe(n) }}</span>
      <span class="post-meta">{{ new Date(n.createdAt).toLocaleString() }}</span>
      <div class="post-actions">
        <button v-if="!n.read" @click="onMarkRead(n.id)">Mark read</button>
        <button @click="onDelete(n.id)">Delete</button>
      </div>
    </li>
  </ul>
</template>
