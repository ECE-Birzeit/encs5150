package edu.birzeit.encs5150.exp6;

import android.content.Context;
import android.content.SharedPreferences;

/** The one place in the app that reads and writes the shared preferences file. */
public class SharedPrefManager {

    // The file is /data/data/edu.birzeit.encs5150.exp6/shared_prefs/exp6_prefs.xml
    private static final String SHARED_PREF_NAME = "exp6_prefs";

    // The keys. Both Activities use these constants, so a typo is a compile error.
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_REMEMBER_PASSWORD = "remember_password";
    public static final String KEY_SAVE_COUNT = "save_count";

    // The single instance. static: it belongs to the class, not to an object.
    private static SharedPrefManager instance = null;

    private final SharedPreferences sharedPreferences;

    // private: no other class can write "new SharedPrefManager(...)".
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    // The only way to get a SharedPrefManager. The first call creates it,
    // and every later call returns that same object.
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void writeString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String readString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void writeInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int readInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void writeBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }
}
