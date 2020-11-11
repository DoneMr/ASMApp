package com.done.testlibrary.ui;

import android.os.SystemClock;
import android.util.Log;

import com.done.testlibrary.LibJCost;
import com.done.testlibrary.Utils;

/**
 * File:com.done.testlibrary.ui.JavaCost
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/16
 */
class JavaCost {

    @LibJCost
    public static void testMethod() {
        Log.d("asd", "asd");
        Log.d("asd", "asd");
        Log.d("asd", "asd");
    }

    @LibJCost
    public void testAsm() {
        long startTime = SystemClock.uptimeMillis();
        Utils.logCost(getClass().getName() + "testAsm", startTime);
    }
}
