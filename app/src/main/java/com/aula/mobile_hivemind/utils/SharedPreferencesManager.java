// SharedPreferencesManager.java
package com.aula.mobile_hivemind.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "HiveMindPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    // Métodos para User ID
    public void setUserId(int userId) {
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    // Métodos para User Type
    public void setUserType(int userType) {
        sharedPreferences.edit().putInt(KEY_USER_TYPE, userType).apply();
    }

    public int getUserType() {
        return sharedPreferences.getInt(KEY_USER_TYPE, 1); // Default: Usuário Comum
    }

    // Métodos para User Name
    public void setUserName(String userName) {
        sharedPreferences.edit().putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    // Métodos para User Email
    public void setUserEmail(String userEmail) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, userEmail).apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    // Métodos para Login Status
    public void setLoggedIn(boolean isLoggedIn) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Limpar todos os dados (logout)
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}