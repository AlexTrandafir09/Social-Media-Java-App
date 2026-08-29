<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser, updateUser, changeEmail, changePassword, deleteUser, getPreferences, updatePreferences } from '../api/users'
import { getPostsByAuthor } from '../api/posts'
import { getFollowing, follow, unfollow } from '../api/follows'
import { authState, clearSession } from '../stores/auth'
import PostCard from '../components/PostCard.vue'

const route = useRoute()
const router = useRouter()

const profileId = computed(() => Number(route.params.id))
const isOwnProfile = computed(() => profileId.value === authState.user.id)

const user = ref(null)
const posts = ref([])
const isFollowing = ref(false)
const apiError = ref('')
const loading = ref(true)

const profileForm = reactive({ bio: '', avatarUrl: '' })
const emailForm = reactive({ newEmail: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '' })
const preferences = reactive({ notifyOnLike: true, notifyOnComment: true, notifyOnFollow: true })
const fieldErrors = reactive({})
const savedMessage = ref('')

async function load() {
  loading.value = true
  apiError.value = ''
  try {
    user.value = await getUser(profileId.value)
    posts.value = await getPostsByAuthor(profileId.value)

    if (isOwnProfile.value) {
      profileForm.bio = user.value.bio || ''
      profileForm.avatarUrl = user.value.avatarUrl || ''
      const prefs = await getPreferences(profileId.value)
      preferences.notifyOnLike = prefs.notifyOnLike
      preferences.notifyOnComment = prefs.notifyOnComment
      preferences.notifyOnFollow = prefs.notifyOnFollow
    } else {
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

async function saveProfile() {
  fieldErrors.bio = profileForm.bio.length > 280 ? 'Bio must be 280 characters or fewer' : ''
  if (fieldErrors.bio) return
  try {
    user.value = await updateUser(profileId.value, profileForm.bio, profileForm.avatarUrl)
    savedMessage.value = 'Profile updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function saveEmail() {
  fieldErrors.newEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.newEmail) ? '' : 'Enter a valid email address'
  if (fieldErrors.newEmail) return
  try {
    user.value = await changeEmail(profileId.value, emailForm.newEmail)
    emailForm.newEmail = ''
    savedMessage.value = 'Email updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function savePassword() {
  fieldErrors.newPassword = passwordForm.newPassword.length === 0 ? 'New password is required' : ''
  if (fieldErrors.newPassword) return
  try {
    await changePassword(profileId.value, passwordForm.currentPassword, passwordForm.newPassword)
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    savedMessage.value = 'Password updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function savePreferences() {
  try {
    await updatePreferences(profileId.value, preferences.notifyOnLike, preferences.notifyOnComment, preferences.notifyOnFollow)
    savedMessage.value = 'Preferences updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function removeAccount() {
  try {
    await deleteUser(profileId.value)
    clearSession()
    router.push({ name: 'login' })
  } catch (err) {
    apiError.value = err.message
  }
}
</script>

<template>
  <div v-if="loading">Loading...</div>
  <p v-else-if="apiError" class="error-banner">{{ apiError }}</p>
  <div v-else-if="user">
    <h1>{{ user.username }}</h1>
    <p v-if="user.bio">{{ user.bio }}</p>
    <p v-if="savedMessage" class="hint">{{ savedMessage }}</p>

    <button v-if="!isOwnProfile" @click="toggleFollow">{{ isFollowing ? 'Unfollow' : 'Follow' }}</button>

    <template v-if="isOwnProfile">
      <h2>Edit profile</h2>
      <form class="auth-form" @submit.prevent="saveProfile">
        <label>Bio <textarea v-model="profileForm.bio" rows="3"></textarea></label>
        <p v-if="fieldErrors.bio" class="field-error">{{ fieldErrors.bio }}</p>
        <label>Avatar URL <input v-model="profileForm.avatarUrl" type="text" /></label>
        <button type="submit">Save profile</button>
      </form>

      <h2>Change email</h2>
      <form class="auth-form" @submit.prevent="saveEmail">
        <label>New email <input v-model="emailForm.newEmail" type="email" /></label>
        <p v-if="fieldErrors.newEmail" class="field-error">{{ fieldErrors.newEmail }}</p>
        <button type="submit">Update email</button>
      </form>

      <h2>Change password</h2>
      <form class="auth-form" @submit.prevent="savePassword">
        <label>Current password <input v-model="passwordForm.currentPassword" type="password" /></label>
        <label>New password <input v-model="passwordForm.newPassword" type="password" /></label>
        <p v-if="fieldErrors.newPassword" class="field-error">{{ fieldErrors.newPassword }}</p>
        <button type="submit">Update password</button>
      </form>

      <h2>Notification preferences</h2>
      <form class="auth-form" @submit.prevent="savePreferences">
        <label><input v-model="preferences.notifyOnLike" type="checkbox" /> Notify on likes</label>
        <label><input v-model="preferences.notifyOnComment" type="checkbox" /> Notify on comments</label>
        <label><input v-model="preferences.notifyOnFollow" type="checkbox" /> Notify on follows</label>
        <button type="submit">Save preferences</button>
      </form>

      <h2>Danger zone</h2>
      <button @click="removeAccount">Delete my account</button>
    </template>

    <h2>Posts</h2>
    <PostCard v-for="post in posts" :key="post.id" :post="post" @deleted="onPostDeleted" />
  </div>
</template>
