/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      // Добавим свои цвета в стиле «кафе» — тёплые, уютные.
      // (В Tailwind можно расширять палитру под бренд.)
      colors: {
        cafe: {
          cream: '#FAF7F2',
          caramel: '#C08552',
          espresso: '#3E2C23',
        },
      },
    },
  },
  plugins: [],
}