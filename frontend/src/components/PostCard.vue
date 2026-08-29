<script setup>
import { ref } from 'vue'
import { updatePost, deletePost, imageUrl } from '../api/posts'
import { getComments, createComment, updateComment, deleteComment } from '../api/comments'
import { likePost, unlikePost, getLikesForPost, countLikes } from '../api/likes'
import { authState } from '../stores/auth'
import UserChip from './UserChip.vue'

const props = defineProps({
  post: { type: Object, required: true },
})
const emit = defineEmits(['deleted'])

const post = ref(props.post)
const apiError = ref('')

const editingPost = ref(false)
const editContent = ref('')

const likeCount = ref(0)
const iLiked = ref(false)
countLikes(post.value.id).then((c) => (likeCount.value = c))
getLikesForPost(post.value.id).then((likes) => {
  iLiked.value = likes.some((l) => l.userId === authState.user.id)
})

const showComments = ref(false)
const commentCount = ref(null)
const comments = ref([])
const newComment = ref('')
const commentError = ref('')
const editingCommentId = ref(null)
const editingCommentContent = ref('')

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

function startEditPost() {
  editContent.value = post.value.content
  editingPost.value = true
}

async function saveEditPost() {
  try {
    post.value = await updatePost(post.value.id, editContent.value)
    editingPost.value = false
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

async function loadComments() {
  try {
    comments.value = (await getComments(post.value.id)).content
    commentCount.value = comments.value.length
  } catch (err) {
    apiError.value = err.message
  }
}

async function toggleComments() {
  showComments.value = !showComments.value
  if (showComments.value && comments.value.length === 0) {
    await loadComments()
  }
}

async function submitComment() {
  commentError.value = newComment.value.length === 0 ? 'Comment cannot be empty' : ''
  if (commentError.value) return
  try {
    await createComment(post.value.id, newComment.value)
    newComment.value = ''
    await loadComments()
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
    await loadComments()
  } catch (err) {
    apiError.value = err.message
  }
}

async function removeComment(id) {
  try {
    await deleteComment(id)
    comments.value = comments.value.filter((c) => c.id !== id)
    commentCount.value = comments.value.length
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
    <p class="post-meta">
      <UserChip :user-id="post.authorId" />
      &middot; {{ formatDate(post.createdAt) }}
    </p>

    <template v-if="editingPost">
      <textarea v-model="editContent" rows="4"></textarea>
      <button @click="saveEditPost">Save</button>
      <button @click="editingPost = false">Cancel</button>
    </template>
    <p v-else class="post-content">{{ post.content }}</p>

    <div v-if="post.images?.length" class="post-images">
      <img v-for="img in post.images" :key="img.id" :src="imageUrl(img.id)" :alt="img.storageKey" />
    </div>

    <div v-if="post.authorId === authState.user.id && !editingPost" class="post-actions">
      <button @click="startEditPost">Edit</button>
      <button @click="removePost">Delete</button>
    </div>

    <p v-if="apiError" class="error-banner">{{ apiError }}</p>

    <div class="like-row">
      <button @click="toggleLike">{{ iLiked ? 'Unlike' : 'Like' }}</button>
      <span>{{ likeCount }} like{{ likeCount === 1 ? '' : 's' }}</span>
      <button @click="toggleComments">
        {{ showComments ? 'Hide comments' : `Comments${commentCount !== null ? ` (${commentCount})` : ''}` }}
      </button>
    </div>

    <template v-if="showComments">
      <ul class="comment-list">
        <li v-for="comment in comments" :key="comment.id">
          <p class="post-meta">
            <UserChip :user-id="comment.authorId" />
            &middot; {{ formatDate(comment.createdAt) }}
          </p>
          <template v-if="editingCommentId === comment.id">
            <textarea v-model="editingCommentContent" rows="2"></textarea>
            <button @click="saveEditComment(comment.id)">Save</button>
            <button @click="editingCommentId = null">Cancel</button>
          </template>
          <template v-else>
            <p>{{ comment.content }}</p>
            <div v-if="comment.authorId === authState.user.id" class="post-actions">
              <button @click="startEditComment(comment)">Edit</button>
              <button @click="removeComment(comment.id)">Delete</button>
            </div>
          </template>
        </li>
      </ul>

      <form @submit.prevent="submitComment" class="comment-form">
        <textarea v-model="newComment" rows="2" placeholder="Add a comment..."></textarea>
        <p v-if="commentError" class="field-error">{{ commentError }}</p>
        <button type="submit">Comment</button>
      </form>
    </template>
  </article>
</template>
