package com.done.plugin.transform

import com.done.plugin.extension.MethodCostExtension
import com.done.plugin.extension.PExtensionWrapper
import com.done.plugin.transform.visitor.BaseVisitMethodImpl
import com.done.plugin.transform.visitor.ClueMethodVisitor
import com.done.plugin.transform.visitor.MethodCostVisitor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList

/**
 * File:com.done.plugin.transform.InsectClassVisitor
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/16
 */
class InsectClassVisitor(private val insectExtension: PExtensionWrapper) : ClassNode(Opcodes.ASM7) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return InsectMethodVisitorProxy(super.api, methodVisitor, access, name!!, descriptor!!,
                this@InsectClassVisitor.name, insectExtension.methodCostEx!!).also { proxy ->
            //最后一次插桩，针对方法进入就是插在第一行，针对方法退出就是插在最后时机的第一个行
            if (insectExtension.isBlockValid()) {
                proxy.appendMethodImpl(ClueMethodVisitor(super.api, methodVisitor, access, name, descriptor,
                        this@InsectClassVisitor.name, insectExtension))
            }
            //方法耗时
            if (insectExtension.isCostValid()) {
                proxy.appendMethodImpl(MethodCostVisitor(super.api, methodVisitor, access, name, descriptor,
                        this@InsectClassVisitor.name, insectExtension))
            }
        }
    }

    class InsectMethodVisitorProxy(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                                   val ownerClassName: String = "class", val methodCostExtension: MethodCostExtension)
        : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        override fun visitInsn(opcode: Int) {
            super.visitInsn(opcode)
        }

        private fun isEmptyMethod(list: InsnList): Boolean {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val node = iterator.next()
                val opcode = node.opcode
                if (opcode == -1) {
                    continue
                } else {
                    return false
                }
            }
            return true
        }

        private val mMethodImpls by lazy {
            LinkedHashMap<Int, BaseVisitMethodImpl>()
        }

        fun appendMethodImpl(methodVisitor: BaseVisitMethodImpl) {
            putVisitorImpl(methodVisitor)
        }

        private fun putVisitorImpl(methodVisitor: BaseVisitMethodImpl) {
            synchronized(mMethodImpls) {
                mMethodImpls[methodVisitor.hashCode()] = methodVisitor
            }
        }

        override fun visitLineNumber(line: Int, start: Label?) {
            super.visitLineNumber(line, start)
            for (methodImpl in mMethodImpls) {
                methodImpl.value.visitLineNumber(this, super.mv, line, start)
            }
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