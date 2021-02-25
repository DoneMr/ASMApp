package com.done.plugin.transform.visitor

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

/**
 * File:com.done.plugin.transform.visitor.IVisitMethod
 * Description:把方法插入的能力进行分发
 *
 * @author maruilong
 * @date 2020/11/12
 */
interface IVisitMethod {

    fun visitLineNumber(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, line: Int, start: Label?)

    fun onVisitAnnotation(annotationVisitor: AnnotationVisitor?, descriptor: String?, visible: Boolean)

    fun onMethodEnter(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?)

    fun onMethodExit(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, opcode: Int)

    fun getMaxStack(): Int

    fun getMaxLocals(): Int
}