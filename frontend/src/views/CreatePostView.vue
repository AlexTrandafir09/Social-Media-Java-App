<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost } from '../api/posts'
import { fileToBase64 } from '../lib/files'

const router = useRouter()
const form = reactive({ content: '' })
const fieldErrors = reactive({})
const apiError = ref('')
const submitting = ref(false)

const file = ref(null)
const previewUrl = ref('')

function onFileChange(event) {
  const picked = event.target.files[0]
  if (!picked) return
  fieldErrors.file = picked.type.startsWith('image/') ? '' : 'Please choose an image file'
  if (fieldErrors.file) {
    file.value = null
    previewUrl.value = ''
    return
  }
  file.value = picked
  previewUrl.value = URL.createObjectURL(picked)
}

function validate() {
  fieldErrors.content =
    form.content.length === 0
      ? 'Content is required'
      : form.content.length > 2000
        ? 'Content must be 2000 characters or fewer'
        : ''
  fieldErrors.file = file.value ? '' : 'A post needs an image'
  return !fieldErrors.content && !fieldErrors.file
}

async function onSubmit() {
  apiError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const data = await fileToBase64(file.value)
    await createPost(form.content, [
      { storageKey: file.value.name, contentType: file.value.type, data, filter: 'NONE' },
    ])
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
      Image
      <input type="file" accept="image/*" @change="onFileChange" />
    </label>
    <p v-if="fieldErrors.file" class="field-error">{{ fieldErrors.file }}</p>
    <img v-if="previewUrl" :src="previewUrl" class="image-preview" />

    <button type="submit" :disabled="submitting">Post</button>
  </form>
</template>
