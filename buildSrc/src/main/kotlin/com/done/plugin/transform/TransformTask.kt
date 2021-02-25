package com.done.plugin.transform

import java.util.concurrent.Callable

/**
 * File:com.done.plugin.transform.TransformTask
 * Description:包装下运行task
 *
 * @author maruilong
 * @date 2021/2/20
 */
open class TransformTask<R>(private val task: OnExecuteListener<R>) : Callable<R> {

    interface OnExecuteListener<R> {
        fun onExecute(): R
    }

    override fun call(): R = task.onExecute()
}