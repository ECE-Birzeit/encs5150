# Android Layouts

In this lab you build a **Customer List** app with two screens:

- **MainActivity** shows an **Add Customer** button above a scrollable list of every customer added so far. Its whole layout is built **in Java code**.
- **AddCustomerActivity** is a form with **Id**, **Name**, **Phone**, a **Gender** spinner and an **Add Customer** button. Its layout is written **in XML**.

Tapping **Add Customer** on the main screen opens the form with an **Intent**. Submitting the form adds the customer and returns to the list, which now shows the new entry.

## Objectives

- Understand Views, ViewGroups, layouts and widgets, and how they relate.
- Declare a layout statically in XML.
- Build a layout dynamically at runtime and add widgets to it from code.
- Switch between two Activities with an Intent.
- Handle user actions with event listeners.

## Background

### Views and ViewGroups

Every Android screen is a **tree of `View` objects**.

- A **widget** is a single UI element: `TextView`, `EditText`, `Button`, `Spinner`… Widgets extend `View` directly.
- A **layout** is a container that positions its children: `LinearLayout`, `ConstraintLayout`, `FrameLayout`, `ScrollView`… Layouts extend `ViewGroup`, which itself extends `View`. Because a `ViewGroup` is a `View`, layouts can be nested inside other layouts.

The add-customer form you build in this lab has this tree:

```text
LinearLayout (vertical)            ← root, id = main
├── TextView        "Customer Info"
├── LinearLayout (horizontal)
│   ├── TextView    "Id"
│   └── EditText    editText_Id
├── LinearLayout (horizontal)
│   ├── TextView    "Name"
│   └── EditText    editText_Name
├── LinearLayout (horizontal)
│   ├── TextView    "Phone"
│   └── EditText    editText_Phone
├── LinearLayout (horizontal)
│   ├── TextView    "Gender"
│   └── Spinner     spinner_Gender
└── Button          button_Add_Customer
```

### Common layouts

| Layout | Arranges its children… |
| --- | --- |
| `LinearLayout` | in a single row (`horizontal`) or column (`vertical`) |
| `ConstraintLayout` | by constraints relative to each other and to the parent. It is the template default and best for complex screens. |
| `FrameLayout` | stacked on top of each other, usually holding one child |
| `ScrollView` | in a scrollable area. It has exactly **one** child, usually a `LinearLayout`. |

::: tip Keep the tree shallow
Each nested layout costs time to measure and draw. Prefer a wide tree over a deep one. This lab nests `LinearLayout`s because they are easy to understand. Real apps often use one `ConstraintLayout` instead.
:::

### XML or code?

You can declare a layout **statically** in an XML file (`res/layout/…`), **dynamically** in Java at runtime, or mix both. XML is easier to read and preview. Code is needed when the content isn't known in advance, like a list whose length depends on user input. This lab does one screen each way.

### Intents and the back stack

An **Intent** is a message asking Android to do something. An **explicit Intent** names the Activity to open:

```java
startActivity(new Intent(MainActivity.this, AddCustomerActivity.class));
```

Android keeps open Activities on a **back stack**. Starting `AddCustomerActivity` puts it on top of `MainActivity`. Calling `finish()` (or pressing Back) removes it, and `MainActivity` comes back. When it does, Android calls its **`onResume()`** method. We use that callback to refresh the list.

::: warning Different from the PDF manual
The old manual started a **new** `MainActivity` from the form and finished the old one. That breaks the Back button and needlessly recreates the screen. This guide simply calls `finish()` and rebuilds the list in `onResume()`.
:::

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp2CustomerList` |
| Package name | `edu.birzeit.encs5150.exp2` |
| Language | Java |
| Minimum SDK | API 24 |

## Step 2: The `Customer` class

This plain Java class holds one customer's data. The `static` list stores all customers so both Activities can reach it.

1. In the Project window, right-click the package `edu.birzeit.encs5150.exp2` → **New → Java Class**.
2. Name it `Customer` and press <kbd>Enter</kbd>.
3. Replace the file's contents with:

<<< @/snippets/exp2/Customer.java{java}

::: tip Let Android Studio write it for you
After typing only the four fields, press <kbd>Alt</kbd>+<kbd>Insert</kbd> (<kbd>⌘</kbd>+<kbd>N</kbd> on macOS) inside the class and choose **Constructor**, **Getter and Setter**, or **toString()**. This saves a lot of typing.
:::

## Step 3: Create `AddCustomerActivity`

Let the wizard create the Java class, the layout file and the manifest entry in one go:

1. Right-click the package `edu.birzeit.encs5150.exp2` → **New → Activity → Empty Views Activity**.
2. Set **Activity Name** to `AddCustomerActivity`. The layout name fills in as `activity_add_customer`.
3. Leave **Launcher Activity** unchecked, check that **Source Language** is **Java**, and click **Finish**.

Open `AndroidManifest.xml`. You should see a new line:

```xml
<activity
    android:name=".AddCustomerActivity"
    android:exported="false" />
