/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: 'var(--color-primary)',
          hover: 'hsl(360, 49%, 45%)',
          light: 'hsl(360, 49%, 95%)',
          dark: 'hsl(360, 49%, 38%)',
        },
        background: 'var(--color-background)',
        card: 'var(--color-card)',
        border: 'var(--color-border)',
        'muted-foreground': 'var(--color-muted-foreground)',
        foreground: 'var(--color-foreground)',
      },
    },
  },
  plugins: [],
}
