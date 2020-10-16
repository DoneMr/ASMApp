package com.done.plugin.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation

/**
 * File:com.done.plugin.transform.ITransform2
 * Description:执行transform逻辑
 *
 * @author maruilong
 * @date 2020/10/13
 */
internal interface ITransform {

    /**
     * 辅助实现transform的核心逻辑
     */
    fun doTransform(transformInvocation: TransformInvocation, input: QualifiedContent)
}