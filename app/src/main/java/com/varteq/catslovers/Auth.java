package com.varteq.catslovers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Auth {

    private static String USER_NAME_KEY = "user_name";
    private static String USER_EMAIL_KEY = "user_email";
    private static String USER_AVATAR_KEY = "user_avatar";

    private static String USER_LOGGED_IN_KEY = "user_logged_in";

    public static void saveUser(Context context, String name, String email) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(USER_NAME_KEY, name)
                .putString(USER_EMAIL_KEY, email)
                .apply();
    }

    public static void saveUserAvatar(Context context, Uri avatar) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(USER_AVATAR_KEY, avatar.toString())
                .apply();
    }

    public static void setUserLogin(Context context, boolean isLoggedIn) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(USER_LOGGED_IN_KEY, isLoggedIn).apply();
    }

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(USER_LOGGED_IN_KEY, false);
    }

    public static String getUserName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USER_NAME_KEY, "");
    }

    public static Uri getUserAvatar(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Uri.parse(preferences.getString(USER_AVATAR_KEY, ""));
    }
}
