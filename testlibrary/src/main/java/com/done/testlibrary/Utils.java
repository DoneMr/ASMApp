package com.done.testlibrary;

import android.os.SystemClock;
import android.util.Log;

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
}
