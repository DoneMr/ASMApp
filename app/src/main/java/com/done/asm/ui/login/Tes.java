package com.done.asm.ui.login;

import android.util.Log;

/**
 * File:com.done.asm.ui.login.Tes
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/6/9
 */
class Tes {
        //1. JVM 数据结构 字节码 有一定了解 门槛低
        //2. 打包流程 java - class - dex
    public void main() {
        long startTime = System.currentTimeMillis();
        a();
        Log.i("123", ("onCreate cost :" + (System.currentTimeMillis() - startTime)));
    }

    private void b() {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        Log.i("123", "on Create cost:" + (endTime - startTime));
    }

    private void a() {
        Log.i(" after method exec", " method in onMethodExit");
    }
}
