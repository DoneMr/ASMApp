package com.done.plugin.transform

import com.done.plugin.InsectExtension
import com.done.plugin.util.PLogger
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

/**
 * File:com.done.plugin.transform.InsectClassVisitor
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/16
 */
class InsectClassVisitor(private val insectExtension: InsectExtension) : ClassNode(Opcodes.ASM7) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor =
            InsectMethodVisitor(super.api, super.visitMethod(access, name, descriptor, signature, exceptions), access, name!!, descriptor!!, this@InsectClassVisitor.name, insectExtension)
//        super.methods.find {
//            val annotations = (if (it.visibleAnnotations == null) mutableListOf() else it.visibleAnnotations) +
//                    (if (it.invisibleAnnotations == null) mutableListOf() else it.invisibleAnnotations) +
//                    (if (it.visibleTypeAnnotations == null) mutableListOf() else it.visibleTypeAnnotations) +
//                    (if (it.invisibleTypeAnnotations == null) mutableListOf() else it.invisibleTypeAnnotations)
//            val judgeCostAnno = judgeCostAnno(annotations, insectExtension)
//            var log = ""
//            if (judgeCostAnno) {
//                annotations.forEach { annotationNode ->
//                    log += (",${annotationNode.desc}")
//                }
//                PLogger.log("${this@InsectClassVisitor.name}.$name@$log")
//            }
//            judgeCostAnno
//        }.let {
//            return if (it != null) {
//                PLogger.log("visitor method, 注入cost代码")
//                InsectMethodVisitor(super.api, visitMethod, access, name!!, descriptor!!, this@InsectClassVisitor.name, insectExtension)
//            } else {
//                visitMethod
//            }
//        }

    inner class InsectMethodVisitor(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                                    val ownerClassName: String = "class", val insectExtension: InsectExtension)
        : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        private var mStartVar: Int? = null

        private var mDoCost = false

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
            var hasMethod = !insectExtension.annotationNames.isNullOrEmpty()
            if (hasMethod) {
                insectExtension.annotationNames?.forEach { annotation ->
                    val relpaceAnno = "L${annotation.replace(".", "/")};"
                    mDoCost = mDoCost || relpaceAnno == descriptor ?: "nonono"
                }
            }
            if (mDoCost) {
                PLogger.i("${this@InsectClassVisitor.name}.${this.name} 可以开始进行插桩")
            }
            return super.visitAnnotation(descriptor, visible)
        }

        override fun onMethodEnter() {
            super.onMethodEnter()
            if (mDoCost) {
                invokeStatic(Type.getType("Landroid/os/SystemClock;"), Method("uptimeMillis", "()J"))
                mStartVar = newLocal(Type.LONG_TYPE)
                storeLocal(mStartVar!!)
            }
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            if (mDoCost) {
                val name = "$ownerClassName.$name => cost ".replace("/", ".")
                super.mv.visitLdcInsn(name)
                super.mv.visitVarInsn(LLOAD, mStartVar!!)
                super.mv.visitMethodInsn(INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
                        insectExtension.methodName, "(Ljava/lang/String;J)V", false)
            }
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 1, maxLocals + 2)
        }
    }
}