<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUser, avatarUrl } from '../api/users'
import { getPostsByAuthor } from '../api/posts'
import { getFollowing, getFollowers, follow, unfollow } from '../api/follows'
import { authState } from '../stores/auth'
import { DEFAULT_AVATAR } from '../lib/defaultAvatar'
import PostCard from '../components/PostCard.vue'
import UserChip from '../components/UserChip.vue'
import Modal from '../components/Modal.vue'

const route = useRoute()

const profileId = computed(() => Number(route.params.id))
const isOwnProfile = computed(() => profileId.value === authState.user.id)

const user = ref(null)
const posts = ref([])
const isFollowing = ref(false)
const apiError = ref('')
const loading = ref(true)
const avatarSrc = ref('')

const followers = ref([])
const following = ref([])
const showFollowers = ref(false)
const showFollowing = ref(false)

function onAvatarLoadError() {
  avatarSrc.value = DEFAULT_AVATAR
}

async function load() {
  loading.value = true
  apiError.value = ''
  avatarSrc.value = avatarUrl(profileId.value)
  showFollowers.value = false
  showFollowing.value = false
  try {
    user.value = await getUser(profileId.value)
    posts.value = await getPostsByAuthor(profileId.value)
    followers.value = await getFollowers(profileId.value)
    following.value = await getFollowing(profileId.value)

    if (!isOwnProfile.value) {
      const viewerFollowing = await getFollowing(authState.user.id)
      isFollowing.value = viewerFollowing.some((f) => f.followingId === profileId.value)
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
      followers.value = followers.value.filter((f) => f.followerId !== authState.user.id)
    } else {
      await follow(profileId.value)
      followers.value.push({ followerId: authState.user.id })
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

    <div class="follow-counts">
      <button @click="showFollowers = !showFollowers">{{ followers.length }} followers</button>
      <button @click="showFollowing = !showFollowing">{{ following.length }} following</button>
    </div>

    <Modal v-if="showFollowers" title="Followers" @close="showFollowers = false">
      <ul class="user-list">
        <li v-if="followers.length === 0">No followers yet.</li>
        <li v-for="f in followers" :key="f.followerId"><UserChip :user-id="f.followerId" /></li>
      </ul>
    </Modal>

    <Modal v-if="showFollowing" title="Following" @close="showFollowing = false">
      <ul class="user-list">
        <li v-if="following.length === 0">Not following anyone yet.</li>
        <li v-for="f in following" :key="f.followingId"><UserChip :user-id="f.followingId" /></li>
      </ul>
    </Modal>

    <button v-if="!isOwnProfile" @click="toggleFollow">{{ isFollowing ? 'Unfollow' : 'Follow' }}</button>

    <h2>Posts</h2>
    <PostCard v-for="post in posts" :key="post.id" :post="post" @deleted="onPostDeleted" />
  </div>
</template>
