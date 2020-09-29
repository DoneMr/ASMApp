package com.done.asm;

import android.app.Application;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * File:com.done.asm.MyApplication
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/6/8
 */
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    @Cost
    public void onCreate() {
        super.onCreate();
        int i = testReturn();
    }

    @Cost
    private int testReturn() {
        Random random = new Random();
        int nextInt = random.nextInt(100);
        if (nextInt % 2 == 0) {
            return 1;
        } else {
            return 2;
        }
    }

    @Cost
    private void tryCatchMethod() {
        try {
            FileOutputStream f = new FileOutputStream("file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
