# Setup Guide

This page gets your machine ready for every lab: install Android Studio, create an emulator, and create a lab project the same way each week. Do this **before** your first lab session. The downloads are several gigabytes.

::: tip Why this page exists
The department manual was written for Android Studio 3.x. Menus, templates and defaults have changed a lot since then. Use the steps here, not the screenshots in the PDF.
:::

## What you need

| | Minimum | Recommended |
| --- | --- | --- |
| RAM | 8 GB | 16 GB or more |
| Free disk | 16 GB | 32 GB on an SSD |
| CPU | 64-bit with virtualization (Intel VT-x / AMD-V) or Apple silicon | |
| OS | Windows 10/11 64-bit, macOS, or a 64-bit Linux distribution | |

You do **not** need to install Java separately. Android Studio ships with its own JDK.

## 1. Install Android Studio

1. Go to [developer.android.com/studio](https://developer.android.com/studio) and download the latest **stable** release for your OS.
2. Install it:
   - **Windows:** run the `.exe` and keep the default components, including **Android Virtual Device**.
   - **macOS:** open the `.dmg` and drag **Android Studio** into **Applications**.
   - **Linux:** extract the `.tar.gz` (for example into `/opt` or your home folder) and run `bin/studio.sh`.
3. Start Android Studio. The **Setup Wizard** opens on first launch:
   1. Choose **Do not import settings** if asked.
   2. Pick the **Standard** install type.
   3. Pick a light or dark theme.
   4. Accept **every** license on the *License Agreement* screen. Click each item in the left list and accept it.
   5. Click **Finish** and wait while the SDK, platform tools and emulator download.
4. When the **Welcome to Android Studio** window appears, the install is done.

::: warning Keep the SDK path short and ASCII-only
On Windows the SDK goes into `C:\Users\<you>\AppData\Local\Android\Sdk` by default. If your user name has spaces or non-English characters, change the SDK location to something like `C:\Android\Sdk` in **Settings → Languages & Frameworks → Android SDK**.
:::

## 2. Create an emulator

1. From the Welcome window, open **More Actions → Virtual Device Manager**. Inside a project, use **View → Tool Windows → Device Manager**.
2. Click **+** (**Create Virtual Device**).
3. Choose a **Phone** profile, such as **Pixel 8** or any recent Pixel, and click **Next**.
4. Choose a system image:
   - Pick a recent API level with the **Google APIs** or **Google Play** label. Images with Google Play are needed for the Maps experiment later.
   - Use the **x86_64** image on Intel/AMD machines and the **arm64-v8a** image on Apple silicon.
   - Click the download icon next to the image if it is not installed yet.
5. Click **Finish**. The device appears in Device Manager.
6. Press **▶** next to the device to boot it once. The first boot is slow, and later boots are much faster.

::: details The emulator is very slow or won't start
- **Windows:** enable **Windows Hypervisor Platform** in *Turn Windows features on or off*, then reboot. Make sure virtualization is enabled in your BIOS/UEFI.
- **Linux:** check that KVM works with `ls -l /dev/kvm`. Add yourself to the `kvm` group if needed (`sudo usermod -aG kvm $USER`), then log out and back in.
- Close other heavy apps. Give the emulator 2 GB of RAM in **Edit device → Show Advanced Settings**.
- As a last resort, use a physical phone (below).
:::

### Using a physical phone (optional)

1. On the phone, open **Settings → About phone** and tap **Build number** seven times to enable **Developer options**.
2. In **Settings → System → Developer options**, turn on **USB debugging**.
3. Connect the phone with a USB cable and accept the **Allow USB debugging?** prompt on the phone.
4. The phone now appears in the device drop-down in the Android Studio toolbar.

Recent phones can also use **Wireless debugging** (Developer options → Wireless debugging, then **Pair Devices Using Wi-Fi** in Device Manager).

## 3. Create a lab project

Every experiment starts with a new project created **exactly** this way. Each experiment page tells you which name to use.

1. From the Welcome window, click **New Project**. Inside an open project, use **File → New → New Project…**.
2. In **Phone and Tablet**, select **Empty Views Activity** and click **Next**.

   ::: danger Not "Empty Activity"
   **Empty Activity** creates a Kotlin + Jetpack Compose project. This lab uses **Java and XML layouts**, so you must pick **Empty Views Activity**.
   :::

3. Fill in the form:

   | Field | Value |
   | --- | --- |
   | Name | The name on the experiment page, e.g. `Exp2CustomerList` |
   | Package name | `edu.birzeit.encs5150.exp2` (change the last part per experiment) |
   | Save location | Any folder **without spaces**, e.g. `C:\AndroidLabs\Exp2CustomerList` |
   | Language | **Java** |
   | Minimum SDK | **API 24 ("Nougat"; Android 7.0)** |
   | Build configuration language | **Kotlin DSL (build.gradle.kts)**, the default. It is fine for Java apps. |

4. Click **Finish** and wait for the first **Gradle sync** to finish (watch the status bar at the bottom). The first sync downloads dependencies and can take several minutes.

### What the template gives you

Switch the Project tool window (left side) to the **Android** view. The files you will work with are:

| File | Purpose |
| --- | --- |
| `app/manifests/AndroidManifest.xml` | Declares the app and every Activity in it |
| `app/java/<package>/MainActivity.java` | The first screen's code |
| `app/res/layout/activity_main.xml` | The first screen's XML layout |
| `app/res/values/strings.xml` | Text resources |
| `Gradle Scripts/build.gradle.kts (Module :app)` | `minSdk`, `targetSdk` and dependencies |

The generated `MainActivity` looks like this:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
```

`EdgeToEdge.enable(this)` lets the app draw behind the status and navigation bars. The insets listener then pads the root view (the one with `android:id="@+id/main"`) so your content isn't hidden under them. **Keep these lines** unless an experiment tells you otherwise. If you replace the layout, keep `android:id="@+id/main"` on its root element.

### Run the app

1. Pick your emulator or phone in the device drop-down in the toolbar.
2. Click **Run ▶** (<kbd>Shift</kbd>+<kbd>F10</kbd> on Windows/Linux, <kbd>Ctrl</kbd>+<kbd>R</kbd> on macOS).
3. The app opens on the device and shows **Hello World!**

## 4. Add another Activity

Several experiments need a second screen. Let the wizard create it, so the Java class, the layout and the manifest entry are all made for you:

1. In the Project window, right-click your package folder under `app/java/`.
2. Choose **New → Activity → Empty Views Activity**.
3. Enter the **Activity Name** (e.g. `AddCustomerActivity`). Leave **Generate a Layout File** checked and **Launcher Activity** unchecked. Make sure **Source Language** is **Java**.
4. Click **Finish**.

The wizard adds `<activity android:name=".AddCustomerActivity" … />` to `AndroidManifest.xml`. You do not need to edit the manifest by hand.

## Troubleshooting

| Problem | Fix |
| --- | --- |
| Gradle sync fails with a network error | You need internet access on the first sync. Check proxy settings in **Settings → Appearance & Behavior → System Settings → HTTP Proxy**, then **File → Sync Project with Gradle Files**. |
| `SDK location not found` | Open **File → Project Structure → SDK Location** and point it to your Android SDK folder. |
| Your Activity extends `ComponentActivity` or the code is Kotlin | You picked **Empty Activity**. Create the project again with **Empty Views Activity**. |
| Red `R` everywhere | Usually an XML error in a layout. Fix it (check the **Build** window), then **Build → Rebuild Project**. |
| Emulator shows "System UI isn't responding" | Wait for the first boot to finish, or use **Cold Boot Now** from the device's menu in Device Manager. |
| App installs but shows an old version | **Run → Stop**, then run again. If that doesn't help, uninstall the app from the emulator. |
