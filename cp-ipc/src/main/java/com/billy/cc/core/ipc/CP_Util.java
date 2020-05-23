package com.billy.cc.core.ipc;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CP_Util {
    private static final String TAG = "IPCCaller";
    private static final String VERBOSE_TAG = "IPCCaller_VERBOSE";
    private static final String PROCESS_UNKNOWN = "UNKNOWN";
    private static String processName = null;
    static boolean DEBUG = false;
    static boolean VERBOSE_LOG = false;

    static {
        if (BuildConfig.DEBUG) {
            DEBUG = true;
            VERBOSE_LOG = true;
        }
    }

    public static void log(String s, Object... args) {
        if (DEBUG) {
            s = format(s, args);
            Log.i(TAG, "(" + getProcessName() +")(" + Thread.currentThread().getName() + ")"
                    + " >>>> " + s);
        }
    }

    public static void verboseLog(String s, Object... args) {
        if (VERBOSE_LOG) {
            s = format(s, args);
            Log.i(VERBOSE_TAG, "(" + getProcessName() +")(" + Thread.currentThread().getName() + ")"
                    + " >>>> " + s);
        }
    }

    public static void logError(String s, Object... args) {
        if (DEBUG) {
            s = format(s, args);
            Log.e(TAG, "(" + getProcessName() +")(" + Thread.currentThread().getName() + ")"
                    + " >>>> " + s);
        }
    }

    public static void printStackTrace(Throwable t) {
        if (DEBUG && t != null) {
            t.printStackTrace();
        }
    }

    public static String getProcessName() {
        if (processName != null) {
            return processName;
        }
        String ret = getProcessName(android.os.Process.myPid());
        if (!TextUtils.isEmpty(ret)) {
            processName = ret;
        } else {
            processName = PROCESS_UNKNOWN;
        }
        return processName;
    }

    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Exception e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return PROCESS_UNKNOWN;
    }

    private static String format(String s, Object... args) {
        try {
            if (args != null && args.length > 0) {
                s = String.format(s, args);
            }
        } catch (Exception e) {
            CP_Util.printStackTrace(e);
        }
        return s;
    }
}
