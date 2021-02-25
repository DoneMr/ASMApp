package com.done.plugin.extension

import org.gradle.api.Action

/**
 * File:com.done.plugin.extension.InsectExtension
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/12
 */
class MethodCostExtension {

    /**
     * 作为标记的注解名称 如：com.android.Nullable
     */
    var annotationNames: Array<String>? = null

    fun annotationNames(annotationNames: Array<String>?) {
        this.annotationNames = annotationNames
    }

    /**
     * 要调用的全路径类名
     */
    var methodOwner: String = ""

    fun methodOwner(methodOwner: String = ""){
        this.methodOwner = methodOwner
    }

    /**
     * 要调用的方法名
     */
    var methodName: String = ""

    fun methodName(methodName: String){
        this.methodName = methodName
    }

    /**
     * 只有debug为true才会进行插桩
     */
    var isDebug: Boolean = false

    fun isDebug(isDebug: Boolean = false){
        this.isDebug = isDebug
    }

    constructor(annotationNames: Array<String>? = null, methodOwner: String = "", methodName: String="", isDebug: Boolean = false) {
        this.annotationNames = annotationNames
        this.methodOwner = methodOwner
        this.methodName = methodName
        this.isDebug = isDebug
    }

    fun isValid() = methodOwner.isNotBlank() && methodOwner.isNotEmpty() && methodName.isNotBlank() && methodName.isNotEmpty() && !annotationNames.isNullOrEmpty() && isDebug
}