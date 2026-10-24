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
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:site_name', content: 'ECE Lab Manuals · Birzeit University' }],
    ['meta', { property: 'og:image', content: 'https://encs5150.birzeit.me/og.png' }],
    ['meta', { property: 'og:image:width', content: '1200' }],
    ['meta', { property: 'og:image:height', content: '630' }],
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['script', { async: '', src: 'https://www.googletagmanager.com/gtag/js?id=G-K834Q72PP9' }],
    [
      'script',
      {},
      `window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());
      gtag('config', 'G-K834Q72PP9');`,
    ],
  ],

  sitemap: { hostname: 'https://encs5150.birzeit.me/' },

  // Link previews: per-page title, the page's (or site's) description, and canonical URL.
  transformHead({ pageData, siteData }) {
    const page = pageData.frontmatter.layout === 'home' ? '' : pageData.title
    const path = pageData.relativePath.replace(/(^|\/)index\.md$/, '$1').replace(/\.md$/, '')
    return [
      ['meta', { property: 'og:title', content: page ? `${page} · ENCS5150` : 'ENCS5150 Advanced Computer Systems Engineering Laboratory' }],
      ['meta', { property: 'og:description', content: pageData.frontmatter.description || pageData.description || siteData.description }],
      ['meta', { property: 'og:url', content: `https://encs5150.birzeit.me/${path}` }],
    ]
  },

  themeConfig: {
    logo: '/favicon.svg',
    siteTitle: 'ENCS5150-BZU',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'Experiments', link: '/experiments/', activeMatch: '/experiments/' },
      { text: 'All labs', link: 'https://birzeit.me/' },
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
            { text: '3. Intents and Notifications', link: '/experiments/exp3-intents' },
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
