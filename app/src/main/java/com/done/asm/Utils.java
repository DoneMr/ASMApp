package com.done.asm;

import android.os.SystemClock;
import android.util.Log;

/**
 * File:com.done.asm.Utils
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/9/24
 */
public class Utils {

    public static void printCost(String method, long start) {
        Log.d("InsectCost", String.format("%s method cost: %sms", method, (SystemClock.uptimeMillis() - start)));
    }
}
