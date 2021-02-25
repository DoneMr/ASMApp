package com.done.plugin.transform.visitor

import com.done.plugin.extension.PExtensionWrapper
import com.done.plugin.util.PLogger
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.lang.StringBuilder

/**
 * File:com.done.plugin.transform.visitor.ClueMethodVisitor
 * Description:线索插桩对象
 *
 * @author maruilong
 * @date 2020/11/13
 */
class ClueMethodVisitor(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                        ownerClassName: String, insectExtension: PExtensionWrapper)
    : BaseVisitMethodImpl(api, methodVisitor, access, name, descriptor, ownerClassName, insectExtension) {

    private var mFilter = false

    override fun onVisitAnnotation(annotationVisitor: AnnotationVisitor?, descriptor: String?, visible: Boolean) {
    }

    override fun onMethodEnter(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?) {
        if (isABS()) {
            return
        }
        //白名单判断
        mFilter = filterPkg(ownerClassName) || filterClass(ownerClassName) || filterMethod("$ownerClassName.$name")
        if (mFilter) {
            return
        }
        methodVisitor?.let { visitor ->
            insertMethod(true, "$ownerClassName.$name", visitor)
        }
    }

    override fun onMethodExit(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, opcode: Int) {
        if (isABS()) {
            return
        }
        //白名单判断
        if (mFilter) {
            return
        }
        methodVisitor?.let { visitor ->
            insertMethod(false, "$ownerClassName.$name", visitor)
        }
    }

    /**
     * 插桩
     */
    private fun insertMethod(isMethodIn: Boolean, target: String, visitor: MethodVisitor) {
        mEx.blockEx?.let { blockConfig ->
            var className = (if (isMethodIn) blockConfig.insertMethodIn else blockConfig.insertMethodOut).replace(".", "/")
            var method = className.split("/").last()
            className = className.substring(0, (className.length - method.length) - 1)
            visitor.visitLdcInsn("$target${if (isMethodIn) "" else ":($mLineNumber)"}")
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, className, method, "(Ljava/lang/String;)V", false)
        }
    }

    /**
     * 过滤方法
     */
    private fun filterMethod(methodName: String): Boolean {
        mEx.blockEx?.let { blockConfig ->
            if (!blockConfig.methodWhitelist.isNullOrEmpty()) {
                blockConfig.methodWhitelist!!.forEach { method ->
                    if (methodName.replace(".", "/").contains(method)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 过滤包名
     */
    private fun filterPkg(ownerClassName: String): Boolean {
        mEx.blockEx?.let { blockConfig ->
            if (!blockConfig.packageWhitelist.isNullOrEmpty()) {
                blockConfig.packageWhitelist!!.forEach { pkg ->
                    if (ownerClassName.startsWith(pkg.replace(".", "/"))) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun filterClass(ownerClassName: String): Boolean {
        mEx.blockEx?.let { blockConfig ->
            if (!blockConfig.classWhitelist.isNullOrEmpty()) {
                blockConfig.classWhitelist!!.forEach { clazz ->
                    if (ownerClassName.startsWith(clazz.replace(".", "/"))) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun getMaxStack(): Int = 0

    override fun getMaxLocals(): Int = 0
}