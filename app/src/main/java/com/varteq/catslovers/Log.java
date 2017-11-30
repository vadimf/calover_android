package com.varteq.catslovers;


import android.os.Environment;
import android.text.format.DateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {

    private static final String LOG_FILE = "logfile";

    public static boolean WRITE_TO_FILE = false;

    public static void d(Object o, String TAG, String mess) {
        if (o.getClass().getSimpleName().contains("Adapter")) {

        } else {
            d(TAG, mess);
        }
    }

    public static void d(String TAG, String mess, Exception e) {
        d(TAG, mess + "|" + e.toString());
    }

    public static void d(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, mess);
        }
        appendLogToFile("d", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            // Crashlytics.log("D/" + TAG + ": " + mess);
        }
    }

    public static void w(String TAG, String mess, Exception e) {
        w(TAG, mess + "|" + e.toString());
    }

    public static void w(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            android.util.Log.w(TAG, mess);
        }
        appendLogToFile("w", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            // Crashlytics.log("W/" + TAG + ": " + mess);
        }
    }

    public static void i(String TAG, String mess, Exception e) {
        i(TAG, mess + "|" + e.toString());
    }

    public static void i(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(TAG, mess);
        }
        appendLogToFile("i", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            // Crashlytics.log("I/" + TAG + ": " + mess);
        }
    }

    // -- V

    public static void v(String TAG, String mess, Exception e) {
        v(TAG, mess + "|" + e.toString());
    }

    public static void v(String TAG, String mess) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(TAG, mess);
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
            android.util.Log.e(TAG, mess);
        }
        appendLogToFile("e", TAG, mess, LOG_FILE);
        if (!BuildConfig.DEBUG) {
            //Crashlytics.log("E/" + TAG + ": " + mess);
        }
    }

    // --

    public static void toFile(String TAG, String mess, String fileName) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, mess);
        }
        appendLogToFile("d", TAG, mess, fileName);
    }

    public static void appendLogToFile(String level, String TAG, String mess, String fileName) {
        if (!WRITE_TO_FILE) {
            return;
        }
        String time = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
        String print = time + "|" + level + "|" + TAG + "->" + mess;
        fileName += "_" + DateFormat.format("yyyy-MM-dd", new java.util.Date()) + ".txt";
        File dirFile = Environment.getExternalStorageDirectory();
        if (!dirFile.exists()) {
            try {
                dirFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File logFile = new File(dirFile.getAbsolutePath() + "/" + fileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(print);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
