package com.done.plugin.extension

/**
 * File:com.done.plugin.extension.BlockExtension
 * Description:xxx
 *
 * @author maruilong
 * @date 2021/2/22
 */
class BlockExtension {

    var isDebug = true

    /**
     * 类白名单
     */
    var classWhitelist: Array<String>? = null

    /**
     * 方法白名单
     */
    var methodWhitelist: Array<String>? = null

    /**
     *包名白名单
     */
    var packageWhitelist: Array<String>? = null

    /**
     * 方法进入时需要调用的方法
     */
    var insertMethodIn: String = ""

    /**
     * 方法退出时需要调用的方法
     */
    var insertMethodOut: String = ""

    constructor(isDebug: Boolean = false, classWhitelist: Array<String>? = null, methodWhitelist: Array<String>? = null, packageWhitelist: Array<String>? = null, insertMethodIn: String = "", insertMethodOut: String = "") {
        this.isDebug = isDebug
        this.classWhitelist = classWhitelist
        this.methodWhitelist = methodWhitelist
        this.packageWhitelist = packageWhitelist
        this.insertMethodIn = insertMethodIn
        this.insertMethodOut = insertMethodOut
    }

    /**
     * 判断自身的数据是否有效
     */
    fun isValid(): Boolean = insertMethodIn.isNotBlank() && insertMethodIn.isNotEmpty() && insertMethodOut.isNotBlank() && insertMethodOut.isNotEmpty() && isDebug

}