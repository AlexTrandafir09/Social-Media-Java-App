<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPost, updatePost, imageUrl } from '../api/posts'
import { fileToBase64 } from '../lib/files'
import { authState } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const postId = Number(route.params.id)

const form = reactive({ content: '' })
const fieldErrors = reactive({})
const apiError = ref('')
const submitting = ref(false)
const loading = ref(true)

const images = ref([])
const previewIndex = ref(0)

async function load() {
  try {
    const post = await getPost(postId)
    if (post.authorId !== authState.user.id) {
      router.replace({ name: 'feed' })
      return
    }
    form.content = post.content
    images.value = post.images.map((img) => ({
      kind: 'existing',
      id: img.id,
      previewUrl: imageUrl(img.id),
    }))
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}
load()

function onFileChange(event) {
  const picked = Array.from(event.target.files)
  event.target.value = ''
  if (picked.length === 0) return

  const notImage = picked.find((f) => !f.type.startsWith('image/'))
  fieldErrors.file = notImage ? 'Please choose image files only' : ''
  if (fieldErrors.file) return

  previewIndex.value = images.value.length
  for (const file of picked) {
    images.value.push({ kind: 'new', file, previewUrl: URL.createObjectURL(file) })
  }
}

function removeCurrentImage() {
  images.value.splice(previewIndex.value, 1)
  if (previewIndex.value >= images.value.length) {
    previewIndex.value = Math.max(0, images.value.length - 1)
  }
}

function prevImage() {
  if (previewIndex.value > 0) previewIndex.value--
}
function nextImage() {
  if (previewIndex.value < images.value.length - 1) previewIndex.value++
}

function validate() {
  fieldErrors.content =
    form.content.length === 0
      ? 'Content is required'
      : form.content.length > 2000
        ? 'Content must be 2000 characters or fewer'
        : ''
  fieldErrors.file = images.value.length > 0 ? '' : 'A post needs at least one image'
  return !fieldErrors.content && !fieldErrors.file
}

async function onSubmit() {
  apiError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    const keepImageIds = images.value.filter((i) => i.kind === 'existing').map((i) => i.id)
    const newImages = await Promise.all(
      images.value
        .filter((i) => i.kind === 'new')
        .map(async ({ file }) => ({
          storageKey: file.name,
          contentType: file.type,
          data: await fileToBase64(file),
          filter: 'NONE',
        })),
    )
    await updatePost(postId, form.content, keepImageIds, newImages)
    router.back()
  } catch (err) {
    apiError.value = err.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form v-if="!loading" class="auth-form" @submit.prevent="onSubmit">
    <h1>Edit post</h1>
    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <label>
      Content
      <textarea v-model="form.content" rows="4"></textarea>
    </label>
    <p v-if="fieldErrors.content" class="field-error">{{ fieldErrors.content }}</p>

    <label>
      Images
      <input type="file" accept="image/*" multiple @change="onFileChange" />
    </label>
    <p v-if="fieldErrors.file" class="field-error">{{ fieldErrors.file }}</p>

    <div v-if="images.length" class="post-images">
      <img :src="images[previewIndex].previewUrl" />
      <button type="button" class="remove-image-btn" @click="removeCurrentImage">&times;</button>
      <template v-if="images.length > 1">
        <button type="button" class="slideshow-nav slideshow-prev" :disabled="previewIndex === 0" @click="prevImage">&lsaquo;</button>
        <button type="button" class="slideshow-nav slideshow-next" :disabled="previewIndex === images.length - 1" @click="nextImage">&rsaquo;</button>
        <span class="slideshow-count">{{ previewIndex + 1 }} / {{ images.length }}</span>
      </template>
    </div>

    <div class="form-actions">
      <button type="submit" :disabled="submitting">Save</button>
      <button type="button" class="button-secondary" @click="router.back()">Cancel</button>
    </div>
  </form>
</template>
