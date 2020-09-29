package com.done.plugin


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

public class LifecycleMethodClassAdapter extends ClassVisitor implements Opcodes {
    private String mClassName
    private String mSuperName

    private InsectExtension mInsectExtension

    LifecycleMethodClassAdapter(int api, ClassVisitor classVisitor, InsectExtension insectExtension) {
        super(api, classVisitor)
        mInsectExtension = insectExtension
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mClassName = name
        mSuperName = superName
        StringBuilder stringBuilder = new StringBuilder("访问到类 ")
        stringBuilder.append("version:" + version + ",")
                .append("access:" + access + ",")
                .append("name:" + name + ",")
                .append("signature:" + signature + ",")
                .append("superName:" + superName + ",")
        if (interfaces != null && interfaces.length > 0) {
            stringBuilder.append("interfaces: [")
            for (int i = 0; i < interfaces.length; i++) {
                stringBuilder.append(interfaces[i] + ",")
            }
            stringBuilder.append("]")
        }
        LogUtilsGv.log(stringBuilder.toString())
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        LogUtilsGv.log("访问方法 name:$name,descriptor:$descriptor,signature:$signature")
        return (mv == null) ? null : new LifecycleMethodVisitor(ASM7, mv, access, name, descriptor,
                mClassName, mInsectExtension)
//        return (mv == null) ? null : new LifecycleMethodVisitor(ASM7, mv, access, name, descriptor)
//        if (shouldAddMethod()) {
//            if (shouldOnCreate(name, descriptor)) {
//                LogUtilsGv.log("扫描到父类是AppCompatActivity, 增加耗时计算")
//                return (mv == null) ? null : new LifecycleMethodVisitor(ASM7, mv, access, name, descriptor)
//            } else if (shouldOnResume(name, descriptor)) {
//                return (mv == null) ? null : new LifecycleMethodVisitor(ASM7, mv, access, name, descriptor)
//            }
//        }
    }


    private boolean shouldAddMethod() {
        return "android/support/v7/app/AppCompatActivity".equals(mSuperName) || "androidx/appcompat/app/AppCompatActivity".equals(mSuperName) || "androidx/fragment/app/Fragment".equals(mSuperName)
    }

    private boolean shouldOnCreate(String methodName, String descriptor) {
        LogUtilsGv.log("methodName:" + methodName + ", descriptor:" + descriptor)
        return "onCreate".equals(methodName) && PagePluginConstants.CHECK_ON_CREATE
    }

    private boolean shouldOnResume(String methodName, String descriptor) {
        LogUtilsGv.log("methodName:" + methodName + ", descriptor:" + descriptor)
        return "onResume()V".equals(methodName + descriptor) && PagePluginConstants.CHECK_ON_RESUME
    }

    private boolean isActivity() {

    }

    private boolean isFragment() {

    }
}