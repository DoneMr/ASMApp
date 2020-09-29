package com.done.plugin

public class InsectExtension {

    public static final String CONFIG_NAME = "insectConfig"

    /**
     * 作为标记的注解名称 如：com.android.Nullable
     */
    public String[] annotationNames

    /**
     * 要调用的全路径类名
     */
    public String methodOwner

    /**
     * 要调用的方法名
     */
    public String methodName

    /**
     * 只有debug为true才会进行插桩
     */
    public boolean isDebug = false
}