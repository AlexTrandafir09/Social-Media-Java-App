<script setup>
import { onMounted, ref } from 'vue'
import { getPosts } from '../api/posts'
import { getFollowing } from '../api/follows'
import { authState } from '../stores/auth'
import PostCard from '../components/PostCard.vue'

const posts = ref([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(true)
const apiError = ref('')
const followingIds = ref([])

async function resolveFeedAuthors() {
  const ownId = authState.user.id
  const following = await getFollowing(ownId)
  followingIds.value = following.map((f) => f.followingId)
  return [ownId, ...followingIds.value]
}

async function loadPage(p) {
  loading.value = true
  apiError.value = ''
  try {
    const authorIds = await resolveFeedAuthors()
    const result = await getPosts(p, 10, authorIds)
    posts.value = result.content
    page.value = result.number
    totalPages.value = result.totalPages
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage(0))

function onPostDeleted(postId) {
  posts.value = posts.value.filter((p) => p.id !== postId)
}
</script>

<template>
  <div class="feed">
    <div class="feed-header">
      <h1>Feed</h1>
      <router-link to="/posts/new" class="button-link">New post</router-link>
    </div>

    <p v-if="apiError" class="error-banner">{{ apiError }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="posts.length === 0 && followingIds.length === 0">
      You're not following anyone yet. Posts from people you follow (and your own posts) will show up here.
    </p>
    <p v-else-if="posts.length === 0">No posts yet from you or the people you follow.</p>

    <PostCard v-for="post in posts" :key="post.id" :post="post" @deleted="onPostDeleted" />

    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="page === 0" @click="loadPage(page - 1)">Previous</button>
      <span>Page {{ page + 1 }} of {{ totalPages }}</span>
      <button :disabled="page + 1 >= totalPages" @click="loadPage(page + 1)">Next</button>
    </div>
  </div>
</template>
