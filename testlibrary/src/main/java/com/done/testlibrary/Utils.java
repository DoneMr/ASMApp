package com.done.testlibrary;

import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * File:com.done.testlibrary.Utils
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/16
 */
public class Utils {
    public static void logCost(String methodName, long startTime) {
        Log.d("cost", String.format("%s cost %s", methodName, SystemClock.uptimeMillis() - startTime));
    }

    public void logClueMethod() {
        methodIn("com.done.testlibrary.Utils.logClueMethod");
    }

    public Lock logClueMethod2() {
        return new ReentrantLock();
    }

    public static void methodIn(String methodIn) {
        Log.d("cost", "method in:" + methodIn);
    }

    public static void methodOut(String methodOut) {
        Log.d("cost", "method out:" + methodOut);

    }
}
