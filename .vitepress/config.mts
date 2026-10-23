import { defineConfig } from 'vitepress'

// BASE_PATH is set by the GitHub Pages workflow ("/repo-name/" or "/" with a custom domain).
const base = process.env.BASE_PATH || '/'

export default defineConfig({
  base,
  lang: 'en-US',
  title: 'ENCS5150 Android Lab',
  description:
    'Step-by-step Android (Java) guides for the ENCS515 Advanced Computer Systems Engineering Lab at Birzeit University.',
  srcExclude: ['README.md'],
  cleanUrls: true,
  lastUpdated: true,

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: `${base}favicon.svg` }],
    ['meta', { name: 'theme-color', content: '#ea580c' }],
  ],

  themeConfig: {
    logo: '/favicon.svg',
    siteTitle: 'ENCS5150-BZU',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Experiments', link: '/experiments/', activeMatch: '/experiments/' },
    ],

    sidebar: {
      '/': [
        {
          text: 'Getting Started',
          items: [
            { text: 'Setup Guide', link: '/setup' },
            { text: 'All Experiments', link: '/experiments/' },
          ],
        },
        {
          text: 'Experiments',
          items: [
            { text: '1. Introduction to Android', link: '/experiments/exp1-intro' },
            { text: '2. Android Layouts', link: '/experiments/exp2-layouts' },
          ],
        },
        {
          // Downloads + Documentation are pushed to the sidebar bottom in custom.css.
          text: 'Downloads',
          collapsed: true,
          items: [
            { text: 'Android Studio', link: 'https://developer.android.com/studio' },
            { text: 'IntelliJ IDEA (Spring Boot labs)', link: 'https://www.jetbrains.com/idea/download/' },
            { text: 'Postman (REST labs)', link: 'https://www.postman.com/downloads/' },
          ],
        },
        {
          text: 'Documentation',
          collapsed: true,
          items: [
            { text: 'Android Developer Guides', link: 'https://developer.android.com/guide' },
            { text: 'Android Studio User Guide', link: 'https://developer.android.com/studio/intro' },
            { text: 'Layouts in Views', link: 'https://developer.android.com/develop/ui/views/layout/declaring-layout' },
            { text: 'Activity Lifecycle', link: 'https://developer.android.com/guide/components/activities/activity-lifecycle' },
            { text: 'Intents and Intent Filters', link: 'https://developer.android.com/guide/components/intents-filters' },
            { text: 'Layout Inspector', link: 'https://developer.android.com/studio/debug/layout-inspector' },
            { text: 'Learn Java (dev.java)', link: 'https://dev.java/learn/' },
            { text: 'Spring Guides', link: 'https://spring.io/guides' },
            { text: 'Spring Initializr', link: 'https://start.spring.io' },
          ],
        },
      ],
    },

    outline: { level: [2, 3] },

    search: { provider: 'local' },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/ECE-Birzeit/encs5150' },
    ],

    footer: {
      message: 'ENCS5150 · Advanced Computer Systems Engineering Laboratory',
      copyright: 'Developed by Mohammad Abu-Shelbaia',
    },
  },
})
