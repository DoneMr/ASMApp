package com.done.plugin.transform

import com.done.plugin.InsectExtension
import com.done.plugin.transform.visitor.BaseVisitMethodImpl
import com.done.plugin.transform.visitor.MethodCostVisitor
import com.done.plugin.util.PLogger
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.ConcurrentHashMap

/**
 * File:com.done.plugin.transform.InsectClassVisitor
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/16
 */
class InsectClassVisitor(private val insectExtension: InsectExtension) : ClassNode(Opcodes.ASM7) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return InsectMethodVisitorProxy(super.api, methodVisitor, access, name!!, descriptor!!,
                this@InsectClassVisitor.name, insectExtension).also { proxy ->
            proxy.appendMethodImpl(MethodCostVisitor(super.api, methodVisitor, access, name, descriptor,
                    this@InsectClassVisitor.name, insectExtension))
        }
    }

    class InsectMethodVisitorProxy(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                                   val ownerClassName: String = "class", val insectExtension: InsectExtension)
        : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        private val mMethodImpls by lazy { ConcurrentHashMap<Int, BaseVisitMethodImpl>() }

        fun appendMethodImpl(methodVisitor: BaseVisitMethodImpl) {
            mMethodImpls[methodVisitor.hashCode()] = methodVisitor
        }

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            val visitAnnotation = super.visitAnnotation(descriptor, visible)
            for (methodImpl in mMethodImpls) {
                methodImpl.value.onVisitAnnotation(visitAnnotation, descriptor, visible)
            }
            return visitAnnotation
        }

        override fun onMethodEnter() {
            super.onMethodEnter()
            for (methodImpl in mMethodImpls) {
                methodImpl.value.onMethodEnter(this, super.mv)
            }
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            for (methodImpl in mMethodImpls) {
                methodImpl.value.onMethodExit(this, super.mv, opcode)
            }
        }

        /**
         * Visits the maximum stack size and the maximum number of local variables of the method.
         *
         * @param maxStack maximum stack size of the method.
         * @param maxLocals maximum number of local variables for the method.
         */
        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            var maxStackCount = 0
            var maxLocalsCount = 0
            for (methodImpl in mMethodImpls) {
                maxStackCount += methodImpl.value.getMaxStack()
                maxLocalsCount += methodImpl.value.getMaxLocals()
            }
            super.visitMaxs(maxStack + maxStackCount, maxLocals + maxLocalsCount)
        }
    }

//    class InsectMethodVisitorProxy(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
//                              val ownerClassName: String = "class", val insectExtension: InsectExtension)
//        : AdviceAdapter(api, methodVisitor, access, name, descriptor) {
//
//        private var mStartVar: Int? = null
//
//        private var mDoCost = false
//
//        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
//            var hasMethod = !insectExtension.annotationNames.isNullOrEmpty()
//            if (hasMethod) {
//                insectExtension.annotationNames?.forEach { annotation ->
//                    val relpaceAnno = "L${annotation.replace(".", "/")};"
//                    mDoCost = mDoCost || relpaceAnno == descriptor ?: "nonono"
//                }
//            }
//            mDoCost = mDoCost && (access and Opcodes.ACC_ABSTRACT == 0)
//            if (mDoCost) {
//                PLogger.i("${this.ownerClassName}.${this.name} 可以开始进行插桩")
//            }
//            return super.visitAnnotation(descriptor, visible)
//        }
//
//        override fun onMethodEnter() {
//            super.onMethodEnter()
//            if (mDoCost) {
//                invokeStatic(Type.getType("Landroid/os/SystemClock;"), Method("uptimeMillis", "()J"))
//                mStartVar = newLocal(Type.LONG_TYPE)
//                storeLocal(mStartVar!!)
//            }
//        }
//
//        override fun onMethodExit(opcode: Int) {
//            super.onMethodExit(opcode)
//            if (mDoCost) {
//                when {
//                    access and Opcodes.ACC_STATIC != Opcodes.ACC_STATIC -> {
//                        appendObjectMethodExit()
//                    }
//                    access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC -> {
//                        appendStaticMethodEixt()
//                    }
//                }
//            }
//        }
//
//        private fun appendObjectMethodExit() {
//            val methodVisitor = super.mv
//            methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
//            methodVisitor.visitInsn(DUP)
//            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
//            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
//            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
//            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false)
//            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
//            methodVisitor.visitLdcInsn("$name")
//            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
//            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
//            methodVisitor.visitVarInsn(LLOAD, mStartVar!!)
//            methodVisitor.visitMethodInsn(INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
//                    insectExtension.methodName, "(Ljava/lang/String;J)V", false)
//        }
//
//        private fun appendStaticMethodEixt() {
//            val name = "$ownerClassName.$name => cost ".replace("/", ".")
//            super.mv.visitLdcInsn(name)
//            super.mv.visitVarInsn(LLOAD, mStartVar!!)
//            super.mv.visitMethodInsn(INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
//                    insectExtension.methodName, "(Ljava/lang/String;J)V", false)
//        }
//
//        /**
//         * Visits the maximum stack size and the maximum number of local variables of the method.
//         *
//         * @param maxStack maximum stack size of the method.
//         * @param maxLocals maximum number of local variables for the method.
//         */
//        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
//            //var1 startTime, var2 String name var3 Insn string
//            super.visitMaxs(maxStack + 3, maxLocals + 3)
//        }
//    }
}