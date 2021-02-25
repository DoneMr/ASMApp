package com.done.testlibrary.ui;

import android.util.Log;

import com.done.testlibrary.LibJCost;

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
        Log.d("asd", "asd");
        Log.d("asd", "asd");
        Log.d("asd", "asd");
        Log.d("asd", "asd");
    }
}
