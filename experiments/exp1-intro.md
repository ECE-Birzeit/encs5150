# Exp 1: Introduction to Android Programming

In this lab you create your first Android project, look at the parts every Android app is made of, and build a small app that reads your name from a text field and greets you when you tap a button. Then you run it on the emulator and on a real phone.

## Objectives

- Install Android Studio and create an emulator.
- Create a new Android project and understand its structure.
- Design a simple screen in XML and connect it to Java code.
- Run the app on the Android emulator and on a physical device.

::: tip Before the lab
Finish the [Setup Guide](/setup) at home. Installing Android Studio and downloading an emulator image takes too long to do in the lab.
:::

## Background

### App components

An Android app is built from four kinds of components. You will meet all of them during the course:

| Component | What it is | Example |
| --- | --- | --- |
| **Activity** | One screen with a user interface. Most labs are about Activities. | A login screen |
| **Service** | Work done in the background, with no UI. | Downloading new email |
| **Broadcast Receiver** | Reacts to system-wide announcements. | Doing something when Wi-Fi connects |
| **Content Provider** | Shares an app's data with other apps. | The Contacts app sharing phone numbers |

### The manifest

Every app has an `AndroidManifest.xml`. It tells Android what the app contains: its components (every Activity must be listed here), the permissions it needs (such as Internet access), and which Activity opens when the user taps the app icon. Android Studio adds Activities to the manifest for you when you create them with the wizard.

### Layouts, IDs and the `R` class

A screen has two halves:

- **The layout**, an XML file in `res/layout/`, describes *what* is on screen. Each view you want to use from code gets an ID, e.g. `android:id="@+id/button_Greet"`.
- **The Activity**, a Java class, describes *what happens*. It loads the layout with `setContentView(R.layout.activity_main)` and finds views with `findViewById(R.id.button_Greet)`.

`R` is a class that the build generates from your resources. Every layout, ID, string and image gets a constant in it, such as `R.layout.activity_main` and `R.id.button_Greet`. You never edit `R` yourself. If `R` shows up red, a resource file usually has an error.

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup#_3-create-a-lab-project), with these values:

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp1Hello` |
| Package name | `edu.birzeit.encs5150.exp1` |
| Language | Java |
| Minimum SDK | API 24 |

Wait for Gradle sync to finish. Then press **Run ▶**. The emulator should show a white screen with **Hello World!** in the middle. If that works, your setup is complete.

## Step 2: Explore the project

With the Project window in **Android** view, open and look at:

1. **`manifests/AndroidManifest.xml`**: find the `<activity android:name=".MainActivity">` element and its `MAIN` / `LAUNCHER` intent filter. That filter is what makes `MainActivity` the screen that opens first.
2. **`res/layout/activity_main.xml`**: the layout editor opens. Use the **Code / Split / Design** buttons in the top-right corner to switch between the XML and the visual preview.
3. **`java/edu.birzeit.encs5150.exp1/MainActivity.java`**: the code that shows that layout.

## Step 3: Design the layout

The template layout is a `ConstraintLayout` with one `TextView`. Replace it with a vertical `LinearLayout` holding three views: a text field, a button and a label.

Open `activity_main.xml`, switch to **Code** view, and replace **the whole file** with:

<<< @/snippets/exp1/activity_main.xml{xml}

Switch to **Design** or **Split** to see the result.

Things to notice:

- `android:orientation="vertical"` stacks the children from top to bottom.
- `match_parent` means "as wide as my parent". `wrap_content` means "just big enough for my content".
- `android:hint` is the grey placeholder text shown while the field is empty.
- The root keeps `android:id="@+id/main"` because the template's Java code looks for it.
- Android Studio highlights `android:text="Say Hello"` with a *Hardcoded string* warning. Real apps put text in `res/values/strings.xml` so it can be translated. For the labs, hardcoded text is fine.

::: details Doing it in the Design view instead
You can also build the same screen by dragging from the **Palette**: right-click the root in the **Component Tree** and choose **Convert view… → LinearLayout**, set *orientation* to *vertical*, then drag in a **Plain Text**, a **Button** and a **TextView**. Set each one's **id** in the **Attributes** panel to match the XML above. Pasting the XML is faster and gives everyone the same result.
:::

## Step 4: Add the code

Open `MainActivity.java` and replace **the whole file** with the code below. The template lines at the top of `onCreate` are kept as they are. Only the part after them is new.

<<< @/snippets/exp1/MainActivity.java{java}

How it works:

1. `findViewById` returns the view object for each ID from the layout. No cast is needed: the return type is worked out from the variable you assign it to.
2. `setOnClickListener` registers an **event listener**. Android calls its `onClick` method every time the button is tapped.
3. Inside `onClick` we read the text, then either show an error on the field with `setError` or put a greeting into the `TextView` with `setText`.

::: tip Lambdas
Since `View.OnClickListener` has a single method, you can also write the listener as a lambda:
```java
greetButton.setOnClickListener(view -> greetingTextView.setText("Hello!"));
```
The guides use the full form so that it is clear which method you are implementing.
:::

## Step 5: Run it

### On the emulator

Select your emulator in the toolbar and press **Run ▶**.

### On a phone

Enable **USB debugging** as described in [Setup Guide → Using a physical phone](/setup#using-a-physical-phone-optional), plug the phone in, select it in the toolbar, and press **Run ▶**. The app stays installed on the phone afterwards.

## Run and check

- The app starts with an empty text field, a **Say Hello** button and no greeting.
- Tapping **Say Hello** with an empty field shows a red error icon and "Please enter your name".
- Typing `Sara` and tapping the button shows **Hello, Sara!**
- Rotating the emulator (the rotate buttons on its side toolbar) still shows the layout correctly.
- The app also runs on a physical phone.

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| `Cannot resolve symbol 'R'` | An XML file has an error. Check the **Problems** or **Build** window, fix the XML, then **Build → Rebuild Project**. |
| `Cannot resolve symbol 'editText_Name'` | The ID in the Java code doesn't match the one in the layout. IDs are case-sensitive. |
| App crashes on launch with `NullPointerException` at `setOnApplyWindowInsetsListener` | The layout's root lost `android:id="@+id/main"`. Add it back. |
| `MainActivity` extends `ComponentActivity` / project is in Kotlin | You chose **Empty Activity**. Recreate the project with **Empty Views Activity**. |
| The phone is not listed | Accept the **Allow USB debugging?** prompt on the phone. Try another cable, as some cables only charge. |

## Things to try

- Add a second button, **Clear**, that empties both the text field and the greeting.
- Change the greeting's colour with `android:textColor` in the XML.
- Show the greeting in a `Toast` instead of the `TextView`. Look up `Toast.makeText`.
