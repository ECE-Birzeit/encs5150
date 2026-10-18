---
layout: home

hero:
  name: ENCS5150
  text: Advanced Computer Systems Engineering Laboratory
  tagline: Department of Electrical & Computer Engineering · Birzeit University
  actions:
    - theme: brand
      text: Explore Experiments
      link: /experiments/
    - theme: alt
      text: Download Full Manual
      # Relative on purpose: VitePress doesn't add `base` to non-page links in hero actions.
      link: ./ENCS515-Manual.pdf
      target: _blank
    - theme: alt
      text: Setup Guide
      link: /setup

features:
  - icon:
      src: /icons/android.svg
      alt: Android
      width: 28
      height: 28
      wrap: true
    title: Android App Development
    details: Build real Android apps in Java with current Android Studio, from your first Activity to multi-screen apps with Intents, Fragments, and notifications.
  - icon:
      src: /icons/sqlite.svg
      alt: SQLite
      width: 28
      height: 28
      wrap: true
    title: Data and Networking
    details: Store data with SQLite and SharedPreferences, then connect your apps to REST APIs to send and receive JSON over the network.
  - icon:
      src: /icons/spring.svg
      alt: Spring
      width: 28
      height: 28
      wrap: true
    title: Backend with Spring Boot
    details: Learn the Spring Boot fundamentals and write your own REST services with Spring MVC, the server side of the APIs your apps call.
---
