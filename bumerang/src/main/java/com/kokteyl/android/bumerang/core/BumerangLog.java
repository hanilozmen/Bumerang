package com.kokteyl.android.bumerang.core;

import android.util.Log;

public final class BumerangLog {
    private static LogLevel mLogLevel = LogLevel.VERBOSE;
    private static final String TAG = "Bumerang";

    public static void setLogLevel(LogLevel logLevel) {
        mLogLevel = logLevel;
    }

    public static void e(String message) {
        e(message, null);
    }

    public static void e(BumerangError error, Throwable t) {
        if(error ==null) return;
        e("Error: " + error.getMessage()+ " , Code: " + error.getCode(), t);
    }

    public static void e(String message, Throwable t) {
        if(mLogLevel.ordinal() > LogLevel.ERROR.ordinal())
            return;
        Log.e(TAG, message, t);
    }

    public static void w(String message) {
        w(message, null);
    }

    public static void w(String message, Throwable t) {
        if(mLogLevel.ordinal() > LogLevel.WARNING.ordinal())
            return;
        Log.w(TAG, message, t);
    }

    public static void i(String message) {
        if(mLogLevel.ordinal() > LogLevel.INFO.ordinal())
            return;
        Log.i(TAG, message);
    }

    public static void d(String message) {
        if(mLogLevel.ordinal() > LogLevel.DEBUG.ordinal())
            return;
        Log.d(TAG, message);
    }


    public static void v(String message) {
        if(mLogLevel.ordinal() > LogLevel.VERBOSE.ordinal())
            return;
        Log.v(TAG, message);
    }


    public enum LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE
    }
}
