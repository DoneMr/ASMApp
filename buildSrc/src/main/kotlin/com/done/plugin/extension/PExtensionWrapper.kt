package com.done.plugin.extension

import org.gradle.api.Action

/**
 * File:com.done.plugin.extension.PExtensionWrapper
 * Description:所有的配置放到这里
 *
 * @author maruilong
 * @date 2021/2/22
 */
open class PExtensionWrapper(var methodCostEx: MethodCostExtension = MethodCostExtension(), var blockEx: BlockExtension = BlockExtension()) {

    fun isBlockValid(): Boolean = blockEx.isValid()

    fun isCostValid(): Boolean = methodCostEx.isValid()

    fun methodCostEx(action: Action<MethodCostExtension>) {
        action.execute(methodCostEx)
    }

    fun blockEx(action: Action<BlockExtension>) {
        action.execute(blockEx)
    }

}