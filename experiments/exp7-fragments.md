---
title: "Fragments"
short: "Fragments"
manual: "/ENCS515-Manual-2025.pdf#page=79"
slides: "/slides/exp7-fragments.pdf"
---
# Fragments

So far every screen you built was one Activity. That works on a phone, but on a tablet one screen can show a list and its details side by side. A **Fragment** is a reusable piece of screen with its own layout and its own code, which lives inside an Activity. One Activity can host several Fragments, and the same Fragment can be used in several Activities.

In this lab you build one screen with three Fragments. A green Fragment at the top has a button. Below it is an empty box, and five buttons add, remove, replace, detach and attach a Fragment in that box. Whichever Fragment is in the box shows how many times the green button was clicked, so the Fragments share data without knowing about each other. Every Fragment also writes its lifecycle to Logcat, so you can see what each operation does.

## Objectives

- Create Fragments and put one in an Activity's layout with `FragmentContainerView`.
- Add, remove, replace, detach and attach Fragments in code with a `FragmentTransaction`.
- Share data between Fragments with a `ViewModel` and `LiveData`.
- Read a Fragment's lifecycle callbacks in Logcat, and know what survives a screen rotation.

## Background

### What a Fragment is

A Fragment is a class that extends `androidx.fragment.app.Fragment` and has a layout. It is **not** a screen on its own: it always sits inside an Activity, and when the Activity is destroyed, its Fragments go with it.

```java
public class ButtonFragment extends Fragment {
    public ButtonFragment() {
        super(R.layout.fragment_button);   // the layout this Fragment shows
    }
}
```

There are two ways to put a Fragment in an Activity. This lab uses both:

| | Static | Dynamic |
| --- | --- | --- |
| How | A `FragmentContainerView` in the Activity's XML names the Fragment class with `android:name` | An empty `FragmentContainerView`, and Java code that runs a **transaction** to put a Fragment in it |
| Can be removed or swapped | No | Yes |
| In this lab | `ButtonFragment`, the green bar | `MessageFragment` and `NumberFragment`, in the box |

### Fragment transactions

The **`FragmentManager`** keeps track of the Fragments in an Activity. You get it with `getSupportFragmentManager()`, and you change its Fragments with a **transaction**: begin it, say what to do, and commit it.

```java
getSupportFragmentManager().beginTransaction()
        .add(R.id.fragment_container, new MessageFragment())
        .commit();
```

| Operation | What happens to the Fragment | Its view |
| --- | --- | --- |
| `add(container, fragment)` | Created and put in the container | Shown |
| `remove(fragment)` | Destroyed and taken out of the manager | Gone |
| `replace(container, fragment)` | The Fragment already in the container is removed, then this one is added | Swapped |
| `detach(fragment)` | Kept in the manager, but its **view** is destroyed | Gone |
| `attach(fragment)` | A detached Fragment gets a new view | Shown |

To get a Fragment back later, ask the manager for it by its container's id with `findFragmentById(R.id.fragment_container)`. You can also give a Fragment a name, a **tag**, when you add it (`add(container, fragment, "message")`) and find it with `findFragmentByTag("message")`.

### The lifecycle

A Fragment has a lifecycle like an Activity's, with extra steps, because its **view** can be destroyed and recreated while the Fragment object stays alive:

| When | Callbacks, in order |
| --- | --- |
| Added | `onAttach` → `onCreate` → `onCreateView` → `onViewCreated` → `onStart` → `onResume` |
| Detached | `onPause` → `onStop` → `onDestroyView` |
| Attached again | `onCreateView` → `onViewCreated` → `onStart` → `onResume` |
| Removed | `onPause` → `onStop` → `onDestroyView` → `onDestroy` → `onDetach` |

Code that uses the Fragment's views goes in **`onViewCreated`**, because the views exist only between `onViewCreated` and `onDestroyView`.

### Sharing data between Fragments

Fragments **should not call each other directly**. If `ButtonFragment` held a reference to `MessageFragment`, it would break as soon as the box held a `NumberFragment` instead, or nothing at all. Android has three ways for Fragments to share data:

