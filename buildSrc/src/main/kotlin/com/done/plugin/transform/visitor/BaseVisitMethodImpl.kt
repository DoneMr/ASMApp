package com.done.plugin.transform.visitor

import com.done.plugin.extension.PExtensionWrapper
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * File:com.done.plugin.transform.visitor.BaseVisitMethodImpl
 * Description:横向方法插桩基类
 *
 * @author maruilong
 * @date 2020/11/12
 */
abstract class BaseVisitMethodImpl(api: Int, methodVisitor: MethodVisitor, access: Int, name: String, descriptor: String,
                                   ownerClassName: String, insectExtension: PExtensionWrapper) : IVisitMethod {

    protected open val api = api
    protected open val methodVisitor = methodVisitor
    protected open val access = access
    protected open val name = name
    protected open val descriptor = descriptor
    protected open val ownerClassName = ownerClassName
    protected open val mEx = insectExtension
    protected var mLineNumber = 0

    override fun visitLineNumber(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, line: Int, start: Label?) {
        mLineNumber = line
    }

    protected open fun isStatic(): Boolean = (access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC)

    protected open fun isAbstract(): Boolean = (access and Opcodes.ACC_ABSTRACT == Opcodes.ACC_ABSTRACT)

    protected open fun isInterface(): Boolean = (access and Opcodes.ACC_INTERFACE == Opcodes.ACC_INTERFACE)

    protected open fun isABS(): Boolean = isAbstract() || isInterface()
}