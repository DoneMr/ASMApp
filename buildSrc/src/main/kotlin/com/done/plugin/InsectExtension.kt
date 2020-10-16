package com.done.plugin

/**
 * File:com.done.plugin.InsectExtension
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/12
 */
class InsectExtension {

    /**
     * 作为标记的注解名称 如：com.android.Nullable
     */
    var annotationNames: Array<String>? = null

    /**
     * 要调用的全路径类名
     */
    var methodOwner: String = ""

    /**
     * 要调用的方法名
     */
    var methodName: String = ""

    /**
     * 只有debug为true才会进行插桩
     */
    var isDebug: Boolean = false
}