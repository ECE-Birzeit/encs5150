---
title: "Singleton and Shared Preferences"
short: "Singleton and Shared Preferences"
manual: "/ENCS515-Manual-2025.pdf#page=69"
---
# Singleton and Shared Preferences

In [SQLite Database](./exp4-sqlite) you saved a table of customers. Many apps also need to remember a few single values: the last username, whether a box was ticked, how many times something happened. A whole database is too much for that. Android's **shared preferences** keep such values as key–value pairs in a small file.

In this lab you build two screens. The first saves a username, a password and a "Remember password" checkbox, and counts how many times you saved. The second reads the values back. Both screens use the file through one **singleton** class, `SharedPrefManager`.

## Objectives

- Write a singleton class in Java: a private constructor and a static `getInstance` method.
- Save and read `String`, `int` and `boolean` values with `SharedPreferences`, and delete one with `remove`.
- Know the difference between `apply()` and `commit()`.
- Fill a form with the values saved last time.
- Find the preferences file on the device, and know what must never be stored in it.
- Build a layout with `ConstraintLayout`.

## Background

### The singleton pattern

A **singleton** is a class that can only ever have **one object**. Every part of the program that asks for it gets that same object. It suits things the whole app shares, such as a settings file or a database connection.

A singleton has three parts:

```java
public class SharedPrefManager {
    private static SharedPrefManager instance = null;   // 1. the one object

    private SharedPrefManager(Context context) { … }    // 2. nobody else can call new

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {                          // 3. create it on the first call only
            instance = new SharedPrefManager(context);
        }
        return instance;
    }
}
```

1. A **`static` field** holds the object. A static field belongs to the class, not to any object, so there is one for the whole app.
2. The **constructor is `private`**. `new SharedPrefManager(…)` outside the class is a compile error, so no second object can be made.
3. **`getInstance`** is the only way to get the object. The first call finds `instance` is `null` and creates it, and every later call returns the same one. Creating the object only when it is first needed is called **lazy initialization**.

`synchronized` lets only one thread run `getInstance` at a time. Without it, two threads calling it at the same moment could both see `null` and create two objects.

### Shared preferences

A `SharedPreferences` file stores **key–value pairs**, like a `HashMap` that survives closing the app. Each value has a `String` key, and the value can be a `String`, `int`, `long`, `float`, `boolean` or `Set<String>`.

You get the file with `context.getSharedPreferences(name, Context.MODE_PRIVATE)`. If no file with that name exists yet, Android creates it on the first write. It is an ordinary XML file in the app's private folder:

```
/data/data/<package>/shared_prefs/<name>.xml
```

**Reading** takes a key and a **default value**, which is returned if the key was never saved:

```java
String username = sharedPreferences.getString("username", "");
int count = sharedPreferences.getInt("save_count", 0);
```

**Writing** goes through an **`Editor`**. You collect one or more changes, then save them all at once:

```java
sharedPreferences.edit()
        .putString("username", "ahmad")
        .putInt("save_count", 3)
        .apply();
```

| Method | Saves | Returns |
| --- | --- | --- |
| `apply()` | Updates the values in memory at once, and writes the file in the background | Nothing |
| `commit()` | Writes the file **now**, and waits until it is done | `true` if it worked |

Reads see the new values immediately with both. Prefer `apply()`: `commit()` makes the screen wait for the disk, which can make the app stutter.

### Preferences or SQLite?

| | Shared preferences | SQLite ([Exp 4](./exp4-sqlite)) |
| --- | --- | --- |
| Holds | A few single values | Many rows of the same shape |
| Example | Last username, a setting, a counter | A list of customers |
| Look up by | Key | Any `WHERE` condition |

