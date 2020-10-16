package com.done.plugin.util

/**
 * File:com.done.plugin.util.PLogger
 * Description:日志工具类
 *
 * @author maruilong
 * @date 2020/10/12
 */
class PLogger {

    companion object {

        const val ANSI_RESET = "\u001B[0m"
        const val ANSI_BLACK = "\u001B[30m"
        const val ANSI_RED = "\u001B[31m"
        const val ANSI_GREEN = "\u001B[32m"
        const val ANSI_YELLOW = "\u001B[33m"
        const val ANSI_BLUE = "\u001B[34m"
        const val ANSI_PURPLE = "\u001B[35m"
        const val ANSI_CYAN = "\u001B[36m"
        const val ANSI_WHITE = "\u001B[37m"

        fun log(message: String, e: Exception? = null) {
            println("${if (e == null) ANSI_GREEN else ANSI_RED}[Insect Plugin] => $message ${if (e == null) "" else "error:$e"}$ANSI_RESET")
        }

        fun i(message: String) {
            println("$ANSI_YELLOW[Insect Plugin] => $message$ANSI_RESET")
        }

        fun e(message: String) {
            println("$ANSI_RED[Insect Plugin] => $message$ANSI_RESET")
        }
    }
}