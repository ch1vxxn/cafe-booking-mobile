import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

/**
 * main.ts — точка входа фронтенда (как main в Java).
 *
 * createApp(App) — создаём Vue-приложение, где App — корневой компонент.
 * .use(router)   — подключаем РОУТЕР (переходы между страницами).
 * .use(pinia)    — подключаем PINIA (хранилище состояния: токен, пользователь).
 * .mount('#app') — «встраиваем» приложение в <div id="app"> из index.html.
 */
createApp(App)
  .use(createPinia())
  .use(router)
  .mount('#app')