package com.varteq.catslovers.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

public class Profile {

    private static String USER_NAME_KEY = "user_name";
    private static String USER_EMAIL_KEY = "user_email";
    private static String USER_AVATAR_KEY = "user_avatar";
    private static String USER_PHONE = "user_phone";
    private static String USER_ID = "user_id";
    private static String QB_USER_ID = "qb_user_id";
    private static String USER_STATION = "user_station";

    private static String USER_LOCATION_PROVIDER = "user_location_provider";
    private static String USER_LOCATION_LAT = "user_location_lat";
    private static String USER_LOCATION_LNG = "user_location_lng";

    private static String USER_LOGGED_IN_KEY = "user_logged_in";

    private static String TOKEN_KEY = "token";
    private static String PET_COUNT_KEY = "pet_cnt";

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public static void saveUser(Context context, String name, String email) {
        getDefaultSharedPreferences(context).edit().putString(USER_NAME_KEY, name)
                .putString(USER_EMAIL_KEY, email)
                .apply();
    }

    public static void saveUserAvatar(Context context, String avatar) {
        getDefaultSharedPreferences(context).edit().putString(USER_AVATAR_KEY, avatar).apply();
    }

    public static void setUserLogin(Context context, boolean isLoggedIn) {
        getDefaultSharedPreferences(context).edit().putBoolean(USER_LOGGED_IN_KEY, isLoggedIn).apply();
    }

    public static boolean isUserLoggedIn(Context context) {
        return getDefaultSharedPreferences(context).getBoolean(USER_LOGGED_IN_KEY, false);
    }

    public static String getUserName(Context context) {
        return getDefaultSharedPreferences(context).getString(USER_NAME_KEY, "");
    }

    public static String getEmail(Context context) {
        return getDefaultSharedPreferences(context).getString(USER_EMAIL_KEY, "");
    }

    public static String getUserAvatar(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(USER_AVATAR_KEY, "");
    }

    public static void setAuthToken(Context context, String token) {
        getDefaultSharedPreferences(context).edit().putString(TOKEN_KEY, token).apply();
    }

    public static String getAuthToken(Context context) {
        return getDefaultSharedPreferences(context).getString(TOKEN_KEY, "");
    }

    public static void setUserPetCount(Context context, int petCnt) {
        getDefaultSharedPreferences(context).edit().putInt(PET_COUNT_KEY, petCnt).apply();
    }

    public static int getUserPetCount(Context context) {
        return getDefaultSharedPreferences(context).getInt(PET_COUNT_KEY, 0);
    }

    public static void setUserPhone(Context context, String phone) {
        getDefaultSharedPreferences(context).edit().putString(USER_PHONE, phone).apply();
    }

    public static String getUserPhone(Context context) {
        return getDefaultSharedPreferences(context).getString(USER_PHONE, "");
    }

    public static void setLocation(Context context, Location location) {
        if (getLocation(context) != null) return;
        getDefaultSharedPreferences(context).edit().putString(USER_LOCATION_PROVIDER, location.getProvider()).apply();
        putDouble(context, USER_LOCATION_LAT, location.getLatitude());
        putDouble(context, USER_LOCATION_LNG, location.getLongitude());
    }

    public static Location getLocation(Context context) {
        String provider = getDefaultSharedPreferences(context).getString(USER_LOCATION_PROVIDER, "");
        double lat = getDouble(context, USER_LOCATION_LAT, 0);
        double lng = getDouble(context, USER_LOCATION_LNG, 0);
        if (provider.isEmpty())
            return null;
        else {
            Location l = new Location(provider);
            l.setLatitude(lat);
            l.setLongitude(lng);
            return l;
        }
    }

    private static void putDouble(Context context, final String key, final double value) {
        getDefaultSharedPreferences(context).edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    private static double getDouble(Context context, final String key, final double defaultValue) {
        return Double.longBitsToDouble(getDefaultSharedPreferences(context).getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public static void setUserId(Context context, String userId) {
        getDefaultSharedPreferences(context).edit().putString(USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        return getDefaultSharedPreferences(context).getString(USER_ID, "");
    }

    public static void setQBUserId(Context context, int userId) {
        getDefaultSharedPreferences(context).edit().putInt(QB_USER_ID, userId).apply();
    }

    public static int getQBUserId(Context context) {
        return getDefaultSharedPreferences(context).getInt(QB_USER_ID, -1);
    }

    public static void setUserStation(Context context, String stationId) {
        getDefaultSharedPreferences(context).edit().putString(USER_STATION, stationId).apply();
    }

    public static String getUserStation(Context context) {
        return getDefaultSharedPreferences(context).getString(USER_STATION, "");
    }
}