::: danger Never store passwords
The preferences file is plain text. Anyone with a rooted phone, a backup of it, or a debug build of your app can read it, as you will in [Look inside the file](#look-inside-the-file). This lab saves a password only to show you that. A real app never stores the user's password: it sends it to the server once, and stores the **token** the server sends back.
:::

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup/#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp6SharedPreferences` |
| Package name | `edu.birzeit.encs5150.exp6` |
| Language | Java |
| Minimum SDK | API 24 |

Then add the second screen: right-click the package `edu.birzeit.encs5150.exp6` → **New → Activity → Empty Views Activity**, name it `SecondActivity`, and click **Finish**. The wizard creates `SecondActivity.java`, `activity_second.xml`, and its entry in the manifest.

## Step 2: The singleton

1. Right-click the package `edu.birzeit.encs5150.exp6` → **New → Java Class**.
2. Name it `SharedPrefManager` and press <kbd>Enter</kbd>.
3. Replace the file's contents with:

<<< @/snippets/exp6/SharedPrefManager.java{java}

How it works:

1. **The constants.** `SHARED_PREF_NAME` is the file name. The four `KEY_…` constants are the keys. They are `public` so both Activities use them, and typing `KEY_USERNAM` is a compile error, while typing `"usernam"` would quietly read a key that doesn't exist.
2. **The singleton**: the `static instance` field, the `private` constructor and `getInstance`, as in [Background](#the-singleton-pattern). The constructor opens the file once, and the object keeps the `SharedPreferences` in a field.
3. **One `write…` and one `read…` method per type.** Each write starts a new `Editor` with `edit()`, puts one value, and saves it with `apply()`. Each read passes the key and the default straight to `getString`, `getInt` or `getBoolean`.
4. **`remove`** deletes a key from the file. After it, reading that key returns the default again.

## Step 3: The layouts

Both screens use `ConstraintLayout`, the template's default. In a `ConstraintLayout`, every view says where it goes with **constraints**, rules that attach its sides to the parent or to another view:

| Attribute | Meaning |
| --- | --- |
| `app:layout_constraintTop_toTopOf="parent"` | My top edge is at the top of the screen. |
| `app:layout_constraintTop_toBottomOf="@id/editText_username"` | My top edge is just below the username field. |
| `app:layout_constraintStart_toStartOf="parent"` and `…End_toEndOf="parent"` | My left and right edges are at the screen's edges. |
| `android:layout_width="0dp"` | "Match constraints": stretch between the two sides I'm attached to. |

Margins set the gap. Each view needs at least one horizontal and one vertical constraint, otherwise Android Studio warns that it will jump to the top-left corner when the app runs.

Open `res/layout/activity_main.xml`, switch to **Code** view, and replace **the whole file** with:

<<< @/snippets/exp6/activity_main.xml{xml}

Each view is constrained below the previous one, so the screen reads top to bottom: a title, the two text fields, the checkbox and two buttons. `inputType="textPassword"` hides the password as dots.

Then replace **the whole** `res/layout/activity_second.xml` with:

<<< @/snippets/exp6/activity_second.xml{xml}

It has the same structure: three `TextView`s for the values, and **Load** and **Back** buttons.

::: tip Look at the constraints
Switch a layout to **Design** view and click a view. The blueprint shows its constraints as arrows to the parent or to the view above. Drag a view's top circle to another view to make a constraint yourself.
:::

## Step 4: The first screen

Open `MainActivity.java` and replace the whole file with the code below. The template lines at the top of `onCreate` stay as they were.

<<< @/snippets/exp6/MainActivity.java{java}

1. **`getInstance(this)`** gets the singleton. On the first launch this call creates it.
2. **Filling the form.** Before the user types anything, the code reads the saved username and checkbox, and puts them back on screen. On the very first launch nothing is saved, so the defaults `""` and `false` are used. The password is filled in only if "Remember password" was ticked.
3. **Save** checks that the username isn't empty, then writes one value of each type:
   - a `String`: the username;
   - a `boolean`: whether the box is ticked. If it is, the password is saved too. If not, `remove` deletes any password saved earlier, so unticking the box really forgets it;
   - an `int`: the save counter. A preference can't add one to itself, so the code reads the old value, adds one and writes it back.
4. **Go to second screen** starts `SecondActivity` and leaves `MainActivity` on the back stack, as in [Android Layouts](./exp2-layouts).

## Step 5: The second screen

Open `SecondActivity.java` and replace the whole file with:

<<< @/snippets/exp6/SecondActivity.java{java}

- **`getInstance(this)`** returns the **same object** `MainActivity` created: `instance` is no longer `null`, so no new one is made.
- **Load** reads the three values and shows them. The defaults (`"(not saved)"` and `0`) show up for keys that were never saved, or that `remove` deleted.
- **Back** calls `finish()`. `MainActivity` is still underneath, so it comes back as it was.

## Run and check

- On the first launch the form is empty. Pressing **Save** with no username shows "Please enter a username" and saves nothing.
- Enter a username and a password, tick **Remember password** and press **Save**. A Toast says "Saved".
- **Go to second screen** → **Load** shows the username, the password and "Saved: 1 times". Press **Save** again and **Load** again, and the counter goes up.
- **Back** returns to the form with everything still typed in.
- **Close the app completely** (swipe it away in recent apps, or press **Stop** in Android Studio) and open it again. The username, the password and the ticked box are filled in.
- Untick **Remember password**, press **Save**, and restart the app. The username is still there, but the password field is empty, and **Load** shows "Password: (not saved)".

### Look inside the file

While the app is running from Android Studio, open **View → Tool Windows → Device Explorer** and go to `/data/data/edu.birzeit.encs5150.exp6/shared_prefs/`. Double-click `exp6_prefs.xml`. With "Remember password" ticked, it looks like this:

```xml
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="remember_password" value="true" />
    <string name="password">secret123</string>
    <int name="save_count" value="2" />
    <string name="username">ahmad</string>
</map>
```

Every value is there in plain text, the password included. This is why passwords don't belong in preferences. The order of the entries doesn't matter: the file is a map, looked up by key.

## Differences from the manual

The manual's singleton works, but it mixes `static` and non-`static` fields, saves with `commit()`, spells the keys out in each Activity, and lists file modes that now crash. It also saves only strings, although its objectives mention integers and booleans. Here is what changed and why.

### 1. The singleton's fields

::: code-group

```java [Manual]
private static SharedPrefManager ourInstance = null;
private static SharedPreferences sharedPreferences = null;
private SharedPreferences.Editor editor = null;
```

```java [This guide]
private static SharedPrefManager instance = null;

private final SharedPreferences sharedPreferences;
```

:::

In the manual, `sharedPreferences` is `static` but `editor` is not. Only the *instance* should be static: everything else belongs to that one object, like the fields of any other class. `final` says the field is set once, in the constructor.

The manual also keeps one `Editor` for the life of the app and reuses it for every write. That works, but an editor is meant to be short-lived: you open one with `edit()`, put your changes, and save. This guide opens a new editor for each write.

### 2. `apply()` instead of `commit()`

::: code-group

```java [Manual]
public boolean writeString(String key, String value) {
    editor.putString(key, value);
    return editor.commit();
}
```

```java [This guide]
public void writeString(String key, String value) {
    sharedPreferences.edit().putString(key, value).apply();
}
```

:::

`commit()` writes the file to storage before it returns, so the app freezes until the disk is done. `apply()` updates the values in memory immediately and writes the file in the background. The manual's Activities ignore the `boolean` that `commit()` returns, so nothing is lost by switching.

### 3. Keys are constants

::: code-group

```java [Manual]
// MainActivity
sharedPrefManager.writeString("userName", …);
// SecondActivity
sharedPrefManager.readString("userName", "noValue");
```

```java [This guide]
// MainActivity
sharedPrefManager.writeString(SharedPrefManager.KEY_USERNAME, username);
// SecondActivity
sharedPrefManager.readString(SharedPrefManager.KEY_USERNAME, "(not saved)");
```

:::

The manual types each key as a string in both Activities, and its own text even calls the key `"username"` while the code uses `"userName"`. Keys are case-sensitive, so a key spelled differently in the two places reads nothing and returns the default, with no error. With the keys as constants in `SharedPrefManager`, a typo doesn't compile. This is the same idea as the column-name constants in [Exp 4](./exp4-sqlite).

### 4. `int`, `boolean` and `remove` (new)

The manual's objectives promise saving "integers, long, Boolean and other variable types", but its app saves only two strings. This guide adds:

- a `boolean`, the "Remember password" checkbox;
- an `int`, the save counter;
- `remove`, to forget the password when the box is unticked.

It also fills the form with the saved values when the app opens, which is what preferences are usually for. In the manual's app, the saved values only appear if you go to the second screen and press **Load**.

### 5. File modes

The manual lists three modes for `getSharedPreferences`: `MODE_PRIVATE`, `MODE_WORLD_READABLE` and `MODE_WORLD_WRITABLE`. The last two let *other apps* read or change your file. They were deprecated in Android 4.2 because of that, and since Android 7.0 (API 24, the lab's minimum) using them throws a **`SecurityException`**. `MODE_PRIVATE` is the only mode to use. To share data with other apps, Android has content providers.

### 6. Navigating back

::: code-group

```java [Manual]
// SecondActivity, Back button
intent = new Intent(SecondActivity.this, MainActivity.class);
startActivity(intent);
finish();
```

```java [This guide]
finish(); // MainActivity is still underneath on the back stack
```

:::

The manual finishes `MainActivity` when it opens the second screen, and the Back button then starts a *new* `MainActivity`. That throws away whatever the user had typed, and the system Back button closes the app instead of going back. This guide leaves `MainActivity` on the back stack and only calls `finish()` in `SecondActivity`, the same fix as in [Android Layouts](./exp2-layouts).

### 7. Smaller changes

| Manual | This guide | Why |
| --- | --- | --- |
| "primitive data types (int, byte, short, long, float, double, boolean, and char)" | `String`, `int`, `long`, `float`, `boolean`, `Set<String>` | There are no `put` methods for `byte`, `short`, `double` or `char`. |
| Folder `shared-prefs` | `shared_prefs` | The real folder name, with an underscore. |
| File name `"My Shared Preference"` | `"exp6_prefs"` | File names with spaces work, but are awkward to find and type. |
| `getInstance` without `synchronized` | `synchronized` | Two threads can't create two instances. |
| `(EditText) findViewById(…)` | `findViewById(…)` | `findViewById` is generic, so the cast is no longer needed. |
| `Intent` objects kept as Activity fields | Created where they are used | They are used once, in one listener. |
| Layouts built from component-tree screenshots | Paste the full XML | Everyone gets an identical layout with the right IDs. |
| Password saved with no warning | Saved only to show why you shouldn't | See [Never store passwords](#preferences-or-sqlite). |

::: info Does Android need the singleton?
`getSharedPreferences` already caches: asking for the same file name twice returns the same `SharedPreferences` object. So the singleton here doesn't save memory. It gives the app one class that owns the file name and the keys, which is the reason to write it. The pattern matters more for objects that *are* expensive to create twice, such as the database helper from [Exp 4](./exp4-sqlite).
:::

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| `SharedPrefManager() has private access in SharedPrefManager` | Some code calls `new SharedPrefManager(…)`. That is the point of the private constructor: use `SharedPrefManager.getInstance(this)`. |
| **Load** shows "(not saved)" although you saved | The key is spelled differently when writing and reading. Use the `KEY_…` constants everywhere. |
| The values are gone after reinstalling | Expected: uninstalling deletes the app's private folder, the preferences file included. Running from Android Studio normally *updates* the app and keeps it. |
| The password is still filled in after unticking the box | **Save** was not pressed after unticking, or `remove` is missing from the `else` branch. |
| `SecurityException: MODE_WORLD_READABLE no longer supported` | The file was opened with a world mode from an old tutorial. Use `Context.MODE_PRIVATE`. |
| `ActivityNotFoundException: Unable to find explicit activity class …SecondActivity` | `SecondActivity` is not in the manifest. Create Activities with **New → Activity**, which registers them, or add `<activity android:name=".SecondActivity" />` yourself. |
| A view jumps to the top-left corner when the app runs | It is missing a constraint. Every view needs a vertical and a horizontal one. |
| The system Back button closes the app from the second screen | `MainActivity` was finished when the second screen opened. Don't call `finish()` there. |

## Things to try

- Add a **Clear** button that deletes everything with `sharedPreferences.edit().clear().apply()`. Add a `clear()` method to `SharedPrefManager` for it.
- Add a **dark mode** switch: save a `boolean`, and on launch apply it with `AppCompatDelegate.setDefaultNightMode(…)`.
- Save the time of the last save as a `long` (`System.currentTimeMillis()`) and show it on the second screen.
- Make `DataBaseHelper` from [Exp 4](./exp4-sqlite) a singleton, so both Activities share one database connection.
- Show the second screen's values in `onCreate`, so they appear without pressing **Load**.
