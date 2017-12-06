package com.varteq.catslovers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Profile {

    private static String USER_NAME_KEY = "user_name";
    private static String USER_EMAIL_KEY = "user_email";
    private static String USER_AVATAR_KEY = "user_avatar";
    private static String USER_PHONE = "user_phone";

    private static String USER_LOGGED_IN_KEY = "user_logged_in";

    private static String TOKEN_KEY = "token";
    private static String PET_COUNT_KEY = "pet_cnt";

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

    public static String getEmail(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USER_EMAIL_KEY, "");
    }

    public static Uri getUserAvatar(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Uri.parse(preferences.getString(USER_AVATAR_KEY, ""));
    }

    public static void setAuthToken(Context context, String token) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(TOKEN_KEY, token).apply();
    }

    public static String getAuthToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(TOKEN_KEY, "");
    }

    public static void setUserPetCount(Context context, int petCnt) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(PET_COUNT_KEY, petCnt).apply();
    }

    public static int getUserPetCount(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(PET_COUNT_KEY, 0);
    }

    public static void setUserPhone(Context context, String phone) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(USER_PHONE, phone).apply();
    }

    public static String getUserPhone(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USER_PHONE, "");
    }
}
