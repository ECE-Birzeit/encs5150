# SQLite Database

In [Android Layouts](./exp2-layouts) the customers lived in an `ArrayList`, and they were gone as soon as Android killed the app. In this lab you keep the same two screens and the same layouts, but save the customers in an **SQLite database** in the app's private storage. They survive closing the app and rebooting the phone. You also add a **confirmation dialog** that asks before a customer is deleted.

## Objectives

- Create a local database with `SQLiteOpenHelper`, and understand when `onCreate` and `onUpgrade` run.
- Insert rows with `ContentValues`, read them back with a `Cursor`, and delete them with a `WHERE` clause.
- Refresh a screen from the database in `onResume`.
- Ask the user to confirm a destructive action with an `AlertDialog`.
- Look inside the app's database with Android Studio's **Database Inspector**.

## Background

### SQLite on Android

SQLite is a small SQL database engine that is built into every Android device. There is no server to install: a database is a single file in your app's private folder, `/data/data/<package>/databases/`. Other apps and the user can't read that folder, so the data is private to your app. It is deleted when the app is uninstalled, or when the user taps **Clear storage** in the app's settings.

This lab assumes you know basic SQL: `CREATE TABLE`, `INSERT`, `SELECT` and `DELETE`.

### Schema and contract

The **schema** is the formal description of the database: its tables, their columns and the columns' types. You write it as the `CREATE TABLE` statement.

Table and column names appear in every query, so keep them in **constants** in one place, instead of typing `"CUSTOMER"` again in each query. Then a typo becomes a compile error instead of a crash, and renaming a column is a one-line change. Larger apps put these constants in their own *contract class*. In this lab they sit at the top of the helper class.

### `SQLiteOpenHelper`

You don't open the database file yourself. You extend `SQLiteOpenHelper` and give it a file name and a **version number**. Then you call `getWritableDatabase()` or `getReadableDatabase()` when you need the database, and the helper:

- **creates** the file and calls your `onCreate` if the file doesn't exist yet. This happens only once in the app's life, not on every launch.
- calls your **`onUpgrade`** if the file exists but its version is lower than the one in your code. That is where you change the schema of a database that already holds data.
- otherwise just **opens** the existing file.

Nothing happens in the constructor itself. The file is created on the first `getWritableDatabase()` call, not at app startup.

### Writing and reading

| Operation | Method | Returns |
| --- | --- | --- |
| Insert a row | `insert(table, null, contentValues)` | The new row's ID, or `-1` if it failed (for example, a duplicate primary key) |
| Read rows | `query(table, columns, selection, selectionArgs, groupBy, having, orderBy)` | A `Cursor` |
| Delete rows | `delete(table, whereClause, whereArgs)` | The number of rows deleted |
| Any other SQL | `execSQL(sql)` / `rawQuery(sql, args)` | Nothing / a `Cursor` |

A **`ContentValues`** is a map from column names to values, the "what to insert". A **`Cursor`** points to one row of a result at a time. It starts *before* the first row, `moveToNext()` moves it forward and returns `false` when there are no rows left, and `getString(i)`, `getLong(i)`… read column `i` of the current row. A cursor holds memory and a database connection until you **close** it.

### `onCreate` and `onResume`

From [Android Layouts](./exp2-layouts) you know that an Activity's `onCreate` runs once, when the screen is created, and `onResume` every time it comes back to the foreground. The list is read from the database in `onResume`, so it is refreshed when the user returns from the form.

## Step 1: Copy the Exp 2 project

This lab changes the [Android Layouts](./exp2-layouts) app. The layouts stay exactly as they are. To keep your Exp 2 version, work on a copy:

1. Close the project in Android Studio (**File → Close Project**).
2. In your file manager, copy the `Exp2CustomerList` folder (normally in `AndroidStudioProjects`) and name the copy `Exp4CustomerDatabase`.
3. In Android Studio, choose **Open** and select the `Exp4CustomerDatabase` folder.

The package name stays `edu.birzeit.encs5150.exp2`. That is fine: the package name identifies the app, not the lab. It does mean the copy *replaces* the Exp 2 app on the emulator when you run it, because two apps can't share a package name.