| Way | How it works | Use it for |
| --- | --- | --- |
| **Shared `ViewModel`** (this lab) | Both Fragments get the **same** `ViewModel` object from their Activity. One changes the data, the others observe it. | Data several Fragments show, which should survive a rotation |
| Fragment Result API | One Fragment sends a `Bundle` with `setFragmentResult`, another receives it with `setFragmentResultListener` | Handing one result back once, such as a choice made in a dialog |
| Interface through the Activity (the manual) | The Fragment calls a method on its Activity, and the Activity calls a method on the other Fragment | Older code. The Activity has to hold both Fragments together. |

A **`ViewModel`** is a class that holds a screen's data. Android keeps it alive while its owner, here the Activity, is on screen, **including through rotations**, which destroy and recreate the Activity and its Fragments. Each Fragment asks for it with:

```java
CounterViewModel counter = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);
```

`requireActivity()` makes the Activity the owner. The first call creates the `ViewModel`, and every later call from any Fragment in that Activity returns the same object.

The data inside it is a **`LiveData`**, a value that others can **observe**:

```java
counter.getCount().observe(getViewLifecycleOwner(), count -> textView.setText(…));
```

The lambda runs at once with the current value, then again each time the value changes. `getViewLifecycleOwner()` ties the observer to the Fragment's **view**: when the view is destroyed, the observer is removed automatically, so a detached or removed Fragment never tries to update a view it no longer has.

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup/#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp7Fragments` |
| Package name | `edu.birzeit.encs5150.exp7` |
| Language | Java |
| Minimum SDK | API 24 |

`ViewModel`, `LiveData` and the Fragment classes all come with the template's `appcompat` library, so you don't add any dependencies.

## Step 2: The shared `ViewModel`

1. Right-click the package `edu.birzeit.encs5150.exp7` → **New → Java Class**.
2. Name it `CounterViewModel` and press <kbd>Enter</kbd>.
3. Replace the file's contents with:

<<< @/snippets/exp7/CounterViewModel.java{java}

- **`count`** is a `MutableLiveData<Integer>` that starts at `0`. Only this class can change it.
- **`getCount()`** returns it as a plain `LiveData`, which has no `setValue`, so the Fragments that show the count can observe it but can't change it.
- **`increment()`** is the only way to change it: it reads the current value, adds one, and sets it. `setValue` tells every observer.

## Step 3: A Fragment that logs its lifecycle

Create another Java class, `LoggedFragment`, and replace its contents with:

<<< @/snippets/exp7/LoggedFragment.java{java}

It overrides each lifecycle callback only to write the callback's name to Logcat, with the tag `FragmentLifecycle`, then calls `super`. The three Fragments in this lab extend it, so you get the log without writing it three times. It is `abstract` because it is a base class, never shown by itself.

## Step 4: Create the Fragments

1. Right-click the package → **New → Fragment → Fragment (Blank)**.
2. Name it `ButtonFragment` and click **Finish**.
3. Do the same for `MessageFragment` and `NumberFragment`.

Each time, the wizard creates a Java class and a layout (`fragment_button.xml`, `fragment_message.xml`, `fragment_number.xml`) with some example code. You will replace all six files.

## Step 5: The Fragment layouts

Replace **the whole** `res/layout/fragment_button.xml` with:

<<< @/snippets/exp7/fragment_button.xml{xml}

**The whole** `fragment_message.xml` with:

<<< @/snippets/exp7/fragment_message.xml{xml}

And **the whole** `fragment_number.xml` with:

<<< @/snippets/exp7/fragment_number.xml{xml}

The button Fragment is a green bar with `layout_height="wrap_content"`, so it takes only the height it needs. The other two fill the box (`match_parent`): a blue one with a sentence and an orange one with a big number. Their `TextView`s have no `android:text`, only `tools:text`, which shows in the Design view but not in the app, because the code sets the real text.

## Step 6: The Fragment code

Replace the whole `ButtonFragment.java` with:

<<< @/snippets/exp7/ButtonFragment.java{java}

1. **`extends LoggedFragment`** and **`super(R.layout.fragment_button)`**: the Fragment logs its lifecycle and shows `fragment_button.xml`. There is no `onCreateView` to write.
2. **`onViewCreated`** runs once the layout exists. It gets the shared `CounterViewModel` from the Activity, finds the button with `view.findViewById`, and makes it call `counter.increment()`.

`ButtonFragment` doesn't know which Fragment shows the count, or whether any does.

Replace the whole `MessageFragment.java` with:

<<< @/snippets/exp7/MessageFragment.java{java}

