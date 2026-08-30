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
const fieldErrors = reactive({})

const profileError = ref('')
const avatarError = ref('')
const emailError = ref('')
const passwordError = ref('')
const preferencesError = ref('')
const accountError = ref('')

const profileSuccess = ref('')
const avatarSuccess = ref('')
const emailSuccess = ref('')
const passwordSuccess = ref('')
const preferencesSuccess = ref('')

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
    emailForm.newEmail = user.value.email
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
  profileError.value = ''
  profileSuccess.value = ''
  fieldErrors.bio = profileForm.bio.length > 280 ? 'Bio must be 280 characters or fewer' : ''
  if (fieldErrors.bio) return
  try {
    user.value = await updateUser(userId, profileForm.bio)
    profileSuccess.value = 'Bio updated'
  } catch (err) {
    profileError.value = err.message
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
  avatarError.value = ''
  avatarSuccess.value = ''
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
    avatarSuccess.value = 'Profile picture updated'
  } catch (err) {
    avatarError.value = err.message
  }
}

async function saveEmail() {
  emailError.value = ''
  emailSuccess.value = ''
  fieldErrors.newEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.newEmail) ? '' : 'Enter a valid email address'
  if (fieldErrors.newEmail) return
  try {
    user.value = await changeEmail(userId, emailForm.newEmail)
    emailForm.newEmail = user.value.email
    emailSuccess.value = 'Email updated'
  } catch (err) {
    emailError.value = err.message
  }
}

async function savePassword() {
  passwordError.value = ''
  passwordSuccess.value = ''
  fieldErrors.newPassword = passwordForm.newPassword.length === 0 ? 'New password is required' : ''
  if (fieldErrors.newPassword) return
  try {
    await changePassword(userId, passwordForm.currentPassword, passwordForm.newPassword)
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordSuccess.value = 'Password updated'
  } catch (err) {
    passwordError.value = err.message
  }
}

async function savePreferences() {
  preferencesError.value = ''
  preferencesSuccess.value = ''
  try {
    await updatePreferences(userId, preferences.notifyOnLike, preferences.notifyOnComment, preferences.notifyOnFollow)
    preferencesSuccess.value = 'Preferences updated'
  } catch (err) {
    preferencesError.value = err.message
  }
}

async function removeAccount() {
  accountError.value = ''
  try {
    await deleteUser(userId)
    clearSession()
    router.push({ name: 'login' })
  } catch (err) {
    accountError.value = err.message
  }
}
</script>

<template>
  <div v-if="loading">Loading...</div>
  <p v-else-if="apiError" class="error-banner">{{ apiError }}</p>
  <div v-else-if="user">
    <h1>Settings</h1>

    <h2>Profile picture</h2>
    <img :src="avatarSrc" class="avatar settings-avatar" @error="onAvatarLoadError" />
    <form class="auth-form" @submit.prevent="saveAvatar">
      <label>Choose image <input type="file" accept="image/*" @change="onAvatarFileChange" /></label>
      <p v-if="fieldErrors.avatar" class="field-error">{{ fieldErrors.avatar }}</p>
      <img v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="image-preview" />
      <p v-if="avatarError" class="error-banner">{{ avatarError }}</p>
      <p v-if="avatarSuccess" class="success-banner">{{ avatarSuccess }}</p>
      <button type="submit">Change profile picture</button>
    </form>

    <h2>Profile</h2>
    <form class="auth-form" @submit.prevent="saveProfile">
      <textarea v-model="profileForm.bio" rows="3"></textarea>
      <p v-if="fieldErrors.bio" class="field-error">{{ fieldErrors.bio }}</p>
      <p v-if="profileError" class="error-banner">{{ profileError }}</p>
      <p v-if="profileSuccess" class="success-banner">{{ profileSuccess }}</p>
      <button type="submit">Change profile bio</button>
    </form>

    <h2>Email</h2>
    <form class="auth-form" @submit.prevent="saveEmail">
      <input v-model="emailForm.newEmail" type="email" />
      <p v-if="fieldErrors.newEmail" class="field-error">{{ fieldErrors.newEmail }}</p>
      <p v-if="emailError" class="error-banner">{{ emailError }}</p>
      <p v-if="emailSuccess" class="success-banner">{{ emailSuccess }}</p>
      <button type="submit">Change email</button>
    </form>

    <h2>Password</h2>
    <form class="auth-form" @submit.prevent="savePassword">
      <label>Current password <input v-model="passwordForm.currentPassword" type="password" /></label>
      <label>New password <input v-model="passwordForm.newPassword" type="password" /></label>
      <p v-if="fieldErrors.newPassword" class="field-error">{{ fieldErrors.newPassword }}</p>
      <p v-if="passwordError" class="error-banner">{{ passwordError }}</p>
      <p v-if="passwordSuccess" class="success-banner">{{ passwordSuccess }}</p>
      <button type="submit">Change password</button>
    </form>

    <h2>Notification preferences</h2>
    <form class="auth-form" @submit.prevent="savePreferences">
      <label class="checkbox-label"><input v-model="preferences.notifyOnLike" type="checkbox" /> Notify on likes</label>
      <label class="checkbox-label"><input v-model="preferences.notifyOnComment" type="checkbox" /> Notify on comments</label>
      <label class="checkbox-label"><input v-model="preferences.notifyOnFollow" type="checkbox" /> Notify on follows</label>
      <p v-if="preferencesError" class="error-banner">{{ preferencesError }}</p>
      <p v-if="preferencesSuccess" class="success-banner">{{ preferencesSuccess }}</p>
      <button type="submit">Save preferences</button>
    </form>

    <h2>Delete account</h2>
    <p v-if="accountError" class="error-banner">{{ accountError }}</p>
    <button class="danger-button" @click="removeAccount">Delete my account</button>
  </div>
</template>