## Step 2: Remove the `ArrayList`

Open `Customer.java` and delete the static list and the comment above it:

```java
// Shared by both activities. It lives in memory only, so it is
// emptied when Android kills the app (Exp 4 stores it in SQLite).
public static final ArrayList<Customer> customersArrayList = new ArrayList<>(); // [!code --]
```

Also delete the now unused `import java.util.ArrayList;`. `Customer` is now a plain data class. Both Activities will show red errors where they used `customersArrayList`, and the next steps replace that code.

## Step 3: The database helper

1. Right-click the package `edu.birzeit.encs5150.exp2` → **New → Java Class**.
2. Name it `DataBaseHelper` and press <kbd>Enter</kbd>.
3. Replace the file's contents with:

<<< @/snippets/exp4/DataBaseHelper.java{java}

::: tip Let Android Studio write the skeleton
If you type only `public class DataBaseHelper extends SQLiteOpenHelper {}`, the class name turns red. Click it, press <kbd>Alt</kbd>+<kbd>Enter</kbd> and choose **Implement methods** to add `onCreate` and `onUpgrade`, then **Create constructor matching super**. Then fill them in from the code above.
:::

How it works:

1. **The constructor** passes the file name (`EXP4`), `null` for the cursor factory (use the default) and the version (`1`) to `SQLiteOpenHelper`. Because these never change, the Activities only pass a `Context`: `new DataBaseHelper(this)`.
2. **`onCreate`** creates the `CUSTOMER` table. `ID` is `INTEGER PRIMARY KEY`, so two customers can't have the same Id. `NOT NULL` makes the database itself reject a missing name, phone or gender.
3. **`onUpgrade`** drops the table and creates it again. That deletes every customer, which is fine for a lab. A real app would `ALTER TABLE` to keep the users' data.
4. **`insertCustomer`** fills a `ContentValues` with one entry per column and calls `insert`. The second argument, `null`, only matters if `contentValues` were empty. It returns whether the insert worked, so the form can report a duplicate Id.
5. **`getAllCustomers`** runs `SELECT * FROM CUSTOMER ORDER BY ID` with `query()`: `null` columns means all of them, the four `null`s mean no `WHERE`, `GROUP BY` or `HAVING`, and the last argument is the `ORDER BY`. It turns each row into a `Customer` and returns a `List`, so the Activities never touch a cursor. `try (Cursor cursor = …)` closes the cursor at the closing brace, even if an exception is thrown.
6. **`deleteCustomer`** runs `DELETE FROM CUSTOMER WHERE ID = ?`. Each `?` is replaced by the next value from the array. Never glue user input into SQL with `+`, because input such as `1 OR 1=1` would change the meaning of the query. This is called *SQL injection*.

## Step 4: Save from the form

Open `AddCustomerActivity.java` and replace the whole file with:

<<< @/snippets/exp4/AddCustomerActivity.java{java}

Compared with Exp 2, three things changed:

1. The Activity creates one `DataBaseHelper` in `onCreate` and closes it in `onDestroy`.
2. `Customer.customersArrayList.add(…)` became `dataBaseHelper.insertCustomer(newCustomer)`.
3. If the insert fails, the Id is already taken. The form shows an error on the Id field and stays open, instead of silently losing the customer.

## Step 5: Show and delete on the main screen

Open `MainActivity.java` and replace the whole file with:

<<< @/snippets/exp4/MainActivity.java{java}

`onCreate` builds the same view tree as in Exp 2. What changed:

1. **`showCustomers`** holds the old `onResume` code, but reads the list with `dataBaseHelper.getAllCustomers()` instead of from the `ArrayList`. It's a separate method because two places call it: `onResume`, and the dialog after a delete.
2. **The long-click listener** on each customer's `TextView` opens the dialog. Returning `true` tells Android the long press was handled, so it doesn't also count as a normal click.
3. **`confirmDelete`** builds an `AlertDialog` with a title, a message and two buttons. Only the **Delete** button has a listener: it deletes the row and rebuilds the list. **Cancel** has `null`, which just closes the dialog. Tapping outside the dialog or pressing Back also cancels.

