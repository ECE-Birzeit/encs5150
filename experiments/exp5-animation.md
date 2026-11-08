---
title: "Frame Animation and Tween Animation"
short: "Frame and Tween Animation"
---
# Frame Animation and Tween Animation

In this lab you build one screen with two kinds of animation. At the top, two light bulbs are animated by swapping their **pictures**: one fades smoothly from off to on, and the other blinks frame by frame like a flip book. Below them, a **tween animation** rotates, scales, moves and fades an image, and finally sends it on a short tour around the screen.

## Objectives

- Play a frame-by-frame animation with an `<animation-list>` and `AnimationDrawable`.
- Cross-fade between two pictures with a `<transition>` and `TransitionDrawable`.
- Write tween animations in XML (`<rotate>`, `<scale>`, `<translate>`, `<alpha>`) and play them with `AnimationUtils` and `startAnimation`.
- Chain several animations in a `<set>` with `startOffset`, and react to an animation's start and end with an `AnimationListener`.
- Know what a tween animation does *not* change: the view's real position and size.

## Background

Android has two old, simple animation systems that work on every version. Both are defined in XML resource files.

### Drawable animations: changing the picture

A **drawable** is anything Android can draw: a PNG, a vector, a shape. Some drawables animate themselves. You put one in an `ImageView` like any other picture, then tell it to start.

| XML root element | Folder | Java class | What it does |
| --- | --- | --- | --- |
| `<animation-list>` | `res/drawable` | `AnimationDrawable` | Shows a list of pictures (*frames*) one after another, each for a set time. This is **frame animation**, the same idea as a cartoon or a flip book. |
| `<transition>` | `res/drawable` | `TransitionDrawable` | Holds exactly two pictures and cross-fades from the first to the second (`startTransition`) and back (`reverseTransition`). |

Frame animation can show anything you can draw, but every frame is a separate picture: smooth motion needs many frames, and many frames make a big app.

### Tween animations: moving the view

A **tween** animation ("in-be*tween*") takes a whole view and changes how it is drawn: you give the start and the end, and Android calculates every frame in between. There are four kinds, each with its own XML element in `res/anim`:

| Element | Changes | Main attributes |
| --- | --- | --- |
| `<translate>` | Position | `fromXDelta`, `toXDelta`, `fromYDelta`, `toYDelta` |
| `<rotate>` | Angle | `fromDegrees`, `toDegrees`, `pivotX`, `pivotY` |
| `<scale>` | Size | `fromXScale`, `toXScale`, `fromYScale`, `toYScale`, `pivotX`, `pivotY` |
| `<alpha>` | Transparency | `fromAlpha`, `toAlpha` (`0.0` invisible, `1.0` opaque) |

All of them also take `android:duration`, in milliseconds. Positions and pivots can be written three ways:

| Value | Meaning | Example |
| --- | --- | --- |
| A number | Pixels | `android:toXDelta="100"` moves 100 px right |
| A number with `%` | Percent of the **view itself** | `android:pivotX="50%"` is the view's centre |
| A number with `%p` | Percent of the view's **parent** | `android:toXDelta="30%p"` moves by 30 % of the parent's width |

The **pivot** is the point that stays fixed: a rotation turns around it, and a scale grows away from it. The default pivot is the view's top-left corner, so you almost always want `50%`.

In Java, an XML animation becomes an object of the class **`Animation`**. **`AnimationUtils.loadAnimation(context, R.anim.name)`** reads the XML file and creates that object, and **`view.startAnimation(animation)`** plays it on a view.

### Sets, timing and interpolators

- A **`<set>`** groups several animations. They all start together, unless you delay one with **`android:startOffset`** (milliseconds). Giving each one an offset equal to the end of the previous one plays them in sequence.
- An **interpolator** controls the speed during the animation. The default, `accelerate_decelerate_interpolator`, starts slowly, speeds up and slows down at the end. `linear_interpolator` keeps a constant speed.
- When an animation ends, the view snaps back to how it was before. **`android:fillAfter="true"`** keeps the last frame on screen instead.

