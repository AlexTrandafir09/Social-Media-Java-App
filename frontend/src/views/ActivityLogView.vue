<script setup>
import { onMounted, ref } from 'vue'
import { getActivity } from '../api/activity'

const entries = ref([])
const page = ref(0)
const totalPages = ref(0)
const apiError = ref('')
const loading = ref(true)

async function loadPage(p) {
  loading.value = true
  apiError.value = ''
  try {
    const result = await getActivity(p)
    entries.value = result.content
    page.value = result.number
    totalPages.value = result.totalPages
  } catch (err) {
    apiError.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage(0))
</script>

<template>
  <h1>Activity log</h1>
  <p v-if="apiError" class="error-banner">{{ apiError }}</p>
  <p v-else-if="loading">Loading...</p>
  <table v-else class="activity-table">
    <thead>
      <tr>
        <th>When</th>
        <th>Actor</th>
        <th>Action</th>
        <th>Description</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="e in entries" :key="e.id">
        <td>{{ new Date(e.createdAt).toLocaleString() }}</td>
        <td>#{{ e.actorId }}</td>
        <td>{{ e.action }}</td>
        <td>{{ e.description }}</td>
      </tr>
    </tbody>
  </table>

  <div v-if="totalPages > 1" class="pagination">
    <button :disabled="page === 0" @click="loadPage(page - 1)">Previous</button>
    <span>Page {{ page + 1 }} of {{ totalPages }}</span>
    <button :disabled="page + 1 >= totalPages" @click="loadPage(page + 1)">Next</button>
  </div>
</template>
