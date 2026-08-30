<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { getPosts } from '../api/posts'
import { getFollowing } from '../api/follows'
import { authState } from '../stores/auth'
import PostCard from '../components/PostCard.vue'

const posts = ref([])
const page = ref(0)
const hasMore = ref(true)
const loading = ref(true)
const loadingMore = ref(false)
const apiError = ref('')
const followingIds = ref([])
const bottomSentinel = ref(null)

let observer = null
let scrollTimer = null

async function resolveFeedAuthors() {
  const ownId = authState.user.id
  const following = await getFollowing(ownId)
  followingIds.value = following.map((f) => f.followingId)
  return [ownId, ...followingIds.value]
}

async function loadPage(p, append = false) {
  if (append) loadingMore.value = true
  else loading.value = true
  apiError.value = ''
  try {
    const authorIds = await resolveFeedAuthors()
    const result = await getPosts(p, 10, authorIds)
    posts.value = append ? posts.value.concat(result.content) : result.content
    page.value = result.number
    hasMore.value = !result.last
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function onPostDeleted(postId) {
  posts.value = posts.value.filter((p) => p.id !== postId)
}

onMounted(() => {
  loadPage(0)

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      scrollTimer = setTimeout(() => {
        if (hasMore.value && !loadingMore.value) {
          loadPage(page.value + 1, true)
        }
      }, 1000)
    } else {
      clearTimeout(scrollTimer)
    }
  })
  observer.observe(bottomSentinel.value)
})

onUnmounted(() => {
  clearTimeout(scrollTimer)
  observer?.disconnect()
})
</script>

<template>
  <div class="feed">
    <h1>Feed</h1>

    <p v-if="apiError" class="error-banner">{{ apiError }}</p>
    <p v-else-if="loading">Loading...</p>
    <p v-else-if="posts.length === 0 && followingIds.length === 0">
      You're not following anyone yet. Posts from people you follow (and your own posts) will show up here.
    </p>
    <p v-else-if="posts.length === 0">No posts yet from you or the people you follow.</p>

    <PostCard v-for="post in posts" :key="post.id" :post="post" @deleted="onPostDeleted" />

    <p v-if="loadingMore">Loading more...</p>
    <div ref="bottomSentinel" class="scroll-sentinel"></div>
  </div>
</template>
