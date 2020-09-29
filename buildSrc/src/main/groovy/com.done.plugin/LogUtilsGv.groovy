package com.done.plugin

public class LogUtilsGv {

    public static void log(String message) {
        log(message, true)
    }

    public static void log(String message, boolean hasDivider) {
        println hasDivider ? ("=====" + message + "=====") : (message)
    }
}