::: tip Why ask first?
Deleting a row can't be undone, and a long press is easy to trigger by accident. Use a confirmation dialog for any action that destroys the user's data. Name the item in the message ("Delete Ahmad (Id 3)?") so the user knows exactly what will go.
:::

## Run and check

- The app opens with "No customers yet."
- Adding a customer returns to the list, which shows them. Customers are sorted by Id, whatever order you added them in.
- Adding a second customer with an Id that already exists shows "A customer with this Id already exists" on the Id field, and the form stays open.
- **Close the app completely**: open the recent apps screen and swipe it away, or press **Stop** in Android Studio. Open it again, and the customers are still there. In Exp 2 they were gone.
- Long-pressing a customer asks "Delete customer?" with their name. **Cancel** keeps them, **Delete** removes them from the list. They stay deleted after restarting the app.

### Look inside the database

While the app is running from Android Studio, open **View → Tool Windows → App Inspection** and choose the **Database Inspector** tab. Expand `EXP4` and double-click `CUSTOMER` to see the table's rows. Add a customer in the app and the table updates. You can also click **Open New Query Tab** and run SQL yourself, for example `SELECT * FROM CUSTOMER WHERE GENDER = 'Female'`.

## Differences from the manual

As printed, the manual's code doesn't compile, saves customers to a different database than the one it reads from, loses data without telling the user, and never closes its cursors. Here is what changed and why.

### 1. The `ID` column type

::: code-group

```java [Manual]
sqLiteDatabase.execSQL("CREATE TABLE CUSTOMER(ID LONG PRIMARY KEY,NAME TEXT, PHONE TEXT,GENDER TEXT)");
```

```java [This guide]
sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_CUSTOMER + " ("
        + COLUMN_ID + " INTEGER PRIMARY KEY, "
        + COLUMN_NAME + " TEXT NOT NULL, "
        + COLUMN_PHONE + " TEXT NOT NULL, "
        + COLUMN_GENDER + " TEXT NOT NULL)");
```

:::

SQLite has no `LONG` type. It accepts any type name and guesses a storage class from it, so `LONG` happens to work. But SQLite's integer type is `INTEGER`, and it already stores values up to 8 bytes, the size of a Java `long`. `INTEGER PRIMARY KEY` is also special: the column becomes the row's built-in ID, which makes lookups by Id fast.

The manual's code also had an extra `)` after the string, so it didn't compile as printed.

### 2. Duplicate Ids are reported

::: code-group

```java [Manual]
sqLiteDatabase.insert("CUSTOMER", null, contentValues);
// …
dataBaseHelper.insertCustomer(newCustomer);
```

```java [This guide]
return sqLiteDatabase.insert(TABLE_CUSTOMER, null, contentValues) != -1;
// …
if (!dataBaseHelper.insertCustomer(newCustomer)) {
    idEditText.setError("A customer with this Id already exists");
    return;
}
```

:::

`insert()` doesn't throw an exception when it fails. It logs an error and returns `-1`. The manual ignored that value, so adding a second customer with Id 1 closed the form as if it had worked, and the customer was simply missing from the list. Now the form tells the user and stays open.

### 3. Cursors are closed, and read by column name

::: code-group

```java [Manual]
Cursor allCustomersCursor = dataBaseHelper.getAllCustomers();
while (allCustomersCursor.moveToNext()) {
    // …
    "Id= " + allCustomersCursor.getString(0)
    // …
}
```

```java [This guide]
try (Cursor cursor = sqLiteDatabase.query(TABLE_CUSTOMER,
        null, null, null, null, null, COLUMN_ID)) {
    int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
    // …
    while (cursor.moveToNext()) {
        customers.add(new Customer(cursor.getLong(idIndex), …));
    }
}
```

:::