It gets the **same** `CounterViewModel` and observes `getCount()`. Each time the count changes, the lambda puts it in the sentence. Because the lambda also runs right away with the current value, a `MessageFragment` added after ten clicks shows "10 times" immediately.

Replace the whole `NumberFragment.java` with:

<<< @/snippets/exp7/NumberFragment.java{java}

It is the same as `MessageFragment`, but it shows only the number. `String.valueOf(count)` turns it into text. Never write `setText(count)` with an `int`: `setText` would read it as the id of a string resource and crash.

## Step 7: The Activity layout

Replace **the whole** `res/layout/activity_main.xml` with:

<<< @/snippets/exp7/activity_main.xml{xml}

From top to bottom:

- **`fragment_button`** is a static `FragmentContainerView`. Its `android:name` is the **full class name** of the Fragment, package included, and Android creates a `ButtonFragment` in it when the layout is inflated.
- **Two rows of buttons.** Each button has `layout_width="0dp"` and `layout_weight="1"`, so the buttons in a row share its width equally.
- **`fragment_container`** is a dynamic `FragmentContainerView`: it has no `android:name`, so it starts empty. `layout_height="0dp"` with `layout_weight="1"` makes it take the rest of the screen.

## Step 8: The Activity code

Open `MainActivity.java` and replace the whole file with the code below. The template lines at the top of `onCreate` stay as they were.

<<< @/snippets/exp7/MainActivity.java{java}

1. **`fragmentManager`** is `getSupportFragmentManager()`, the manager for this Activity.
2. **Each button runs one method**, and each method is one transaction: `beginTransaction()`, one operation, `commit()`.
3. **`findCurrentOrWarn`** asks the manager for the Fragment in the box with `findFragmentById(R.id.fragment_container)`. This finds a detached Fragment too, since it is still in the manager. If the box is empty, it shows a Toast and returns `null`, and the caller does nothing.
4. **`addFragment`** creates a new `MessageFragment` each time, but only if the box is empty.
5. **`replaceFragment`** looks at the Fragment in the box: a `MessageFragment` is replaced by a new `NumberFragment`, and the other way round. It refuses to replace a detached Fragment, because `replace` only removes the Fragments that are attached, and the detached one would stay behind in the manager.

The Activity only moves Fragments around. It plays no part in passing the count, which goes through the `ViewModel`.

## Run and check

Open **Logcat** in Android Studio and type `FragmentLifecycle` in the search box. When the app starts, `ButtonFragment` logs `onAttach` through `onResume`, and the box is empty.

- Press **Remove**, **Replace**, **Detach** or **Attach** while the box is empty. A Toast says "Add a fragment first".
- Press **Click me** three times, then **Add**. The blue box appears and says "The button was clicked 3 times": the new Fragment read the current count the moment it started observing.
- Press **Add** again. A Toast says "The box already has a fragment".
- Press **Click me** a few more times. The blue text follows each click.

