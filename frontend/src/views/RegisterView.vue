<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { setSession } from '../stores/auth'

const router = useRouter()
const form = reactive({ username: '', email: '', password: '' })
const fieldErrors = reactive({})
const apiError = ref('')
const submitting = ref(false)

function validate() {
  fieldErrors.username =
    form.username.length < 3 || form.username.length > 30
      ? 'Username must be 3-30 characters'
      : ''
  fieldErrors.email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email) ? '' : 'Enter a valid email address'
  fieldErrors.password = form.password.length === 0 ? 'Password is required' : ''
  return !fieldErrors.username && !fieldErrors.email && !fieldErrors.password
}

async function onSubmit() {
  apiError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const { accessToken } = await register(form.username, form.email, form.password)
    setSession(accessToken)
    router.push({ name: 'feed' })
  } catch (err) {
    apiError.value = err.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="auth-form" @submit.prevent="onSubmit">
    <h1>Register</h1>
    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <label>
      Username
      <input v-model="form.username" type="text" />
    </label>
    <p v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</p>

    <label>
      Email
      <input v-model="form.email" type="email" />
    </label>
    <p v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</p>

    <label>
      Password
      <input v-model="form.password" type="password" />
    </label>
    <p v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</p>

    <button type="submit" :disabled="submitting">Create account</button>
    <router-link to="/login">Already have an account? Log in</router-link>
  </form>
</template>
