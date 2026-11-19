---
title: "Integrating a REST API"
short: "REST API"
manual: "/ENCS515-Manual-2025.pdf#page=90"
slides: "/slides/exp8-rest-api.pdf"
---
# Integrating a REST API

Until now, all of your app's data lived on the phone: in its layouts, in SQLite, in shared preferences. Most real apps get their data from a **server** instead. The app sends an HTTP request to an address, and the server answers with data, usually as **JSON** text. A server that works this way is called a **REST API**.

In this lab you build an app that talks to two servers. **Get Data** downloads a list of students from this site as JSON, turns it into Java objects, and lists them. **Add Student** sends a new student to a test server as JSON, and shows the server's answer. Both requests run on a background thread, with a spinner while they wait and a clear message if anything goes wrong.

## Objectives

- Send HTTP GET and POST requests from Android with `HttpURLConnection`.
- Parse a JSON array into Java objects with `JSONArray` and `JSONObject`, and build JSON to send.
- Run network code on a background thread with an `ExecutorService`, and show the result on the main thread with a `Handler`.
- Read HTTP status codes, and handle a missing connection, a timeout and a server error.

## Background

### REST in one paragraph

**REST** (Representational State Transfer) is a style of building web APIs. Each kind of data the server has, such as students, is a **resource** with its own address, and you work with it using the standard **HTTP methods**:

| Method | Means | Example |
| --- | --- | --- |
| `GET` | Read | `GET /students` returns the list of students |
| `POST` | Create | `POST /students` with a student in the body adds one |
| `PUT` | Replace | `PUT /students/3` replaces student 3 |
| `DELETE` | Delete | `DELETE /students/3` deletes student 3 |

In Exp 9 you write a server like this yourself with Spring Boot. In this lab you only write the app side.

### A request and its response

A request has a **method**, a **URL**, some **headers** and, for `POST` and `PUT`, a **body**. The response has a **status code**, headers and a body. This is the `POST` the app sends in this lab, and what the test server answers:

::: code-group

```http [Request]
POST /users HTTP/1.1
Host: jsonplaceholder.typicode.com
Content-Type: application/json; charset=utf-8
Accept: application/json

{"name":"Ahmad","age":21}
```

```http [Response]
HTTP/1.1 201 Created
Content-Type: application/json; charset=utf-8

{
  "name": "Ahmad",
  "age": 21,
  "id": 11
}
```

:::

`Content-Type` says what the body is, and `Accept` says what you want back. The status code tells you how it went. The first digit is what matters:

| Code | Means | You will see it when |
| --- | --- | --- |
| `200 OK` | It worked, the answer is in the body | Get Data succeeds |
| `201 Created` | It worked, something was created | Add Student succeeds |
| `404 Not Found` | There is nothing at that address | The URL has a typo |
| `500 Internal Server Error` | The server crashed handling it | The server has a bug (your Exp 9 server, one day) |

Codes from `200` to `299` mean success, `4xx` means the request was wrong, and `5xx` means the server failed.

### JSON

**JSON** is text that describes data with two building blocks: **objects** in `{ }`, which are named fields, and **arrays** in `[ ]`, which are lists. This is the start of the file Get Data downloads, [`/api/students.json`](/api/students.json){target="_blank"}:

```json
[
  { "id": 1, "name": "Ahmad", "age": 21 },
  { "id": 2, "name": "Omar", "age": 22 }
]
```

Android includes the `org.json` classes to read and write it, with no library to add:

| JSON | Class | Read a value with |
| --- | --- | --- |
| `[ … ]` | `JSONArray` | `length()`, `getJSONObject(i)` |
| `{ … }` | `JSONObject` | `getInt("id")`, `getString("name")`, and `put("name", …)` to write |

Each `get` method throws a `JSONException` if the field is missing or has the wrong type, so your code finds out at once when the server sends something unexpected.

### Never on the main thread

The **main thread** draws the screen and handles every tap. A network request can take seconds, and if the main thread waited for it, the app would freeze, and after five seconds Android would offer to close it: an **ANR** (Application Not Responding). So Android doesn't allow it at all: a request on the main thread throws `NetworkOnMainThreadException`.

