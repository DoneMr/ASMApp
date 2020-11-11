package com.done.plugin.transform.visitor

import com.done.plugin.InsectExtension
import com.done.plugin.util.PLogger
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

/**
 * File:com.done.plugin.transform.visitor.MethodCostVisitor
 * Description:方法耗时插桩
 *
 * @author maruilong
 * @date 2020/11/12
 */
class MethodCostVisitor(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                        ownerClassName: String, insectExtension: InsectExtension)
    : BaseVisitMethodImpl(api, methodVisitor, access, name, descriptor, ownerClassName, insectExtension) {

    private var mStartVar: Int? = null

    private var mDoCost = false

    override fun onVisitAnnotation(annotationVisitor: org.objectweb.asm.AnnotationVisitor?, descriptor: String?, visible: Boolean) {
        val hasMethod = !insectExtension.annotationNames.isNullOrEmpty()
        if (hasMethod) {
            insectExtension.annotationNames?.forEach { annotation ->
                val relpaceAnno = "L${annotation.replace(".", "/")};"
                mDoCost = mDoCost || relpaceAnno == descriptor ?: "nonono"
            }
        }
        mDoCost = mDoCost && !isAbstract()
        if (mDoCost) {
            PLogger.i("${this.ownerClassName}.${this.name} 可以开始进行插桩")
        }
    }

    override fun onMethodEnter(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?) {
        adviceAdapter?.let { adpater ->
            methodVisitor?.let { methodVisitor ->
                if (mDoCost) {
                    adpater.invokeStatic(Type.getType("Landroid/os/SystemClock;"), Method("uptimeMillis", "()J"))
                    mStartVar = adpater.newLocal(Type.LONG_TYPE)
                    adpater.storeLocal(mStartVar!!)
                }
            }
        }
    }

    override fun onMethodExit(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, opcode: Int) {
        adviceAdapter?.let { adapter ->
            methodVisitor?.let { methodVisitor ->
                if (mDoCost) {
                    if (isStatic()) {
                        appendStaticMethodExit(adapter, methodVisitor)
                    } else {
                        appendObjectMethodExit(adapter, methodVisitor)
                    }
                }
            }
        }
    }

    private fun appendStaticMethodExit(adapter: AdviceAdapter, methodVisitor: MethodVisitor) {
        val name = "$ownerClassName.$name => cost ".replace("/", ".")
        methodVisitor.visitLdcInsn(name)
        methodVisitor.visitVarInsn(AdviceAdapter.LLOAD, mStartVar!!)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
                insectExtension.methodName, "(Ljava/lang/String;J)V", false)
    }

    private fun appendObjectMethodExit(adapter: AdviceAdapter, methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(AdviceAdapter.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(AdviceAdapter.DUP)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn("$name")
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitVarInsn(AdviceAdapter.LLOAD, mStartVar!!)
        methodVisitor.visitMethodInsn(AdviceAdapter.INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
                insectExtension.methodName, "(Ljava/lang/String;J)V", false)
    }

    override fun getMaxLocals(): Int = 3

    override fun getMaxStack(): Int = 3
}