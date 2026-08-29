<script setup>
import { onMounted, ref } from 'vue'
import { avatarUrl, getUser } from '../api/users'
import { DEFAULT_AVATAR } from '../lib/defaultAvatar'

const props = defineProps({
  userId: { type: Number, required: true },
})

const src = ref(avatarUrl(props.userId))
const label = ref(`User #${props.userId}`)

function onError() {
  src.value = DEFAULT_AVATAR
}

onMounted(async () => {
  try {
    const user = await getUser(props.userId)
    label.value = user.username
  } catch {}
})
</script>

<template>
  <router-link :to="`/users/${userId}`" class="post-author">
    <img :src="src" class="avatar-small" @error="onError" />
    {{ label }}
  </router-link>
</template>
