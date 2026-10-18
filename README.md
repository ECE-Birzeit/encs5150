# ENCS5150 Android Lab

Student site for the ENCS515 / ENCS5150 Advanced Computer Systems Engineering Laboratory (Android, Java) at Birzeit University, built with [VitePress](https://vitepress.dev).

## Development

```sh
npm install
npm run dev       # dev server with hot reload at http://localhost:5173
```

## Build

```sh
npm run build     # outputs to .vitepress/dist (fails on dead links)
npm run preview   # serve the built site locally
```

To build for a sub-path (e.g. a GitHub project site at `/<repo>/`):

```sh
BASE_PATH=/encs5150/ npm run build
```

## Deploying

`.github/workflows/deploy.yml` builds and deploys to GitHub Pages on every push to `main`. In the repository settings, set **Pages → Source** to **GitHub Actions**. The workflow sets `BASE_PATH` from the Pages configuration, so it works both as a project site and with a custom domain.

## Layout

| Path | Contents |
| --- | --- |
| `index.md` | Home page |
| `setup.md` | Setup guide (Android Studio, emulator, creating lab projects) |
| `experiments/` | Experiment index and one page per experiment |
| `snippets/` | Long code files included in pages with `<<< @/snippets/File.java{java}` |
| `public/` | Static files served as-is (favicon, full manual PDF) |
| `.vitepress/config.mts` | Site config (nav, sidebar, search, footer) |
| `.vitepress/theme/custom.css` | Theme colours and overrides |
