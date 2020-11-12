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
//            var maxStackCount = 0
//            var maxLocalsCount = 0
//            for (methodImpl in mMethodImpls) {
//                maxStackCount += methodImpl.value.getMaxStack()
//                maxLocalsCount += methodImpl.value.getMaxLocals()
//            }
//            super.visitMaxs(maxStack + maxStackCount, maxLocals + maxLocalsCount)
            super.visitMaxs(maxStack, maxLocals)
        }
    }
}