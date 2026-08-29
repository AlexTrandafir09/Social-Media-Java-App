<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '../api/posts'

const router = useRouter()
const form = reactive({ content: '', storageKey: '' })
const fieldErrors = reactive({})
const apiError = ref('')
const submitting = ref(false)

function validate() {
  fieldErrors.content =
    form.content.length === 0
      ? 'Content is required'
      : form.content.length > 2000
        ? 'Content must be 2000 characters or fewer'
        : ''
  fieldErrors.storageKey = form.storageKey.length === 0 ? 'A post needs at least one image' : ''
  return !fieldErrors.content && !fieldErrors.storageKey
}

async function onSubmit() {
  apiError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    await createPost(form.content, [{ storageKey: form.storageKey, filter: 'NONE' }])
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
    <h1>New post</h1>
    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <label>
      Content
      <textarea v-model="form.content" rows="4"></textarea>
    </label>
    <p v-if="fieldErrors.content" class="field-error">{{ fieldErrors.content }}</p>

    <label>
      Image storage key
      <input v-model="form.storageKey" type="text" placeholder="e.g. photo.png" />
    </label>
    <p v-if="fieldErrors.storageKey" class="field-error">{{ fieldErrors.storageKey }}</p>
    <p class="hint">No file upload is wired up yet - this is just an identifier the backend stores.</p>

    <button type="submit" :disabled="submitting">Post</button>
  </form>
</template>
