<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getUser,
  updateUser,
  changeEmail,
  changePassword,
  deleteUser,
  getPreferences,
  updatePreferences,
  updateAvatar,
  avatarUrl,
} from '../api/users'
import { authState, clearSession } from '../stores/auth'
import { fileToBase64 } from '../lib/files'
import { DEFAULT_AVATAR } from '../lib/defaultAvatar'

const router = useRouter()
const userId = authState.user.id

const user = ref(null)
const apiError = ref('')
const loading = ref(true)
const avatarSrc = ref('')
const savedMessage = ref('')
const fieldErrors = reactive({})

const profileForm = reactive({ bio: '' })
const emailForm = reactive({ newEmail: '' })
const passwordForm = reactive({ currentPassword: '', newPassword: '' })
const preferences = reactive({ notifyOnLike: true, notifyOnComment: true, notifyOnFollow: true })

const avatarFile = ref(null)
const avatarPreviewUrl = ref('')

function onAvatarLoadError() {
  avatarSrc.value = DEFAULT_AVATAR
}

async function load() {
  loading.value = true
  apiError.value = ''
  avatarSrc.value = avatarUrl(userId)
  try {
    user.value = await getUser(userId)
    profileForm.bio = user.value.bio || ''
    const prefs = await getPreferences(userId)
    preferences.notifyOnLike = prefs.notifyOnLike
    preferences.notifyOnComment = prefs.notifyOnComment
    preferences.notifyOnFollow = prefs.notifyOnFollow
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function saveProfile() {
  fieldErrors.bio = profileForm.bio.length > 280 ? 'Bio must be 280 characters or fewer' : ''
  if (fieldErrors.bio) return
  try {
    user.value = await updateUser(userId, profileForm.bio)
    savedMessage.value = 'Profile updated'
  } catch (err) {
    apiError.value = err.message
  }
}

function onAvatarFileChange(event) {
  const picked = event.target.files[0]
  if (!picked) return
  fieldErrors.avatar = picked.type.startsWith('image/') ? '' : 'Please choose an image file'
  if (fieldErrors.avatar) {
    avatarFile.value = null
    avatarPreviewUrl.value = ''
    return
  }
  avatarFile.value = picked
  avatarPreviewUrl.value = URL.createObjectURL(picked)
}

async function saveAvatar() {
  if (!avatarFile.value) {
    fieldErrors.avatar = 'Choose an image first'
    return
  }
  try {
    const data = await fileToBase64(avatarFile.value)
    await updateAvatar(userId, avatarFile.value.type, data)
    avatarSrc.value = `${avatarUrl(userId)}?t=${Date.now()}`
    avatarFile.value = null
    avatarPreviewUrl.value = ''
    savedMessage.value = 'Avatar updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function saveEmail() {
  fieldErrors.newEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.newEmail) ? '' : 'Enter a valid email address'
  if (fieldErrors.newEmail) return
  try {
    user.value = await changeEmail(userId, emailForm.newEmail)
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
    await changePassword(userId, passwordForm.currentPassword, passwordForm.newPassword)
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    savedMessage.value = 'Password updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function savePreferences() {
  try {
    await updatePreferences(userId, preferences.notifyOnLike, preferences.notifyOnComment, preferences.notifyOnFollow)
    savedMessage.value = 'Preferences updated'
  } catch (err) {
    apiError.value = err.message
  }
}

async function removeAccount() {
  try {
    await deleteUser(userId)
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
    <h1>Settings</h1>
    <p v-if="savedMessage" class="hint">{{ savedMessage }}</p>

    <h2>Avatar</h2>
    <img :src="avatarSrc" class="avatar" @error="onAvatarLoadError" />
    <form class="auth-form" @submit.prevent="saveAvatar">
      <label>Choose image <input type="file" accept="image/*" @change="onAvatarFileChange" /></label>
      <p v-if="fieldErrors.avatar" class="field-error">{{ fieldErrors.avatar }}</p>
      <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="image-preview" />
      <button type="submit">Upload avatar</button>
    </form>

    <h2>Profile</h2>
    <form class="auth-form" @submit.prevent="saveProfile">
      <label>Bio <textarea v-model="profileForm.bio" rows="3"></textarea></label>
      <p v-if="fieldErrors.bio" class="field-error">{{ fieldErrors.bio }}</p>
      <button type="submit">Save profile</button>
    </form>

    <h2>Email</h2>
    <p class="hint">Current: {{ user.email }}</p>
    <form class="auth-form" @submit.prevent="saveEmail">
      <label>New email <input v-model="emailForm.newEmail" type="email" /></label>
      <p v-if="fieldErrors.newEmail" class="field-error">{{ fieldErrors.newEmail }}</p>
      <button type="submit">Update email</button>
    </form>

    <h2>Password</h2>
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
  </div>
</template>
