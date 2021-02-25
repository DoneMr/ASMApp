package com.done.plugin.util

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * File:com.done.plugin.util.ThreadPoolMngr
 * Description:线程池单例
 *
 * @author maruilong
 * @date 2021/2/20
 */
object ThreadPoolMngr {

    private val CORE_COUNT by lazy {
        Runtime.getRuntime().availableProcessors()
    }

    private const val ALIVE_TIME = 60L

    private val mExecutors by lazy {
        ForkJoinPool.commonPool()
    }

    fun execute(tasks: List<Runnable>) {
        if (tasks.isNotEmpty()) {
            for (task in tasks) {
                execute(task)
            }
        }
    }

    fun execute(task: Runnable) {
        mExecutors.execute(task)
    }

    fun <T> submit(callable: Callable<T>): ForkJoinTask<T> {
        return mExecutors.submit(callable)
    }

    fun destroy() {
        try {
            if (!mExecutors.isShutdown) {
                mExecutors.shutdown()
            }
        } catch (e: Exception) {
        }
    }

    class TF : ThreadFactory {
        val tCount by lazy {
            AtomicInteger()
        }

        override fun newThread(r: Runnable): Thread = Thread("DInsectThread-${tCount.getAndIncrement()}").apply {
            setUncaughtExceptionHandler { t, e -> PLogger.e("${t?.name ?: "thread-null"} has exception:$e") }
        }

    }
}