<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '../api/auth'
import { setSession } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const form = reactive({ username: '', password: '' })
const fieldErrors = reactive({})
const apiError = ref('')
const submitting = ref(false)

function validate() {
  fieldErrors.username = form.username.length === 0 ? 'Username is required' : ''
  fieldErrors.password = form.password.length === 0 ? 'Password is required' : ''
  return !fieldErrors.username && !fieldErrors.password
}

async function onSubmit() {
  apiError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const { accessToken } = await login(form.username, form.password)
    setSession(accessToken)
    router.push(route.query.redirect || { name: 'feed' })
  } catch (err) {
    apiError.value = err.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="auth-form" @submit.prevent="onSubmit">
    <h1>Log in</h1>
    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <label>
      Username
      <input v-model="form.username" type="text" />
    </label>
    <p v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</p>

    <label>
      Password
      <input v-model="form.password" type="password" />
    </label>
    <p v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</p>

    <button type="submit" :disabled="submitting">Log in</button>
    <router-link to="/register">Need an account? Register</router-link>
  </form>
</template>
