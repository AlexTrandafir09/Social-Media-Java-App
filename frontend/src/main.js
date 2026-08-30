import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.config.errorHandler = (err) => {
  console.error(err)
  alert('Something went wrong. Please try again.')
}

app.use(router).mount('#app')
