package com.done.testlibrary.has;

import com.done.testlibrary.Utils;

/**
 * File:com.done.testlibrary.has.TestNoneMethod
 * Description:xxx
 *
 * @author maruilong
 * @date 2021/2/22
 */
public class TestNoneMethod {

    public void testMethod1() {
        Utils.methodIn("com.done.testlibrary.has.TestNoneMethod.testMethod1");
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
        Utils.methodOut("com.done.testlibrary.has.TestNoneMethod.testMethod1");
    }

    public void testMethod2() {
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
    }

    public static void testMethod3() {
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
        System.out.println("123");
    }
}