::: warning Only the drawing moves
A tween animation changes where the view is **drawn**, not where it **is**. After moving a button with `<translate>` and `fillAfter`, the button appears in its new place, but you still have to tap its *old* place to click it. To really move or resize a view, use property animation (`view.animate()`), shown in [Things to try](#things-to-try).
:::

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup/#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp5Animations` |
| Package name | `edu.birzeit.encs5150.exp5` |
| Language | Java |
| Minimum SDK | API 24 |

## Step 2: Draw the bulb

Both bulbs need two pictures: off and on. Instead of downloading PNGs, you write them as **vector drawables**. A vector is a list of shapes in XML, so it is sharp at any size and on every screen density, and it takes a few hundred bytes.

1. In the Project window, right-click `res/drawable` → **New → Drawable Resource File**.
2. Set **File name** to `bulb_off`, set **Root element** to `vector`, and click **OK**.
3. Switch to **Code** view and replace the file's contents with:

<<< @/snippets/exp5/bulb_off.xml{xml}

Then create `bulb_on` the same way, with this content. It is the same bulb, only the glass is yellow:

<<< @/snippets/exp5/bulb_on.xml{xml}

`viewportWidth` and `viewportHeight` set up a 24 × 24 grid that `pathData` draws on, and `width` / `height` set the size the drawable has by default. Each `<path>` is one filled shape: `M` moves the pen, `h` / `v` / `c` draw lines and curves, and `z` closes the shape. Nobody writes path data by hand: these two paths are the "lightbulb" icon from **File → New → Vector Asset → Clip art**, split into glass and base so that the glass can be coloured on its own.

::: tip Preview
With a drawable file open, the **Split** view on the right shows what it looks like. Use it to check each file as you go.
:::

## Step 3: The animated drawables

Create two more drawable resource files in `res/drawable`, in the same way.

**`bulb_switch`** (root element `transition`). It stacks the two pictures, and starting the transition fades the second one in on top of the first:

<<< @/snippets/exp5/bulb_switch.xml{xml}

**`bulb_blink`** (root element `animation-list`). It is the frame animation: each `<item>` is one frame, shown for `duration` milliseconds. With `oneshot="false"`, after the last frame it starts again from the first:

<<< @/snippets/exp5/bulb_blink.xml{xml}

## Step 4: The tween animations

Tween animations live in their own resource folder, `res/anim`:

1. Right-click `res` → **New → Android Resource Directory**.
2. Set **Resource type** to `anim` (the directory name changes to `anim` too) and click **OK**.
3. Right-click the new `anim` folder → **New → Animation Resource File**. Create the five files below, each with its name as the file name, and replace each file's contents with the code.

`rotate.xml`:

<<< @/snippets/exp5/rotate.xml{xml}

`scale.xml`:

<<< @/snippets/exp5/scale.xml{xml}

`translate.xml`:

<<< @/snippets/exp5/translate.xml{xml}

`fade.xml`:

<<< @/snippets/exp5/fade.xml{xml}

`tour.xml` is a `<set>` of five animations played one after another:

<<< @/snippets/exp5/tour.xml{xml}

How the tour works:

1. **Timing.** Each part lasts 800 ms, and each `startOffset` is the moment the previous part ends: 0, 800, 1600, 2400, 3200. The whole tour takes 4 seconds.
2. **The path.** Inside a set, the effects **add up**. After the first part the image is 25 % to the right. The second moves it down, and it *stays* to the right. The third moves it 25 % left, back above where it started, and the fourth moves it back up. So the image goes around a square and ends where it began.
3. **`fillAfter` on the set** makes each part keep its final position after it ends. Without it, the image would jump back to the start after each part, instead of continuing from where the last part stopped. It is set once on the `<set>`, and the set passes it on to all its children.
4. **`linear_interpolator`** with **`shareInterpolator`** gives all five parts a constant speed, so the image doesn't slow down and speed up at every corner.

## Step 5: The layout

Open `res/layout/activity_main.xml`, switch to **Code** view, and replace **the whole file** with:

<<< @/snippets/exp5/activity_main.xml{xml}

The screen has three parts, top to bottom:

1. **Two columns**, each with a 96 dp bulb and a button under it. The left `ImageView` shows `@drawable/bulb_switch` and the right one `@drawable/bulb_blink`. An `ImageView` doesn't care whether its picture is a PNG, a vector or an animated drawable.
2. **The stage**, a `FrameLayout` with `layout_weight="1"`, so it takes all the space the other parts don't use. The tween image sits in its centre. It is the app's own launcher icon, `@mipmap/ic_launcher`, so you don't need another picture. Because the stage is the image's **parent**, every `%p` in the animations is a percentage of the stage's size.
3. **Five buttons** in two rows, one per tween animation.

## Step 6: The code

Open `MainActivity.java` and replace the whole file with the code below. The template lines at the top of `onCreate` stay as they were.

<<< @/snippets/exp5/MainActivity.java{java}

### Switch: `TransitionDrawable`

When Android loads the layout, it reads `bulb_switch.xml` and creates a `TransitionDrawable` for the `ImageView`. `getDrawable()` returns it typed as a plain `Drawable`, so the code **casts** it to `TransitionDrawable` to reach its methods.

- `startTransition(500)` fades from the first layer (off) to the second (on) in 500 ms. It always starts **from the first layer**, even if the bulb is already on.
- `reverseTransition(500)` plays the transition backwards, from wherever it is now. If you tap the button again in the middle of a fade, the fade simply turns around.

The `bulbOn` field remembers which way to go next, and the button's text says what the next tap will do.

### Blink: `AnimationDrawable`

The same way, `bulb_blink.xml` becomes an `AnimationDrawable`.

- `start()` begins showing the frames, and `isRunning()` tells whether it is playing.
- `stop()` freezes the animation on **whatever frame it is showing**, which could be "on". `selectDrawable(0)` then switches back to frame 0, the off bulb.

### Rotate, scale, move and fade

Each button does the same two things: `AnimationUtils.loadAnimation` reads the XML file from `res/anim` into an `Animation` object, and `startAnimation` plays it on the image. Starting a new animation on the same view replaces the old one immediately, so tapping **Rotate** during **Scale** stops the scaling.

`MainActivity.this` is the Activity. Inside an anonymous `OnClickListener`, a plain `this` would mean the listener, which is not a `Context`.

### Tour: `Animation` and `AnimationListener`

The tour is loaded once, before the listener, and kept in the variable `tour`, so that it can get an **`AnimationListener`**. Android calls the listener's three methods when the animation starts, ends and repeats.

Here, `onAnimationStart` disables all five tween buttons and `onAnimationEnd` enables them again, so the tour can't be interrupted. That matters: if another `startAnimation` replaced the tour halfway, Android would never call the tour's `onAnimationEnd`, and the buttons would stay disabled forever.

## Run and check

- The app opens with two grey bulbs and the launcher icon in the middle.
- **Switch on** fades the left bulb to yellow in half a second, and the button changes to **Switch off**. **Switch off** fades it back. Tapping twice quickly reverses the fade halfway.
- **Blink** makes the right bulb flash on and off until you tap **Stop**. After **Stop** the bulb is always off.
- **Rotate** turns the icon once in 3 seconds, around its centre.
- **Scale** grows the icon to 3 times its size in 2 seconds, then it snaps back to normal.
- **Move** slides the icon right for 3 seconds, then it jumps back to the centre.
- **Fade in** makes the icon disappear and fade back in over 1 second.
- **Tour** moves the icon right, down, left and up, then spins it once. During the tour all five tween buttons are greyed out, and they work again when it ends.

## Differences from the manual

The manual's animations work, but its "frame animation" app doesn't use frame animation, and its images aren't available to download. Here is what changed and why.

### 1. Frame animation is now real frame animation

::: code-group

```xml [Manual: drawable/animation.xml]
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/off" />
    <item android:drawable="@drawable/on" />
</transition>
```

```xml [This guide: drawable/bulb_blink.xml]
<animation-list xmlns:android="http://schemas.android.com/apk/res/android"
    android:oneshot="false">
    <item android:drawable="@drawable/bulb_off" android:duration="400" />
    <item android:drawable="@drawable/bulb_on" android:duration="400" />
</animation-list>
```

:::

The manual's introduction correctly says frame animation means "a sequence of images in order with an AnimationDrawable", but its app then builds a `<transition>`, which is a `TransitionDrawable`. A transition holds exactly two pictures and cross-fades between them once. It can't show a sequence of frames, and it doesn't loop.

This guide keeps the manual's transition, as the **Switch** bulb, because a light switch is exactly what it's good at. It adds the **Blink** bulb, an `<animation-list>` played by an `AnimationDrawable`, so the lab actually covers frame animation.

The manual also asks you to "try `reverseTransition`". In this guide the Switch button uses both methods, `startTransition` to switch on and `reverseTransition` to switch off, and [Step 6](#switch-transitiondrawable) explains the difference.

### 2. Vector drawables instead of PNGs

The manual asks you to import its two bulb PNGs and the tween image into `res/drawable`, but they exist only as figures inside the PDF. It also warns that you "should make sure to have images sized appropriately for different screen densities", and then uses one PNG for all of them.

This guide draws the bulbs as two small **vector drawables** (Step 2), which you paste as text and which are sharp on every screen. The tween image is the app's own launcher icon, which every new project already has.

### 3. One element per animation file

::: code-group

```xml [Manual: anim/rotate.xml]
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <rotate
        android:duration="3000"
        android:fromDegrees="0"
        android:pivotX="50%"
        android:pivotY="50%"
        android:toDegrees="360" />
</set>
```

```xml [This guide]
<rotate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="3000"
    android:fromDegrees="0"
    android:toDegrees="360"
    android:pivotX="50%"
    android:pivotY="50%" />
```

:::

The manual wraps every single animation in a `<set>`. That works, but a set is for *combining* animations, and a file can just as well hold one `<rotate>`, `<scale>`, `<translate>` or `<alpha>` as its root element. This guide uses a `<set>` only for the tour, where it actually combines five animations. The values are the manual's.

### 4. The combined animation finishes

The manual's second "try this" code moves the image along a square and then rotates it with:

```xml
android:interpolator="@android:anim/cycle_interpolator"
android:repeatCount="infinite"
```

`repeatCount="infinite"` means the animation **never ends**, so after pressing the button the image wobbles back and forth forever and only another animation stops it. `cycle_interpolator` is the reason it wobbles instead of turning: it plays the animation forwards and then backwards, like a sine wave.

The tour in this guide does the same square, then spins once and **ends**. That makes `onAnimationEnd` meaningful, which the code uses to enable the buttons again. Other changes:

- The manual moves by `75%p` and `70%p`, which only fits on screen if the image starts at the top-left corner, and the manual's layout doesn't put it there. Here the image starts in the centre of the stage and moves by `25%p`, so the square stays on screen.
- The manual sets `fillAfter="true"` on the set *and* on every child. Setting it on the set is enough, because the set passes it on to its children.
- The manual starts the first move after `startOffset="300"`, a pause before anything happens. The tour starts at 0.

### 5. One app instead of two

The manual builds a "Frame Animation Application" and a separate "Tween Animation Application". Each is a single screen with a couple of buttons, so this guide puts both on one screen and saves you a second project setup.

### 6. Smaller changes

| Manual | This guide | Why |
| --- | --- | --- |
| `(Button) findViewById(R.id.button)` | `findViewById(R.id.button_switch)` | `findViewById` is generic, so the cast is no longer needed. |
| `final ImageView imageView = …` | `ImageView switchImageView = …` | Since Java 8, a local variable that never changes can be used in an anonymous class without `final`. |
| `android:pivotY and android:pivotY` in the attribute list | `pivotX` and `pivotY` | A typo in the manual. |
| Layout built in the Design view from a component tree screenshot | Paste the full XML | Everyone gets an identical layout with the right IDs. |

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| `ClassCastException: … cannot be cast to android.graphics.drawable.AnimationDrawable` (or `TransitionDrawable`) | The `ImageView`'s `src` is not the drawable the code expects. `imageView_switch` must show `@drawable/bulb_switch`, and `imageView_blink` must show `@drawable/bulb_blink`. Also check the root element of each file. |
| `cannot find symbol: variable anim` in `R.anim.rotate` | The animation files are not in a folder named exactly `anim`, or the folder was created as a normal directory. Create it with **New → Android Resource Directory** and resource type `anim`. |
| `Element rotate is not allowed here` (or `vector`, `transition`…) | The file is in the wrong folder. Drawables (`vector`, `transition`, `animation-list`) go in `res/drawable`, tween animations (`rotate`, `scale`, `translate`, `alpha`, `set`) in `res/anim`. |
| Rotate or Scale grows from the top-left corner | `pivotX` / `pivotY` is missing. The default pivot is `0%`, the corner. Use `50%`. |
| The image jumps back at the end of an animation | Expected. Add `android:fillAfter="true"` to keep the last frame. |
| The tour moves only right, then jumps back | `fillAfter="true"` is missing on the `<set>`, so each part forgets the previous one when it ends. |
| All parts of the tour play at the same time | `startOffset` is missing. Without it, every animation in a set starts at 0. |
| The image looks cut off when scaled near the edge | A view is drawn only inside its parent. Keep the image away from the stage's edges, or scale it less. |
| The blinking bulb stops on "on" | `stop()` keeps the current frame. Call `selectDrawable(0)` after it. |
| A moved view can't be clicked where it appears | Tween animations move only the drawing. See [Only the drawing moves](#sets-timing-and-interpolators). |

## Things to try

- Add a third and fourth frame to `bulb_blink`, for example a dim orange bulb between off and on, and change the durations to make it flicker.
- Make the Switch button call only `reverseTransition`. Does it still switch on and off? Read what [`reverseTransition`](https://developer.android.com/reference/android/graphics/drawable/TransitionDrawable#reverseTransition(int)) does when no transition is running.
- Add `android:repeatCount="2"` and `android:repeatMode="reverse"` to `scale.xml`, so the image grows, shrinks and grows again.
- Try other interpolators on `translate.xml`: `@android:anim/bounce_interpolator`, `overshoot_interpolator`, `anticipate_interpolator`.
- Replace the Rotate button's code with **property animation**: `tweenImageView.animate().rotationBy(360).setDuration(3000);`. Then do the same with `translationXBy(200)`, and check that after the move the image is really in its new place (for example, give it a click listener).
- Make the tween image fade **out** and become invisible at the end, using `fillAfter`, then fade it back in with another button.