```

`exported="false"` means only your own app can open this screen, which is what we want.

## Step 4: The form layout (XML)

Open `res/layout/activity_add_customer.xml`, switch to **Code** view, and replace **the whole file** with:

<<< @/snippets/exp2/activity_add_customer.xml{xml}

Switch to **Split** view to check that it matches the tree in the [Background](#views-and-viewgroups) section.

Things to notice:

- Each row is a horizontal `LinearLayout`. The label has a fixed width of `80dp` so all fields line up. The field takes `layout_width="0dp"` plus `layout_weight="1"`, which means "fill whatever width is left".
- `inputType` chooses the keyboard: `number` shows a digits-only keypad and `phone` a dial pad. `maxLength="9"` keeps the Id small enough to parse as a `long`.
- The spinner has no items yet. They are added in Java in the next step.
- The root keeps `android:id="@+id/main"`. The wizard's Java code uses it to keep the form clear of the status bar.

## Step 5: The form code

Open `AddCustomerActivity.java` and replace the whole file with the code below. The first lines of `onCreate` are exactly what the wizard generated, and everything after them is new.

<<< @/snippets/exp2/AddCustomerActivity.java{java}

How it works:

1. **Spinner:** an `ArrayAdapter` turns the `String[]` into one row per option. `simple_spinner_item` is the built-in look of the closed spinner, and `simple_spinner_dropdown_item` that of the open list.
2. **Listener:** `onClick` runs when **Add Customer** is tapped.
3. **Validation:** every empty field is marked with `setError`, which shows a red icon and a message on the field. If anything is missing, `return` stops before saving. The user can't add a customer with missing data.
4. **Save and close:** the customer is added to `Customer.customersArrayList` and `finish()` closes the form. There is no new Intent back to `MainActivity`, because it is still on the back stack underneath.

## Step 6: The main screen (built in code)

`MainActivity` doesn't use an XML layout at all. It creates every view with `new`, nests them, and hands the root to `setContentView`:

```text
LinearLayout (vertical)      ← rootLinearLayout
├── Button "Add Customer"    ← addButton
└── ScrollView
    └── LinearLayout (vertical)   ← customersLinearLayout, one TextView per customer
```

Open `MainActivity.java` and replace the whole file with:

<<< @/snippets/exp2/MainActivity.java{java}

::: danger Remove the template lines
The template's `onCreate` contains these lines. **They must not be in your file:**

```java
EdgeToEdge.enable(this);
setContentView(R.layout.activity_main);
ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> { … });
```

`R.id.main` only exists inside `activity_main.xml`, which this screen no longer loads. `findViewById` would return `null` and the app would crash on launch. The code above attaches the same kind of insets listener to `rootLinearLayout` instead. That listener is still needed because Android 15 and later always draw apps behind the status bar.

You may delete `res/layout/activity_main.xml`, since nothing uses it anymore. Leaving it there does no harm.
:::

How it works:

1. **`onCreate`** builds the view tree once: it creates the views, sets their properties (`setOrientation`, `setText`, `setLayoutParams`), and nests them with `addView`. A `ScrollView` can only hold one child, so the customers go into a vertical `LinearLayout` inside it.
2. **`LayoutParams`** is the code equivalent of `layout_width` / `layout_height` in XML.
3. **The button's listener** starts `AddCustomerActivity` with an explicit Intent. `MainActivity` is *not* finished, so it stays underneath on the back stack.
4. **`onResume`** runs on first launch *and* every time the user comes back from the form. It clears the list with `removeAllViews()` and adds one `TextView` per customer. If the list were filled in `onCreate`, new customers would never appear.

## Run and check

- The app opens on the main screen with an **Add Customer** button and "No customers yet."
- Tapping **Add Customer** opens the form. **Back** returns to the main screen without adding anything.
- Tapping **Add Customer** on the empty form shows an error on **Id**, **Name** and **Phone** and stays on the form.
- The Id field accepts digits only, and the Phone field shows a dial pad.
- Filling in all fields and tapping **Add Customer** returns to the list, which shows the new customer.
- After adding several customers, the list scrolls and the button stays at the top.
- Pressing **Back** on the main screen exits the app in one press. With the manual's old design, you had to press Back once for every customer added.

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| Crash on launch: `NullPointerException` … `setOnApplyWindowInsetsListener` in `MainActivity` | The template's `findViewById(R.id.main)` line is still in `MainActivity`. Remove it (see Step 6). |
| Crash on launch: `NullPointerException` in `AddCustomerActivity` | The root of `activity_add_customer.xml` lost `android:id="@+id/main"`, or a view ID doesn't match the Java code. |
| `ActivityNotFoundException: Unable to find explicit activity class` | `AddCustomerActivity` is missing from `AndroidManifest.xml`. Create Activities with the wizard, or add the `<activity>` line by hand. |
| `cannot find symbol: variable TextAppearance_AppCompat_…` | Library styles aren't in your app's `R`. Write `androidx.appcompat.R.style.TextAppearance_AppCompat_Medium`, not `R.style.…` (the old manual's line no longer compiles). |
| New customers don't appear in the list | The list is filled in `onCreate` instead of `onResume`. |
| Customers appear twice | `removeAllViews()` is missing at the start of `onResume`. |
| Button is hidden under the status bar | The insets listener on `rootLinearLayout` is missing. |
| `IllegalStateException: ScrollView can host only one direct child` | Add views to `customersLinearLayout`, not to `scrollView`. |
| All customers disappear after a while | Expected: the list lives in memory and is cleared when Android kills the app. [Exp 4](/experiments/) stores data in SQLite. |

## Optional: WebView

A `WebView` is a widget that displays a web page inside your app. To try it, add one to a layout:

```xml
<WebView
    android:id="@+id/webView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

Load a page from code:

```java
WebView webView = findViewById(R.id.webView);
webView.loadUrl("https://ritaj.birzeit.edu");
```

Then allow Internet access in `AndroidManifest.xml`, above the `<application>` element:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Things to try

- Reject an Id that already exists in the list, with `setError`.
- Show a short confirmation such as "Customer added" before the form closes. Look up `Toast`.
- Move the **Gender** options into `res/values/strings.xml` as a `<string-array>` and load them with `android:entries` instead of Java.
- Rebuild the add-customer form with a single `ConstraintLayout` and compare the two trees in the **Layout Inspector**.