The opposite rule also holds: **only the main thread may touch views**. Setting a `TextView`'s text from another thread throws `CalledFromWrongThreadException`. So every request in this lab goes the same way:

| Where | What happens | With |
| --- | --- | --- |
| Main thread | Show the spinner, start the request | A click listener |
| Background thread | Send the request, wait, parse the JSON | `executor.execute(() -> …)` |
| Main thread | Hide the spinner, show the students or the error | `mainHandler.post(() -> …)` |

An **`ExecutorService`** is a pool of background threads that runs the tasks you give it. `Executors.newSingleThreadExecutor()` makes one with a single thread, so requests run one after another. A **`Handler`** made with `Looper.getMainLooper()` runs the code you `post` to it on the main thread.

### Permissions and HTTPS

An app can only use the network if its manifest asks for the **`INTERNET`** permission. It is granted when the app is installed, without asking the user.

Since Android 9, apps may also only use **HTTPS** by default. Both servers in this lab use HTTPS, so there is nothing to change. A plain `http://` address fails with `Cleartext HTTP traffic … not permitted`. You will meet this in Exp 9, when the app calls your own server on your laptop: [Differences from the manual](#_6-https-and-a-narrow-exception-for-your-own-server) shows how to allow it for that one address.

## Step 1: Create the project

Create a new project as described in [Setup Guide → Create a lab project](/setup/#_3-create-a-lab-project):

| Field | Value |
| --- | --- |
| Template | **Empty Views Activity** |
| Name | `Exp8Rest` |
| Package name | `edu.birzeit.encs5150.exp8` |
| Language | Java |
| Minimum SDK | API 24 |

`HttpURLConnection`, the `org.json` classes and `ExecutorService` are all part of Android, so you don't add any dependencies.

## Step 2: Ask for the `INTERNET` permission

Open `manifests/AndroidManifest.xml` and add this line **inside** `<manifest>`, **above** `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Without it, every request fails with `SecurityException: Permission denied (missing INTERNET permission?)`.

## Step 3: The `Student` class

1. Right-click the package `edu.birzeit.encs5150.exp8` → **New → Java Class**.
2. Name it `Student` and press <kbd>Enter</kbd>.
3. Replace the file's contents with:

<<< @/snippets/exp8/Student.java{java}

It has the same three fields as one object in the JSON: `id`, `name` and `age`. The fields are `final`, so a `Student` can't change after it is made. `toString()` gives the line the list shows, such as `1. Ahmad, 21`.

## Step 4: The JSON parser

Create another Java class, `StudentJsonParser`, and replace its contents with:

<<< @/snippets/exp8/StudentJsonParser.java{java}

- **`fromJsonArray`** reads the whole reply. `new JSONArray(json)` parses the text, and the loop turns each object in it into a `Student`.
- **`fromJson`** reads one object. It is separate because the `POST` reply is a single object, not an array.
- **`toJson`** goes the other way: it builds the object to send. It has no `id`, because the server chooses it.

All three throw `JSONException` instead of catching it, so the caller decides what to show when the JSON is wrong.

## Step 5: The HTTP manager

Create a Java class, `HttpManager`, and replace its contents with:

<<< @/snippets/exp8/HttpManager.java{java}

1. **`Response`** holds what came back: the status `code` and the `body` as text. `isSuccessful()` is true for any `2xx` code.
2. **`open`** turns the address into a `URL` and opens an `HttpURLConnection`. Nothing is sent yet. The two **timeouts** make a request give up after 10 seconds, instead of waiting for ever on a bad connection.
3. **`get`** sets the method and the `Accept` header, then reads the reply.
4. **`post`** also sets `Content-Type`, and **`setDoOutput(true)`** says the request has a body. Writing the JSON to `getOutputStream()` sends it.
5. **`read`** calls `getResponseCode()`, which sends the request and waits for the answer. A successful body is on `getInputStream()`, but for a `4xx` or `5xx` code it is on `getErrorStream()`, and `getInputStream()` would throw. The `BufferedReader` then reads the body line by line.
6. **`disconnect()`** in `finally` releases the connection, even if something threw.

`try (…)` is **try-with-resources**: it closes the stream at the end of the block, even after an exception.

## Step 6: The layout

Replace **the whole** `res/layout/activity_main.xml` with:

<<< @/snippets/exp8/activity_main.xml{xml}

From top to bottom: the **Get Data** button, a row with a small `ProgressBar` (hidden with `visibility="gone"` until a request runs) and a status `TextView`, a `ScrollView` that takes the rest of the screen (`layout_height="0dp"` with `layout_weight="1"`) holding the empty `student_list`, and at the bottom the name and age fields and the **Add Student** button. The age field has `inputType="number"`, so its keyboard has digits only.

## Step 7: The Activity

Open `MainActivity.java` and replace the whole file with the code below. The template lines at the top of `onCreate` stay as they were.

<<< @/snippets/exp8/MainActivity.java{java}

1. **The two URLs** are constants at the top. `STUDENTS_URL` is a JSON file on this site. `ADD_STUDENT_URL` is [JSONPlaceholder](https://jsonplaceholder.typicode.com){target="_blank"}, a free test server that answers `POST` requests like a real API, but doesn't save anything.
2. **`executor`** is the background thread and **`mainHandler`** the way back to the main thread, as in [Never on the main thread](#never-on-the-main-thread).
3. **`getStudents`** shows the spinner, then gives the executor a lambda. Inside it, everything until `mainHandler.post` runs on the background thread: the `GET`, the status check and the parsing. The lambda inside `mainHandler.post` runs on the main thread and fills the list.
4. **`addStudent`** first checks the two fields on the main thread, and shows an error on an empty field with `setError`. Then the background thread builds the JSON, sends the `POST`, and parses the single object the server sends back. The main thread shows the code and the reply, and adds the new student to the list.
5. **The `catch` blocks** turn each failure into a message: `IOException` when the request didn't get an answer, `JSONException` when it did but the JSON is wrong. `describe` gives the two most common `IOException`s a plain sentence.
6. **`showError`** is called from the background thread, so it too goes through `mainHandler.post`.
7. **`setBusy`** shows or hides the spinner and turns both buttons off while a request runs, so a second tap can't start a second request.
8. **`onDestroy`** shuts the executor down, so its thread doesn't outlive the Activity.

## Run and check

Run the app, open **Logcat** in Android Studio, and type `Exp8Rest` in the search box.

- Press **Get Data**. The spinner shows for a moment, then the status says "Got 6 students" and the six students are listed.
- Type a name and an age and press **Add Student**. The status shows `The server answered 201 with:` and the JSON the server sent back, with `"id": 11`, and the student is added at the end of the list.
- Press **Add Student** with an empty field. The field shows an error and nothing is sent.

Now break things on purpose, one at a time, and put each one back afterwards:

| Do this | On screen | Why |
| --- | --- | --- |
| Turn on **Airplane mode** in the emulator (swipe down from the top), then press **Get Data** | "No internet connection, or the address is wrong" | Without a network, the name `encs5150.birzeit.me` can't be looked up: `UnknownHostException` |
| Change `students.json` in `STUDENTS_URL` to `student.json` | "The server answered 404" | The server answered, but there is no file at that address |
| Change `STUDENTS_URL` to `https://jsonplaceholder.typicode.com/users/1` | "The reply is not the JSON we expected" | That address returns one object, not an array, so `new JSONArray` throws |
| Change `https` to `http` in `STUDENTS_URL` | "Network error: Cleartext HTTP traffic to encs5150.birzeit.me not permitted" | Android allows HTTPS only, as in [Permissions and HTTPS](#permissions-and-https) |
| Move the code inside `executor.execute(…)` out of the lambda, so it runs directly in `getStudents` | The app crashes | Logcat shows `NetworkOnMainThreadException` |

Every error is also written to Logcat with `Log.e`, in red.

## Differences from the manual

The manual's address no longer works, and its code uses `AsyncTask`, which Android has deprecated. Here is what changed and why.

### 1. A working address, and a `POST`

The manual downloads its students from a `mocky.io` address that has been taken down, so its app can no longer get any data. This guide downloads the same kind of list, with the same `id`, `name` and `age` fields, from a file on this site, which stays up as long as the lab does.

The manual's objectives also promise `POST` requests, but its code only does a `GET`. This guide adds **Add Student**, which sends JSON to JSONPlaceholder and reads the `201 Created` reply.

### 2. `ExecutorService` and `Handler` instead of `AsyncTask`

::: code-group

```java [Manual]
public class ConnectionAsyncTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        return HttpManager.getData(params[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        ((MainActivity) activity).fillStudents(StudentJsonParser.getObjectFromJson(s));
    }
}
```

```java [This guide]
executor.execute(() -> {
    HttpManager.Response response = HttpManager.get(STUDENTS_URL);
    List<Student> students = StudentJsonParser.fromJsonArray(response.body);
    mainHandler.post(() -> {
        // show the students
    });
});
```

:::

`AsyncTask` has been deprecated since Android 11. Its official replacement is the standard `java.util.concurrent` classes, which is what this guide uses. It also had real problems:

- **It leaked the Activity.** The manual's task keeps the `Activity` in a field. If the screen rotates while it runs, Android destroys that Activity and makes a new one, but the task still holds the old one, and `onPostExecute` fills a list that is no longer on screen.
- **It needed a cast.** `((MainActivity) activity)` ties the task to one Activity class. Here the code is a lambda inside `MainActivity`, so it calls its methods directly.
- **It parsed on the main thread.** The manual parses the JSON in `onPostExecute`, on the main thread. Here the parsing happens in the background, next to the request.

The same two steps also exist in libraries. [Retrofit](https://square.github.io/retrofit/){target="_blank"}, the most used HTTP library for Android, runs the request on its own thread and calls you back on the main thread. Writing it yourself once with `HttpURLConnection` shows what such a library does for you.

### 3. Errors are handled

The manual's `getData` catches every exception, logs it, and returns `null`, which the parser and `fillStudents` then use without checking. So **any** network problem crashes the app with a `NullPointerException`. Bad JSON does the same: the parser returns a `null` list.

This guide doesn't hide errors. `HttpManager` throws them, and `MainActivity` turns each kind into a message for the user:

| Problem | Manual | This guide |
| --- | --- | --- |
| No connection | Crash | "No internet connection, or the address is wrong" |
| Server never answers | Waits for ever | Gives up after 10 seconds: "The server took too long to answer" |
| `404` or `500` | `getInputStream()` throws, so it crashes | "The server answered 404" |
| JSON in the wrong shape | Crash | "The reply is not the JSON we expected" |

It also checks the status code. The manual never calls `getResponseCode()`, so it can't tell a list of students from an error page.

### 4. The parser, tidied

::: code-group

```java [Manual]
JSONObject jsonObject = new JSONObject();
jsonObject = (JSONObject) jsonArray.get(i);
Student student = new Student();
student.setID(jsonObject.getInt("id"));
student.setName(jsonObject.getString("name"));
student.setAge(jsonObject.getDouble("age"));
```

```java [This guide]
JSONObject object = array.getJSONObject(i);
return new Student(object.getInt("id"), object.getString("name"), object.getInt("age"));
```

:::

The manual creates an empty `JSONObject` only to throw it away on the next line, and casts the result of `get(i)` where `getJSONObject(i)` already returns the right type. It also builds each `Student` empty and fills it with setters. A constructor makes a complete `Student` in one step, and the fields can be `final`.

### 5. The layout

The manual's layout uses `android.support.constraint.ConstraintLayout`, from the old support library, which doesn't exist in a new project: the layout fails to inflate. It also gives the list a fixed size, `395dp` by `507dp`, which is too big for small phones and leaves a gap on large ones, and the list doesn't scroll. This guide uses a `LinearLayout` where the list's `ScrollView` takes whatever space is left.

### 6. HTTPS, and a narrow exception for your own server

The manual's address uses plain `http://`, so it turns on **cleartext** (unencrypted) traffic for **every** address with a `base-config`. This guide uses HTTPS, which needs no configuration.

In Exp 9, though, the app calls the Spring Boot server on your laptop, which speaks plain HTTP. From the emulator, your laptop is always at the address `10.0.2.2`. Allow cleartext for that address **only**: add a new file `res/xml/network_security_config.xml`:

<<< @/snippets/exp8/network_security_config.xml{xml}

and point to it from the `<application>` tag in the manifest:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    … >
```

Every other address still needs HTTPS, so the app stays as safe as before.

### 7. Smaller changes

| Manual | This guide | Why |
| --- | --- | --- |
| `getData` returns the body as a `String`, or `null` | `get` and `post` return a `Response` with the status code and the body | The caller can check the code. |
| `new URL(URL)` | `URI.create(address).toURL()` | The `URL` constructors are deprecated in current Java. The parameter is also renamed: a variable named `URL` hides the class `URL`. |
| `Double age` | `int age` | Ages in the JSON are whole numbers. |
| `ID` | `id` | Java's naming convention: fields start with a lower-case letter. |
| `new View.OnClickListener() { … }` | A lambda, `v -> …` | Shorter. |
| `import android.support.v7.app.AppCompatActivity` | `androidx.appcompat.app.AppCompatActivity` | The support library was replaced by AndroidX. |
| The button text changes to "connecting" and "connected" | The button is disabled while a request runs, and a status line says what happened | "connected" stayed on the button for ever, and a second tap started a second request. |

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| `SecurityException: Permission denied (missing INTERNET permission?)` | The `<uses-permission>` line is missing from the manifest, or is inside `<application>`. Put it above `<application>`, as in Step 2. |
| `NetworkOnMainThreadException` | A request runs on the main thread. Every call to `HttpManager` must be inside `executor.execute(() -> …)`. |
| `CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views` | A view is changed on the background thread. Wrap that code in `mainHandler.post(() -> …)`. |
| "No internet connection" on the emulator, but your laptop is online | The emulator lost its network. Turn Airplane mode off, or cold-boot the emulator from the **Device Manager** (the menu next to the device → **Cold Boot Now**). |
| `Cleartext HTTP traffic to … not permitted` | The URL starts with `http://`. Use `https://`, or for your own server, the `network_security_config.xml` in [Differences from the manual](#_6-https-and-a-narrow-exception-for-your-own-server). |
| "The reply is not the JSON we expected" | The reply isn't an array of objects with `id`, `name` and `age`. Open the URL in a browser to see what it really returns. |
| `JSONException: No value for age` | A field name in `getInt("…")` doesn't match the JSON. Names are case-sensitive: `"ID"` is not `"id"`. |
| The spinner never goes away | A path through the background lambda ends without calling `mainHandler.post` or `showError`. Every `return` and every `catch` must end in one of them. |
| The added student always gets `id` 11 | That's JSONPlaceholder: it only pretends to save, and gives every new user the next id after its ten. A real server, such as yours in Exp 9, saves it and gives a new id each time. |

## Things to try

- Open [`/api/students.json`](/api/students.json){target="_blank"} and `https://jsonplaceholder.typicode.com/users` in a browser, then send the same `GET` and `POST` from **Postman**, and compare the status codes and headers with what the app shows.
- Show the students in a `ListView` with an `ArrayAdapter`, as in Exp 2, instead of adding `TextView`s by hand.
- Save the downloaded students in the SQLite database from Exp 4, so the list still shows when the phone is offline.
- Add a **Delete** button to each row that sends `DELETE https://jsonplaceholder.typicode.com/users/{id}`. What status code comes back?
- Rotate the screen while a request runs (set the timeout to a few seconds and use a slow address). The result is lost, because the new Activity never hears about it. Move the request into a `ViewModel` with `LiveData`, as in Exp 7, and try again.
