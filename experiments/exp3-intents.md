# Using Intents and Notifications

In this lab you build one screen with five buttons. Three of them use **implicit Intents** to hand work to other apps: the phone dialer, an email app and Google Maps. The other two notify the user: a **Toast** that pops up on screen, and a **notification** in the status bar.

## Objectives

- Use the `Intent` class to ask other apps to do work: dial a number, write an email, show a map.
- Tell explicit and implicit Intents apart, and know how Android picks the app that handles one.
- Show short messages with Toasts.
- Post a notification in the status bar, including the notification channel and the permission current Android requires.

## Background

### Intents

An **Intent** is a message describing an operation you want performed. One component creates it and Android delivers it to a component that can do the work. You already used one in [Android Layouts](./exp2-layouts) to open a second Activity.

An Intent can carry these fields:

| Field | Meaning | Example |
| --- | --- | --- |
| **Action** | What to do | `Intent.ACTION_DIAL`, `ACTION_VIEW`, `ACTION_SENDTO` |
| **Data** | What to do it on, as a URI | `tel:+97022982000`, `geo:31.959,35.182`, `mailto:` |
| **Category** | Extra information about which component may handle it | `CATEGORY_LAUNCHER` |
| **Component** | The exact class to start, when you know it | `AddCustomerActivity.class` |
| **Extras** | Additional key–value data | `EXTRA_SUBJECT` → `"My Subject"` |

Set the action and data either in the constructor or with setters:

```java
Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+97022982000"));
// is the same as
Intent intent = new Intent();
intent.setAction(Intent.ACTION_DIAL);
intent.setData(Uri.parse("tel:+97022982000"));
```

### Explicit and implicit Intents

- An **explicit** Intent names the component to start. You use it inside your own app, where you know the class name: `new Intent(this, AddCustomerActivity.class)`.
- An **implicit** Intent only describes an action, and *any* installed app that can perform it may answer. "Show this location on a map" doesn't care whether Google Maps or another map app does it.

For an implicit Intent, Android performs **intent resolution**: it compares the Intent's action, data and category with the **intent filters** that other apps declare in their manifests. Then it starts the match, or shows a chooser if several apps match. If **no** app matches, `startActivity` throws `ActivityNotFoundException`, and an uncaught exception crashes your app. This lab catches it.

### User notifications

There are two ways to tell the user something:

- A **Toast** is a short message that pops up over the current screen and fades away by itself. The user doesn't interact with it.
- A **notification** appears in the status bar outside your app, and in the drawer the user pulls down. It stays until the user dismisses it, and tapping it can open your app. The system manages notifications, not your Activity.

Since Android 8.0 every notification belongs to a **channel**, such as "Messages" or "Downloads". Users can mute each channel separately in Settings. Since Android 13, an app must also ask the user's **permission** before posting notifications.

## Step 1: Prepare the emulator

The Maps and email buttons need those apps to be installed:

1. Use an emulator whose system image has the **Google Play** label (see [Setup Guide → Create an emulator](/setup#_2-create-an-emulator)). These images include Gmail and the Play Store.
2. If **Maps** isn't installed, open **Play Store** on the emulator, sign in with a Google account, and install **Google Maps**.
3. To actually send the test email, open **Gmail** on the emulator once and sign in.

Without these apps the buttons still work: they show "No app installed to handle this" instead of crashing.

## Step 2: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp3IntentsNotifications` |
| Package name | `edu.birzeit.encs5150.exp3` |
| Language | Java |
| Minimum SDK | API 24 |

::: tip No need for API 26
The old manual raised the minimum SDK to 26 because notification channels need Android 8.0. The code below uses `NotificationChannelCompat`, which does the right thing on every version, so the lab's usual **API 24** works.
:::

## Step 3: The layout

Open `res/layout/activity_main.xml`, switch to **Code** view, and replace **the whole file** with:

<<< @/snippets/exp3/activity_main.xml{xml}

It's a vertical `LinearLayout` with five full-width buttons. The IDs (`button_dial`, `button_gmail`, …) are what the Java code looks up.

## Step 4: Ask for the notification permission

Open `manifests/AndroidManifest.xml` and add this line **inside** `<manifest>`, **above** `<application>`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

This declares that the app *may* ask for the permission. On Android 13 and later the user still has to allow it at runtime, and the code in the next step handles that.

## Step 5: The code

Open `MainActivity.java` and replace the whole file with the code below. The template lines at the top of `onCreate` stay as they were.

<<< @/snippets/exp3/MainActivity.java{java}

Each button covers one part of the lab:

### Dial

`ACTION_DIAL` with a `tel:` URI opens the phone app with the number already typed in. The user still presses the call button, so this needs no permission. (`ACTION_CALL` places the call directly and does need the `CALL_PHONE` permission.)

### Email

`ACTION_SENDTO` with `mailto:` matches **only email apps**. The extras fill in the message:

- `EXTRA_EMAIL` is the list of recipients, and it **must be a `String[]`**. A single `String` is silently ignored. Put your own address there, send the email, and check that it arrives.
- `EXTRA_SUBJECT` and `EXTRA_TEXT` are the subject and body.

### Maps

`ACTION_VIEW` with a `geo:` URI opens a map app. `geo:latitude,longitude` centres the map. The `?q=Birzeit+University` part also searches for a place and drops a pin on it.

### Safe start

`startActivitySafely` wraps `startActivity` in `try` / `catch (ActivityNotFoundException e)`. An implicit Intent can always find no app to answer, for example on an emulator without Maps. Catching the exception turns that crash into a Toast.

### Notification

Posting a notification takes four pieces. See [Differences from the manual](#differences-from-the-manual) for how each one changed since the PDF:

1. **A channel.** `createNotificationChannel()` runs once in `onCreate`. Creating a channel that already exists does nothing, so calling it on every launch is safe.
2. **The permission check.** On Android 13+ (`TIRAMISU`), `createNotification` checks `POST_NOTIFICATIONS`. If it isn't granted yet, it launches the permission dialog and returns. When the user taps **Allow**, the `requestPermissionLauncher` callback calls `createNotification` again.
3. **A `PendingIntent`.** This wraps an ordinary Intent so the *system* can fire it later, when the user taps the notification. `FLAG_IMMUTABLE` is required on Android 12+.
4. **The builder.** `NotificationCompat.Builder` sets the icon, title, text and tap action. `setAutoCancel(true)` removes the notification once it's tapped. `notify(NOTIFICATION_ID, …)` posts it, and posting again with the same ID replaces it instead of adding a second one.

The small icon is `R.drawable.ic_launcher_foreground`, the Android head from the template. Status-bar icons are drawn as white silhouettes, so a flat shape works best. The colourful `mipmap/ic_launcher` used in the old manual can crash the system UI on Android 8.0.

### Toast

`Toast.makeText(context, text, duration).show()`. The duration is `Toast.LENGTH_SHORT` (about 2 s) or `Toast.LENGTH_LONG` (about 3.5 s). Forgetting `.show()` is the most common Toast bug.

## Differences from the manual

The 2019 manual's code was written for Android 8 and Android Studio 3.x. Several parts of it now crash, silently do nothing, or force a different minimum SDK. If you compare this page with the PDF, here is what changed and why.

### 1. The notification channel

::: code-group

```java [Manual]
private void createNotificationChannel() {
    int importance = NotificationManager.IMPORTANCE_DEFAULT;
    NotificationChannel channel = new NotificationChannel(MY_CHANNEL_ID,
            MY_CHANNEL_NAME, importance);
    NotificationManager notificationManager =
            getSystemService(NotificationManager.class);
    if (notificationManager != null) {
        notificationManager.createNotificationChannel(channel);
    }
}
```

```java [This guide]
private void createNotificationChannel() {
    NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
            MY_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(MY_CHANNEL_NAME)
            .build();
    NotificationManagerCompat.from(this).createNotificationChannel(channel);
}
```

:::

`NotificationChannel` only exists on Android 8.0 (API 26) and later. That is why the manual made you set the **minimum SDK to 26**. With minSdk 24, that code would crash on Android 7 with `NoClassDefFoundError`.

The `…Compat` classes from AndroidX check the Android version for you. On API 26+ they create the channel, and on API 24–25 (no channels) they do nothing. So the lab keeps the usual **minSdk 24**, and the code has no `if (Build.VERSION…)` check.

The manual also created the channel inside `createNotification`, every time the button was pressed. This guide creates it once in `onCreate`. Both work, because creating a channel that already exists does nothing, but the channel is set-up work rather than part of posting.

### 2. The notification permission (new)

::: code-group

```java [Manual]
// No permission handling: the notification was posted directly.
```

```java [This guide]
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    return;
}
```

:::

Android 13 (API 33, `TIRAMISU`) made notifications a **runtime permission**, like the camera or location. Without the user's permission, `notify()` is silently ignored. There is no crash, just no notification, which is very confusing to debug. Two parts are needed:

- `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` in the manifest (Step 4) declares that the app may ask.
- The check above actually asks. `requestPermissionLauncher` shows the system's *Allow notifications?* dialog, and when the user taps **Allow**, its callback calls `createNotification` again.

The version check matters: on Android 12 and older the permission doesn't exist and notifications are always allowed. `registerForActivityResult` is the current way to ask for permissions. It replaces the older `requestPermissions` / `onRequestPermissionsResult` pair you may find in tutorials.

### 3. The `PendingIntent` flag

::: code-group

```java [Manual]
PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
```

```java [This guide]
PendingIntent pendingIntent = PendingIntent.getActivity(
        this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
```

:::

A `PendingIntent` hands an Intent to *another* process (the system's notification UI) to fire later. Since Android 12 (API 31) you must say whether that process may change the Intent: `FLAG_IMMUTABLE` (it can't) or `FLAG_MUTABLE` (it can). Passing `0` throws an `IllegalArgumentException` the moment you press the button.

Use `FLAG_IMMUTABLE` unless you have a reason not to. Here the Intent only reopens `MainActivity`, so nothing needs to change it.

### 4. The small icon

::: code-group

```java [Manual]
.setSmallIcon(R.mipmap.ic_launcher)
```

```java [This guide]
.setSmallIcon(R.drawable.ic_launcher_foreground)
```

:::

Android draws status-bar icons as **white silhouettes**: it keeps only the shape (the alpha channel) and ignores the colours. `mipmap/ic_launcher` is the full-colour app icon, and on current templates it is an *adaptive icon* (a background layer plus a foreground layer). As a notification icon it shows up as a plain white square or circle, and on Android 8.0 an adaptive small icon can crash the System UI.

`drawable/ic_launcher_foreground` is just the Android-head vector from the template, so its silhouette is recognisable. In a real app you'd create a dedicated icon with **File → New → Image Asset → Notification Icons**.

### 5. Removing the notification after a tap (new)

```java
.setAutoCancel(true)
```

Without this, tapping the notification opens the app but the notification stays in the drawer until swiped away. This one line removes it on tap, which is what users expect.

### 6. The email Intent

::: code-group

```java [Manual]
gmailIntent.setAction(Intent.ACTION_SENDTO);
gmailIntent.setType("message/rfc822");
gmailIntent.setData(Uri.parse("mailto:"));
gmailIntent.putExtra(Intent.EXTRA_EMAIL, "rajaie.imseeh@gmail.com");
```

```java [This guide]
gmailIntent.setAction(Intent.ACTION_SENDTO);
gmailIntent.setData(Uri.parse("mailto:"));
gmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"your.email@example.com"});
```

:::

Two bugs here:

- **`EXTRA_EMAIL` must be a `String[]`**, because an email can have several recipients. Email apps read it with `getStringArrayExtra`, so a plain `String` is silently ignored and the **To** field stays empty.
- **`setType` did nothing.** `setData` and `setType` each clear the other, so the `setData` call that followed erased the type. With `ACTION_SENDTO`, the `mailto:` data alone is what restricts the choice to email apps, so `setType` is removed.

### 7. Starting other apps safely (new)

```java
try {
    startActivity(intent);
} catch (ActivityNotFoundException e) {
    Toast.makeText(this, "No app installed to handle this", Toast.LENGTH_SHORT).show();
}
```

The manual called `startActivity` directly. That is fine for explicit Intents, but with an implicit Intent there may be **no app** that can answer: an emulator without Maps, or a phone without an email account. The uncaught `ActivityNotFoundException` then crashes your app. The `startActivitySafely` helper is used by all three implicit-Intent buttons.

The older `if (intent.resolveActivity(getPackageManager()) != null)` check found in many tutorials no longer works reliably. Since Android 11, apps can't see other installed apps unless they declare them in a `<queries>` block, so that check returns `null` even when Maps *is* installed. Catching the exception avoids this problem.

### 8. Smaller changes

| Manual | This guide | Why |
| --- | --- | --- |
| `(Button) findViewById(R.id.button_dial)` | `findViewById(R.id.button_dial)` | `findViewById` is generic since API 26 / AppCompat, so the cast is no longer needed. |
| `geo:19.076,72.8777` (Mumbai) | `geo:31.9590,35.1820?q=Birzeit+University` | A familiar place. `?q=` also drops a labelled pin. |
| `tel:+9725` | `tel:+97022982000` | A complete phone number in international format. |
| Minimum SDK **26** | Minimum SDK **24** | Same as every other lab, thanks to `NotificationChannelCompat` (see 1). |
| Convert the layout in the Design view | Paste the full XML | Everyone gets an identical layout with the right IDs. |

## Run and check

- **Dial** opens the phone app with `+970 2 298 2000` typed in, without calling.
- **Send Email** opens Gmail (or a chooser) with the recipient, subject and body filled in. After sending, the email arrives in your inbox.
- **Open Maps** shows Birzeit University with a pin. Without a map app, a Toast says no app can handle it, and the app does not crash.
- **Show Notification** asks for permission the first time (Android 13+). After **Allow**, an icon appears in the status bar. Pulling down the drawer shows the title and body.
- Tapping the notification opens the app and removes the notification.
- Pressing **Show Notification** several times keeps **one** notification, not several.
- **Show Toast** shows "This is my toast message" at the bottom of the screen for about two seconds.

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| App crashes on **Open Maps** or **Send Email** with `ActivityNotFoundException` | No installed app can handle the Intent. Use `startActivitySafely`, and install the app on the emulator (Step 1). |
| Email opens with an empty **To** field | `EXTRA_EMAIL` was given a `String`. It must be `new String[]{"…"}`. |
| Email shows a list of every sharing app | You used `ACTION_SEND`. `ACTION_SENDTO` with `mailto:` limits it to email apps. |
| Nothing happens on **Show Notification** | Permission was denied, or the `<uses-permission>` line is missing from the manifest. Re-allow it in **Settings → Apps → Exp3IntentsNotifications → Notifications**. |
| Crash: `Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified` | The `PendingIntent` was created with flags `0`, as in the old manual. Pass `PendingIntent.FLAG_IMMUTABLE`. |
| `No Channel found for pkg=…` in Logcat and no notification | `createNotificationChannel()` was never called, or the channel ID in the builder doesn't match the channel's ID. |
| Notification icon is a white or grey square | The small icon is a full-colour image. Use a flat vector such as `ic_launcher_foreground`. |
| Toast never appears | `.show()` is missing. |

## Things to try

- Add an **EditText** for a phone number and dial whatever the user typed.
- Let the user type an address and open it in Maps with `geo:0,0?q=` followed by `Uri.encode(address)`.
- Change the channel's importance to `IMPORTANCE_HIGH`, then uninstall the app, run it again, and compare how the notification appears. A channel's importance can't change once it has been created.
- Add a button to the notification with `.addAction(…)`.
- Open a web page with `ACTION_VIEW` and an `https://` URI.
