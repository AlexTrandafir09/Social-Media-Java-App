<script setup>
import { ref } from 'vue'
import { deletePost, imageUrl } from '../api/posts'
import { getComments, createComment, updateComment, deleteComment } from '../api/comments'
import { likePost, unlikePost, getLikesForPost, countLikes } from '../api/likes'
import { authState } from '../stores/auth'
import UserChip from './UserChip.vue'
import Modal from './Modal.vue'
import Icon from './Icon.vue'

const props = defineProps({
  post: { type: Object, required: true },
})
const emit = defineEmits(['deleted'])

const post = ref(props.post)
const apiError = ref('')

const imageIndex = ref(0)
function prevImage() {
  if (imageIndex.value > 0) imageIndex.value--
}
function nextImage() {
  if (imageIndex.value < post.value.images.length - 1) imageIndex.value++
}

const likeCount = ref(0)
const iLiked = ref(false)
countLikes(post.value.id).then((c) => (likeCount.value = c))
getLikesForPost(post.value.id).then((likes) => {
  iLiked.value = likes.some((l) => l.userId === authState.user.id)
})

const totalComments = ref(0)

const showCommentsModal = ref(false)
const modalComments = ref([])
const modalPage = ref(0)
const modalHasMore = ref(true)

const newComment = ref('')
const commentError = ref('')
const editingCommentId = ref(null)
const editingCommentContent = ref('')

async function loadCommentCount() {
  try {
    const page = await getComments(post.value.id, 0)
    totalComments.value = page.totalElements
  } catch (err) {
    apiError.value = err.message
  }
}
loadCommentCount()

async function openCommentsModal() {
  showCommentsModal.value = true
  modalComments.value = []
  modalPage.value = 0
  modalHasMore.value = true
  await loadMoreModalComments()
}

async function loadMoreModalComments() {
  try {
    const page = await getComments(post.value.id, modalPage.value)
    modalComments.value = modalComments.value.concat(page.content)
    modalHasMore.value = !page.last
    modalPage.value++
  } catch (err) {
    apiError.value = err.message
  }
}

async function refreshComments() {
  await loadCommentCount()
  if (showCommentsModal.value) {
    await openCommentsModal()
  }
}

async function toggleLike() {
  try {
    if (iLiked.value) {
      await unlikePost(post.value.id)
      likeCount.value--
    } else {
      await likePost(post.value.id)
      likeCount.value++
    }
    iLiked.value = !iLiked.value
  } catch (err) {
    apiError.value = err.message
  }
}

async function removePost() {
  try {
    await deletePost(post.value.id)
    emit('deleted', post.value.id)
  } catch (err) {
    apiError.value = err.message
  }
}

async function submitComment() {
  commentError.value = newComment.value.length === 0 ? 'Comment cannot be empty' : ''
  if (commentError.value) return
  try {
    await createComment(post.value.id, newComment.value)
    newComment.value = ''
    await refreshComments()
  } catch (err) {
    commentError.value = err.message
  }
}

function startEditComment(comment) {
  editingCommentId.value = comment.id
  editingCommentContent.value = comment.content
}

async function saveEditComment(id) {
  try {
    await updateComment(id, editingCommentContent.value)
    editingCommentId.value = null
    await refreshComments()
  } catch (err) {
    apiError.value = err.message
  }
}

async function removeComment(id) {
  try {
    await deleteComment(id)
    await refreshComments()
  } catch (err) {
    apiError.value = err.message
  }
}

function formatDate(iso) {
  return new Date(iso).toLocaleString()
}
</script>

<template>
  <article class="post-card">
    <div v-if="post.authorId === authState.user.id" class="post-actions-top">
      <router-link :to="{ name: 'edit-post', params: { id: post.id } }" class="icon-button" title="Edit" aria-label="Edit"><Icon name="edit" /></router-link>
      <button class="icon-button danger" title="Delete" aria-label="Delete" @click="removePost"><Icon name="trash" /></button>
    </div>

    <p class="post-meta">
      <UserChip :user-id="post.authorId" />
      &middot; {{ formatDate(post.createdAt) }}
    </p>

    <div v-if="post.images?.length" class="post-images">
      <img :src="imageUrl(post.images[imageIndex].id)" :alt="post.images[imageIndex].storageKey" />
      <template v-if="post.images.length > 1">
        <button type="button" class="slideshow-nav slideshow-prev" :disabled="imageIndex === 0" @click="prevImage">&lsaquo;</button>
        <button type="button" class="slideshow-nav slideshow-next" :disabled="imageIndex === post.images.length - 1" @click="nextImage">&rsaquo;</button>
        <span class="slideshow-count">{{ imageIndex + 1 }} / {{ post.images.length }}</span>
      </template>
    </div>

    <p class="post-content">{{ post.content }}</p>

    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <div class="like-row">
      <button class="like-button" :class="{ liked: iLiked }" :title="iLiked ? 'Unlike' : 'Like'" :aria-label="iLiked ? 'Unlike' : 'Like'" @click="toggleLike">
        <Icon name="thumbs-up" :filled="iLiked" />
      </button>
      <span>{{ likeCount }} like{{ likeCount === 1 ? '' : 's' }}</span>
    </div>

    <button class="view-comments-link" @click="openCommentsModal">
      {{ totalComments > 0 ? `View comments (${totalComments})` : 'Add a comment' }}
    </button>

    <Modal v-if="showCommentsModal" title="Comments" @close="showCommentsModal = false">
      <ul class="comment-list">
        <li v-for="comment in modalComments" :key="comment.id">
          <div v-if="comment.authorId === authState.user.id && editingCommentId !== comment.id" class="post-actions-top">
            <button class="icon-button" title="Edit" aria-label="Edit" @click="startEditComment(comment)"><Icon name="edit" /></button>
            <button class="icon-button danger" title="Delete" aria-label="Delete" @click="removeComment(comment.id)"><Icon name="trash" /></button>
          </div>

          <p class="post-meta">
            <UserChip :user-id="comment.authorId" />
            &middot; {{ formatDate(comment.createdAt) }}
          </p>
          <div v-if="editingCommentId === comment.id" class="comment-form">
            <textarea v-model="editingCommentContent" rows="2"></textarea>
            <div class="edit-comment-actions">
              <button class="button-secondary" @click="saveEditComment(comment.id)">Save</button>
              <button class="button-secondary" @click="editingCommentId = null">Cancel</button>
            </div>
          </div>
          <template v-else>
            <p>{{ comment.content }}</p>
          </template>
        </li>
      </ul>

      <button v-if="modalHasMore" class="button-secondary" @click="loadMoreModalComments">Load more</button>

      <form @submit.prevent="submitComment" class="comment-form">
        <textarea v-model="newComment" rows="2" placeholder="Add a comment..."></textarea>
        <p v-if="commentError" class="field-error">{{ commentError }}</p>
        <button type="submit" class="button-primary">Comment</button>
      </form>
    </Modal>
  </article>
</template>
