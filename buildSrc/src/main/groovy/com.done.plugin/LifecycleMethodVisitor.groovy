package com.done.plugin


import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

public class LifecycleMethodVisitor extends AdviceAdapter {

    private String mMethodName

    private int startTime

    private String mClassName

    private boolean canPrint = false

    private InsectExtension mInsectExtension

    private String mCostMethod, mCostMethodClass

    LifecycleMethodVisitor(int api, MethodVisitor methodVisitor, int access,
                           String name, String descriptor, String className,
                           InsectExtension insectExtension) {
        super(api, methodVisitor, access, name, descriptor)
        mMethodName = name
        mClassName = className
        mInsectExtension = insectExtension
    }

    @Override
    void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    @Override
    public void visitCode() {
        super.visitCode();

    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        boolean hasMethod = true
        if (Utils.isEmptyString(mInsectExtension.methodOwner) || Utils.isEmptyString(mInsectExtension.methodName)) {
            hasMethod = false
        } else {
            mCostMethod = mInsectExtension.methodName
            mCostMethodClass = mInsectExtension.methodOwner.replaceAll("(\\.)", "/")
        }
        if (hasMethod) {
            for (String annotation : mInsectExtension.annotationNames) {
                String replaceAnno = "L" + annotation.replaceAll("(\\.)", "/") + ";"
                canPrint = canPrint || replaceAnno == desc
                LogUtilsGv.log("外部配置的注解：$replaceAnno, $mClassName.$mMethodName 插桩:$canPrint, 插入$mCostMethodClass#$mCostMethod")
            }
        } else {
            LogUtilsGv.log("does not have cost method, do not insert cost code")
        }
        return super.visitAnnotation(desc, visible)
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (canPrint) {
            String newName = (mClassName + "#" + mMethodName).replaceAll("/", ".")
            mv.visitLdcInsn(newName)
            mv.visitVarInsn(LLOAD, startTime)
            mv.visitMethodInsn(INVOKESTATIC, mCostMethodClass, mCostMethod, "(Ljava/lang/String;J)V", false)
        }
    }

    @Override
    protected void onMethodEnter() {
        if (canPrint) {
            invokeStatic(Type.getType("Landroid/os/SystemClock;"), new Method("uptimeMillis", "()J"))
            startTime = newLocal(Type.LONG_TYPE)
            storeLocal(startTime)
        }
    }
}