- The manual **never closed** its cursor, and leaked a new one every time `onResume` ran. Each open cursor holds memory until the garbage collector happens to find it. `try (…)` closes the cursor automatically.
- `getString(0)` depends on the column *order* in the table. If someone adds a column first, every index shifts and the screen shows the wrong data without any error. `getColumnIndexOrThrow("ID")` looks the column up by name, and fails loudly if it doesn't exist.
- `getAllCustomers` returns a `List<Customer>` instead of the cursor. The Activity no longer needs to know about columns, and the list reuses `Customer.toString()` from Exp 2.
- The rows are sorted with `ORDER BY ID`. Without `ORDER BY`, SQL doesn't promise any order.

### 4. The helper's arguments

::: code-group

```java [Manual]
// AddCustomerActivity
DataBaseHelper dataBaseHelper = new DataBaseHelper(AddCustomerActivity.this, "DB_NAME_EXP4", null, 1);
// MainActivity
DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this, "EXP4", null, 1);
```

```java [This guide]
dataBaseHelper = new DataBaseHelper(this);
```

:::

The manual repeats the name and version in every Activity, and the 2025 revision shows why that is risky: the form opens a database called `DB_NAME_EXP4` while the list reads one called `EXP4`. Each name is a separate file, so every customer is saved and none ever appears. (The 2019 manual used `EXP4` in both places.)

Likewise, if you bumped the version in one place and forgot the other, the two screens would disagree about the schema, and SQLite would throw `Can't downgrade database from version 2 to 1`. The name and version now live once in the helper.

The manual also created a new helper on every `onResume` and every button press and never closed it. This guide creates one per Activity in `onCreate` and closes it in `onDestroy`. In [Exp 6](/experiments/) you'll see the singleton pattern, which shares one helper across the whole app.

### 5. `onUpgrade` does something

The manual left `onUpgrade` empty. Then changing the schema and bumping the version does nothing: the old table stays, and the first query that uses a new column crashes. Here `onUpgrade` drops and recreates the table.

### 6. The confirmation dialog (new)

The manual lists "How to create Confirmation Dialogs" as an objective but never builds one. This guide uses one to confirm deleting a customer, which also adds the `delete()` call to the lab.

## Common problems

| Symptom | Cause and fix |
| --- | --- |
| `SQLiteException: table CUSTOMER has no column named …` or `no such table` | You changed `onCreate` after the database file already existed. `onCreate` runs only once. Uninstall the app (or **Settings → Apps → … → Storage → Clear storage**), or increase `DATABASE_VERSION` so `onUpgrade` runs. |
| `Can't downgrade database from version 2 to 1` | The installed database has a higher version than your code. Uninstall the app, or set the version back. |
| The form closes but the customer doesn't appear | `insert()` returned `-1`, usually because the Id already exists. Check the return value as in Step 4. Logcat shows `UNIQUE constraint failed: CUSTOMER.ID`. |
| The list doesn't update after adding a customer | The list is read in `onCreate` instead of `onResume`. |
| Customers appear twice | `removeAllViews()` is missing at the start of `showCustomers`. |
| `IllegalArgumentException: column 'ID' does not exist` | A column name in `getColumnIndexOrThrow` doesn't match the table. Use the constants everywhere. |
| `CursorIndexOutOfBoundsException: Index -1 requested` | A `getString` / `getLong` call before `moveToNext()`. The cursor starts before the first row. |
| `cannot find symbol: variable customersArrayList` | Some code still uses the old list. Replace both Activities as in Steps 4 and 5. |
| The database is empty after reinstalling | Expected: uninstalling deletes the app's private folder, database included. Running from Android Studio normally *updates* the app and keeps it. |

## Things to try

- Add an **Edit** option: tap a customer to open the form filled in with their data, and save with `update(table, values, "ID = ?", args)`.
- Add a search box that shows only customers whose name contains the text, using `query` with the selection `"NAME LIKE ?"` and the argument `"%" + text + "%"`.
- Add an `EMAIL` column. Increase `DATABASE_VERSION` to 2, and in `onUpgrade` run `ALTER TABLE CUSTOMER ADD COLUMN EMAIL TEXT` instead of dropping the table, so existing customers survive.
- Show the number of customers at the top of the list with `SELECT COUNT(*) FROM CUSTOMER`.
- Replace the `TextView` per customer with a **custom View**: an XML layout with the name in bold and the phone and gender below it, inflated with `LayoutInflater` for each customer.
