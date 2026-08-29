<script setup>
defineProps({
  title: { type: String, required: true },
})
const emit = defineEmits(['close'])
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-box">
      <div class="modal-header">
        <h2>{{ title }}</h2>
        <button class="modal-close" @click="emit('close')">&times;</button>
      </div>
      <slot />
    </div>
  </div>
</template>
