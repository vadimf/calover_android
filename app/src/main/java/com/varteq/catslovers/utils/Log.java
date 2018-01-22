package com.varteq.catslovers.utils;


import android.os.Build;
import android.text.format.DateFormat;

import com.crashlytics.android.Crashlytics;
import com.varteq.catslovers.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Log {

    private static final String LOG_FILE = "logfile";

    public static boolean WRITE_TO_FILE = true;
    private static int MAX_MESSAGE_LENGTH = 3000;

    public static void d(String TAG, String mess, Exception e) {
        d(TAG, mess + "|" + e.toString());
    }

    public static void d(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            if (mess.length() > MAX_MESSAGE_LENGTH)
                for (String s : getMessageParts(mess))
                    android.util.Log.i(TAG, s);
            else android.util.Log.i(TAG, mess);
        }
        appendLogToFile("d", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            //Crashlytics.log("D/" + TAG + ": " + mess);
        }
    }

    public static void w(String TAG, String mess, Exception e) {
        w(TAG, mess + "|" + e.toString());
    }

    public static void w(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            if (mess.length() > MAX_MESSAGE_LENGTH)
                for (String s : getMessageParts(mess))
                    android.util.Log.w(TAG, s);
            else android.util.Log.w(TAG, mess);
        }
        appendLogToFile("w", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            //Crashlytics.log("W/" + TAG + ": " + mess);
        }
    }

    public static void i(String TAG, String mess, Exception e) {
        i(TAG, mess + "|" + e.toString());
    }

    public static void i(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            if (mess.length() > MAX_MESSAGE_LENGTH)
                for (String s : getMessageParts(mess))
                    android.util.Log.i(TAG, s);
            else android.util.Log.i(TAG, mess);
        }
        appendLogToFile("i", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            //Crashlytics.log("I/" + TAG + ": " + mess);
        }
    }

    // -- V

    public static void v(String TAG, String mess, Exception e) {
        v(TAG, mess + "|" + e.toString());
    }

    public static void v(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            if (mess.length() > MAX_MESSAGE_LENGTH)
                for (String s : getMessageParts(mess))
                    android.util.Log.v(TAG, s);
            else android.util.Log.v(TAG, mess);
        }
        appendLogToFile("v", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            // Crashlytics.log("V/" + TAG + ": " + mess);
        }
    }

    // -- E

    public static void e(String TAG, String mess, Exception e) {
        e(TAG, mess + "|" + e.toString());
    }

    public static void e(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            if (mess.length() > MAX_MESSAGE_LENGTH)
                for (String s : getMessageParts(mess))
                    android.util.Log.e(TAG, s);
            else android.util.Log.e(TAG, mess);
        }
        appendLogToFile("e", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            Crashlytics.log("E/" + TAG + ": " + mess);
        }
    }

    private static List<String> getMessageParts(String message) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < message.length(); i += MAX_MESSAGE_LENGTH) {
            int endIndex = message.length() - i > MAX_MESSAGE_LENGTH ? i + MAX_MESSAGE_LENGTH : message.length();
            parts.add(message.substring(i, endIndex));
        }
        return parts;
    }

    // --

    public static void toFile(String TAG, String mess, String fileName) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, mess);
        }
        appendLogToFile("d", TAG, mess, fileName);
    }

    public static void appendLogToFile(String level, String TAG, String mess, String fileName) {
        if (!WRITE_TO_FILE || mess.length() > 3000) {
            return;
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(getLogFile(), true));

            String time = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
            String print = time + "|" + level + "|" + TAG + "->" + mess;

            buf.append(print);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
        }
    }

    private static File logFile;
    private static int createLogFileAttempts = 0;

    public static File getLogFile() {
        String currentDate = (String) DateFormat.format("yyyy-MM-dd", new java.util.Date());
        if (logFile != null && logFile.getName().contains(currentDate)) {
            return logFile;
        } else if (createLogFileAttempts > 10) {
            return null;
        }
        String fileName = LOG_FILE + "_" + currentDate + ".txt";
        File dirFile = new File(StorageUtils.getAppExternalDataDirectoryPath() + "/logs");
        if (!dirFile.exists()) {
            try {
                dirFile.mkdir();
            } catch (Exception e) {
            }
        }
        logFile = new File(dirFile.getAbsolutePath() + "/" + fileName);
        if (!logFile.exists()) {
            try {
                createLogFileAttempts++;
                StorageUtils.clearDirectory(dirFile);
                logFile.createNewFile();

                appendLogToFile("I", "Device info",
                        "\n CatsLovers version : " + BuildConfig.VERSION_CODE +
                                "\n versionName : " + BuildConfig.VERSION_NAME +
                                "\n manufacturer : " + Build.MANUFACTURER +
                                "\n MODEL : " + Build.MODEL +
                                "\n PRODUCT : " + Build.PRODUCT +
                                "\n BRAND : " + Build.BRAND +
                                "\n RELEASE : " + Build.VERSION.RELEASE +
                                "\n androidSDK_INT : " + Build.VERSION.SDK_INT +
                                "\n androidVersion : " + Build.VERSION.CODENAME, null);
            } catch (IOException e) {
            }
        }
        return logFile;
    }
}
