package edu.birzeit.encs5150.exp2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    // Database file name and schema version. Bump the version when the schema changes.
    private static final String DATABASE_NAME = "EXP4";
    private static final int DATABASE_VERSION = 1;

    // Table and column names, defined once so a typo can't creep into one query.
    private static final String TABLE_CUSTOMER = "CUSTOMER";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME = "NAME";
    private static final String COLUMN_PHONE = "PHONE";
    private static final String COLUMN_GENDER = "GENDER";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Runs once, the first time the database file is opened.
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_CUSTOMER + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_PHONE + " TEXT NOT NULL, "
                + COLUMN_GENDER + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Runs when DATABASE_VERSION goes up. For a lab, start over with an empty table.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        onCreate(sqLiteDatabase);
    }

    // Returns false if a customer with the same Id already exists.
    public boolean insertCustomer(Customer customer) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, customer.getCustomerId());
        contentValues.put(COLUMN_NAME, customer.getName());
        contentValues.put(COLUMN_PHONE, customer.getPhone());
        contentValues.put(COLUMN_GENDER, customer.getGender());
        // insert() returns the new row's ID, or -1 if the insert failed.
        return sqLiteDatabase.insert(TABLE_CUSTOMER, null, contentValues) != -1;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        // SELECT * FROM CUSTOMER ORDER BY ID. try-with-resources closes the cursor.
        try (Cursor cursor = sqLiteDatabase.query(TABLE_CUSTOMER,
                null, null, null, null, null, COLUMN_ID)) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME);
            int phoneIndex = cursor.getColumnIndexOrThrow(COLUMN_PHONE);
            int genderIndex = cursor.getColumnIndexOrThrow(COLUMN_GENDER);
            while (cursor.moveToNext()) {
                customers.add(new Customer(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(phoneIndex),
                        cursor.getString(genderIndex)));
            }
        }
        return customers;
    }

    public void deleteCustomer(long customerId) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        // "?" is filled in from the array, so the value can never break the SQL.
        sqLiteDatabase.delete(TABLE_CUSTOMER, COLUMN_ID + " = ?",
                new String[]{String.valueOf(customerId)});
    }
}
