<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUser, avatarUrl } from '../api/users'
import { getPostsByAuthor } from '../api/posts'
import { getFollowing, follow, unfollow } from '../api/follows'
import { authState } from '../stores/auth'
import { DEFAULT_AVATAR } from '../lib/defaultAvatar'
import PostCard from '../components/PostCard.vue'

const route = useRoute()

const profileId = computed(() => Number(route.params.id))
const isOwnProfile = computed(() => profileId.value === authState.user.id)

const user = ref(null)
const posts = ref([])
const isFollowing = ref(false)
const apiError = ref('')
const loading = ref(true)
const avatarSrc = ref('')

function onAvatarLoadError() {
  avatarSrc.value = DEFAULT_AVATAR
}

async function load() {
  loading.value = true
  apiError.value = ''
  avatarSrc.value = avatarUrl(profileId.value)
  try {
    user.value = await getUser(profileId.value)
    posts.value = await getPostsByAuthor(profileId.value)

    if (!isOwnProfile.value) {
      const following = await getFollowing(authState.user.id)
      isFollowing.value = following.some((f) => f.followingId === profileId.value)
    }
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(profileId, load)

function onPostDeleted(postId) {
  posts.value = posts.value.filter((p) => p.id !== postId)
}

async function toggleFollow() {
  try {
    if (isFollowing.value) {
      await unfollow(profileId.value)
    } else {
      await follow(profileId.value)
    }
    isFollowing.value = !isFollowing.value
  } catch (err) {
    apiError.value = err.message
  }
}
</script>

<template>
  <div v-if="loading">Loading...</div>
  <p v-else-if="apiError" class="error-banner">{{ apiError }}</p>
  <div v-else-if="user">
    <img :src="avatarSrc" class="avatar" @error="onAvatarLoadError" />
    <h1>{{ user.username }}</h1>
    <p v-if="user.bio">{{ user.bio }}</p>

    <button v-if="!isOwnProfile" @click="toggleFollow">{{ isFollowing ? 'Unfollow' : 'Follow' }}</button>
    <router-link v-else to="/settings" class="button-link">Settings</router-link>

    <h2>Posts</h2>
    <PostCard v-for="post in posts" :key="post.id" :post="post" @deleted="onPostDeleted" />
  </div>
</template>