Now go through the table and compare Logcat with [the lifecycle](#the-lifecycle):

| Press | On screen | Logcat |
| --- | --- | --- |
| **Detach** | The blue box disappears | `MessageFragment: onPause`, `onStop`, `onDestroyView` |
| **Click me** twice | Nothing changes | Nothing: the detached Fragment has no view, so it has no observer |
| **Attach** | The blue box is back, with the count **including** the two clicks | `onCreateView`, `onViewCreated`, `onStart`, `onResume`. No `onAttach` or `onCreate`: the Fragment was never destroyed. |
| **Replace** | The orange box shows the same count as a number | The old Fragment stops (`onPause`, `onStop`), the new one is created (`onAttach` … `onStart`), then the old one is destroyed (`onDestroyView`, `onDestroy`, `onDetach`) and the new one resumes (`onResume`) |
| **Replace** again | Back to the blue sentence | The same, with the two Fragments swapped |
| **Remove** | The box is empty | `onPause`, `onStop`, `onDestroyView`, `onDestroy`, `onDetach` |
| **Add** | The blue box, still with the right count | A brand-new `MessageFragment`: `onAttach` … `onResume` |

**Detach** and **Remove** look the same on screen, but Logcat shows the difference. After **Detach**, the Fragment object is still alive (`onDestroy` was not called), and **Attach** only gives it a new view. After **Remove**, it is gone, and **Add** creates a new one.

Finally, with a Fragment in the box, **rotate the emulator** (the rotate buttons in the emulator's toolbar). Logcat shows every Fragment destroyed and then created again, because rotating recreates the Activity. The `FragmentManager` restores the Fragment that was in the box, and the count is unchanged, because the `ViewModel` survives the rotation.

## Differences from the manual

The manual builds two apps, connects its Fragments with an interface through the Activity, and uses several APIs that Android Studio now marks as deprecated. Here is what changed and why.

### 1. One app instead of two

The manual builds a "Fragments Communication Application" with two static Fragments, and a separate "Fragments Transaction Application" that adds Fragments in code. This guide puts both on one screen: the static `ButtonFragment` sends the count, and the Fragments you add, replace and remove show it. That saves a second project setup. It also shows communication working with a Fragment that comes and goes, which the manual's apps never try.

### 2. A shared `ViewModel` instead of an interface

::: code-group

```java [Manual]
// SecondFragment
interface communicator {
    public void respond(String data);
}

// MainActivity implements SecondFragment.communicator
@Override
public void respond(String data) {
    SecondFragment secondFragment =
            (SecondFragment) getSupportFragmentManager().findFragmentById(R.id.fragment2);
    secondFragment.changeData(data);
}

// FirstFragment
final SecondFragment.communicator communicator =
        (SecondFragment.communicator) getActivity();
… communicator.respond("the button is clicked " + i[0] + " times");
```

```java [This guide]
// ButtonFragment
counter.increment();

// MessageFragment and NumberFragment
counter.getCount().observe(getViewLifecycleOwner(), count -> …);
```

:::

The manual's interface works, but it is the older pattern. Android's current guide to Fragment communication recommends a shared `ViewModel` for data like this, and the Fragment Result API for one-off results. For this app the `ViewModel` is better:

- **The Activity is no longer the middleman.** In the manual, `MainActivity` has to implement the interface, find the second Fragment, cast it and call it. Here it does none of that.
- **The receiving Fragment doesn't have to exist.** The manual's `respond` assumes `SecondFragment` is on screen. If it had been removed, `findFragmentById` would return `null` and the app would crash. Here, a Fragment that isn't there simply isn't observing, and one added later reads the current count.
- **The count survives rotation.** The manual keeps the count in a local `final int[] i` in the Fragment, so it starts again from 0 when the screen rotates. The `ViewModel` keeps it.
- **The data has one owner.** The manual sends a ready-made sentence, so the receiver can't show the count any other way. Here the `ViewModel` holds the number, and each Fragment decides how to show it.

The manual's interface is also defined inside `SecondFragment`, the Fragment that **receives** the data, although `FirstFragment` calls it, and it is named `communicator` in lower case, against Java's naming convention.

### 3. `FragmentContainerView` instead of `<fragment>`

::: code-group

```xml [Manual]
<fragment
    android:id="@+id/fragment1"
    android:name="….FirstFragment" … />
```

```xml [This guide]
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/fragment_button"
    android:name="edu.birzeit.encs5150.exp7.ButtonFragment" … />
```

:::

The manual drags a `<fragment>` tag from the palette. Android Studio now flags that tag and recommends `FragmentContainerView`, which handles Fragment transactions and animations correctly. It is also the right container for Fragments added in code, so this guide uses it for the box too, where the manual adds Fragments to its root `LinearLayout`.

### 4. `onViewCreated` instead of `onActivityCreated`

::: code-group

```java [Manual]
@Override
public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    …
    Button button = (Button) getActivity().findViewById(R.id.button);
```

```java [This guide]
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    …
    Button countButton = view.findViewById(R.id.button_count);
```

:::

`onActivityCreated` is deprecated: a Fragment shouldn't depend on when its Activity finishes `onCreate`. Code that sets up the Fragment's views now goes in `onViewCreated`, which gets the Fragment's own view as a parameter.

The manual also finds views with `getActivity().findViewById`, which searches the **whole Activity's layout**. If two Fragments had a view with the same id, it would find whichever comes first, and it returns `null` if the Fragment's view isn't on screen. `view.findViewById` searches only this Fragment's layout.

### 5. A new Fragment for each `add`

::: code-group

```java [Manual]
final FirstFragment firstFragment = new FirstFragment();   // created once, in onCreate

fragmentTransaction.add(R.id.root_layout, firstFragment, "FristFrag");
```

```java [This guide]
fragmentManager.beginTransaction()
        .add(R.id.fragment_container, new MessageFragment())
        .commit();
// later: fragmentManager.findFragmentById(R.id.fragment_container)
```

:::

The manual creates one Fragment object in `onCreate` and keeps it for every **Add**. Adding that same object a second time throws `IllegalStateException: Fragment already added`. It also goes wrong after a rotation: Android restores the Fragments the manager had, but the new Activity creates another object, so its variable no longer points at the Fragment on screen. This guide creates a new Fragment for each `add` and always asks the manager for the one that is really in the box.

### 6. All five operations, and a log

The manual shows only **add** and leaves "remove, attach, detach and replace" to you, without explaining how they differ. This guide does all five, and the lifecycle log shows what each one does to the Fragment.

### 7. Smaller changes

| Manual | This guide | Why |
| --- | --- | --- |
| `FirstFragment`, `SecondFragment` | `ButtonFragment`, `MessageFragment`, `NumberFragment` | Each name says what the Fragment shows. |
| Override `onCreateView` (what the wizard generates) | `super(R.layout.fragment_button)` in the constructor | One line instead of a method. |
| `final int[] i = {0}` to count clicks | `count` in `CounterViewModel` | The array was a workaround to change a number inside an anonymous class, and lost the count on rotation. |
| `new View.OnClickListener() { … }` | A lambda, `v -> …` | Shorter. |
| `getFragmentManager()` in the text | `getSupportFragmentManager()` | `getFragmentManager()` is deprecated. The manual's own code already uses the support one. |
| `@android:color/holo_green_dark` and `holo_blue_light` | `#2E7D32`, `#0277BD`, `#EF6C00` | Colours that are readable with white text. |
| Layouts built from screenshots | Paste the full XML | Everyone gets an identical layout with the right ids. |

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| The box never shows the clicks: it stays at 0 | The Fragments use `new ViewModelProvider(this)`. In a Fragment, `this` makes the **Fragment** the owner, so each Fragment gets its own `CounterViewModel`. Use `requireActivity()` so they share the Activity's. |
| `InflateException` with `ClassNotFoundException` for `ButtonFragment` | The `android:name` in `activity_main.xml` isn't the full class name. It needs the package: `edu.birzeit.encs5150.exp7.ButtonFragment`. |
| `IllegalArgumentException: No view found for id … for fragment MessageFragment` | The transaction uses an id that is not in `activity_main.xml`. Use `R.id.fragment_container`. |
| `IllegalStateException: Fragment already added` | The same Fragment object was added twice. Create it with `new` for each `add`. |
| `Resources$NotFoundException: String resource ID #0x3` | `setText` was given an `int`. Turn the number into text first: `String.valueOf(count)`. |
| Android Studio underlines `observe(this, …)` in a Fragment | Observe with `getViewLifecycleOwner()`, not `this`. With `this`, the observer outlives a detached Fragment's view, and each **Attach** adds another one. |
| `NullPointerException` in `increment` | `count` was created with `new MutableLiveData<>()`, which starts as `null`. Give it a starting value: `new MutableLiveData<>(0)`. |
| Logcat shows nothing | Type `FragmentLifecycle` in Logcat's search box, and make sure the log level isn't set above **Debug**. |
| A button does nothing | Its `findViewById(...).setOnClickListener` line in `MainActivity` is missing, or uses the wrong `R.id`. |

## Things to try

- Add `.addToBackStack(null)` before `.commit()` in `replaceFragment`. Now the system **Back** button undoes a replace instead of closing the app.
- Use `hide` and `show` instead of `detach` and `attach`, and compare the lifecycle in Logcat. Which callbacks run?
- Add a **Reset** button to `NumberFragment` that calls a new `reset()` method in `CounterViewModel`. The `MessageFragment` doesn't need to change.
- Make a tablet layout: copy `activity_main.xml` into a new `res/layout-sw600dp/` folder and put the box beside the button bar instead of under it. Run it on a tablet emulator.
- Rewrite the communication with the Fragment Result API: `ButtonFragment` calls `getParentFragmentManager().setFragmentResult(…)` and `MessageFragment` listens with `setFragmentResultListener`. What happens to the count when you rotate, or when you add the Fragment after a few clicks?
