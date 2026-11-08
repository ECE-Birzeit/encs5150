import { defineConfig } from 'vitepress'
import { withPwa } from '@vite-pwa/vitepress'
import { linkPreviews } from './og.mts'
import { experimentEdges, sidebar } from './lab.mts'
import { fileURLToPath } from 'node:url'

const srcDir = fileURLToPath(new URL('..', import.meta.url))

// Per-experiment link previews, drawn at build time (see og.mts)
const og = linkPreviews({ code: 'ENCS5150', name: 'Advanced Computer Systems Engineering Laboratory', host: 'encs5150.birzeit.me', accent: '#fb923c', platform: 'Android (Java) · Spring Boot' })

// BASE_PATH is set by the GitHub Pages workflow ("/repo-name/" or "/" with a custom domain).
const base = process.env.BASE_PATH || '/'

export default withPwa(defineConfig({
  base,
  lang: 'en-US',
  title: 'ENCS5150 Android Lab',
  description:
    'Step-by-step Android (Java) guides for the ENCS515 Advanced Computer Systems Engineering Lab at Birzeit University.',
  srcExclude: ['README.md'],
  cleanUrls: true,
  lastUpdated: true,

  head: [
    // .ico first for browsers and tools without SVG favicon support; the SVG wins where it is supported.
    ['link', { rel: 'icon', href: `${base}favicon.ico`, sizes: 'any' }],
    ['link', { rel: 'icon', type: 'image/svg+xml', href: `${base}favicon.svg` }],
    ['link', { rel: 'apple-touch-icon', href: `${base}apple-touch-icon.png` }],
    ['meta', { name: 'theme-color', content: '#ea580c' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:site_name', content: 'ECE Labs · Birzeit University' }],
    ...og.head,
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

  buildEnd: og.buildEnd,

  // No Previous page on the first experiment, no Next page on the last (see lab.mts)
  transformPageData: experimentEdges(srcDir),

  // Link previews: per-page title, the page's (or site's) description, and canonical URL.
  transformHead({ pageData, siteData }) {
    const page = pageData.frontmatter.layout === 'home' ? '' : pageData.title
    const path = pageData.relativePath.replace(/(^|\/)index\.md$/, '$1').replace(/\.md$/, '')
    return [
      ...og.transformHead({ pageData } as any),
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

    // Built from the pages (see lab.mts): adding an experiment or a setup page is the only step
    sidebar: sidebar(srcDir, {
      downloads: [
            { text: 'Android Studio', link: 'https://developer.android.com/studio' },
            { text: 'IntelliJ IDEA (Spring Boot labs)', link: 'https://www.jetbrains.com/idea/download/' },
            { text: 'Postman (REST labs)', link: 'https://www.postman.com/downloads/' },
      ],
      docs: [
            { text: 'Android Developer Guides', link: 'https://developer.android.com/guide' },
            { text: 'Android Studio User Guide', link: 'https://developer.android.com/studio/intro' },
            { text: 'Layouts in Views', link: 'https://developer.android.com/develop/ui/views/layout/declaring-layout' },
            { text: 'Activity Lifecycle', link: 'https://developer.android.com/guide/components/activities/activity-lifecycle' },
            { text: 'Intents and Intent Filters', link: 'https://developer.android.com/guide/components/intents-filters' },
            { text: 'Save Data Using SQLite', link: 'https://developer.android.com/training/data-storage/sqlite' },
            { text: 'Database Inspector', link: 'https://developer.android.com/studio/inspect/database' },
            { text: 'Drawable Animations', link: 'https://developer.android.com/develop/ui/views/animations/drawable-animation' },
            { text: 'View (Tween) Animation', link: 'https://developer.android.com/guide/topics/resources/animation-resource#View' },
            { text: 'Layout Inspector', link: 'https://developer.android.com/studio/debug/layout-inspector' },
            { text: 'Learn Java (dev.java)', link: 'https://dev.java/learn/' },
            { text: 'Spring Guides', link: 'https://spring.io/guides' },
            { text: 'Spring Initializr', link: 'https://start.spring.io' },
      ],
    }),

    outline: { level: [2, 3] },

    search: { provider: 'local' },

    editLink: {
      pattern: 'https://github.com/ECE-Birzeit/encs5150/edit/main/:path',
      text: 'Edit this page on GitHub',
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/ECE-Birzeit/encs5150' },
    ],

    footer: {
      message: 'ENCS5150 · Advanced Computer Systems Engineering Laboratory',
      copyright: 'Developed by <a href="https://mabushelbaia.com" target="_blank">Mohammad Abu-Shelbaia</a>',
    },
  },

  // Installable, offline-capable app. Icons are rendered from favicon.svg (the sidebar logo).
  pwa: {
    registerType: 'autoUpdate',
    manifest: {
      name: 'ENCS5150 Advanced Computer Systems Engineering Laboratory',
      short_name: 'ENCS5150',
      description:
        'Step-by-step Android (Java) guides for the ENCS515 Advanced Computer Systems Engineering Lab at Birzeit University.',
      theme_color: '#ea580c',
      background_color: '#ffffff',
      display: 'standalone',
      scope: base,
      start_url: base,
      icons: [
        { src: `${base}icons/pwa-192x192.png`, sizes: '192x192', type: 'image/png' },
        { src: `${base}icons/pwa-512x512.png`, sizes: '512x512', type: 'image/png' },
        { src: `${base}icons/maskable-512x512.png`, sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        { src: `${base}favicon.svg`, sizes: 'any', type: 'image/svg+xml' },
      ],
    },
    workbox: {
      // The manual PDF is left out: it is too large to precache, and the guides do not need it offline.
      globPatterns: ['**/*.{css,js,html,svg,png,jpg,jpeg,webp,ico,txt,woff2}'],
      // Link-preview images are for chat apps and search engines, not for reading offline
      globIgnores: ['og/**'],
      maximumFileSizeToCacheInBytes: 5000000,
    },
    experimental: {
      includeAllowlist: true,
    },
  },
}))
