# ASM自定义函数耗时

[TOC]

## 简介

[本插件源码地址~](https://github.com/DoneMr/ASMApp)

使用ASM技术，在android transform过程中完成对Java或者kotlin方法的函数耗时代码插桩，用于解决性能问题做函数耗时计算查看代码优化成果的辅助工具

仅需要关注buildSrc即可

## 技术前置了解能力

写这个小插件主要需要了解以下几个技术点：

* Java字节码

1. [轻松看懂Java字节码](https://juejin.im/post/6844903588716609543)
2. [[字节码增强技术探索](https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html)](https://tech.meituan.com/2019/09/05/java-bytecode-enhancement.html)

* Android打包流程(这里主要知道Android的transform调用时机以及部分源码即可)

1. [Android APK文件结构 完整打包编译的流程](https://blog.csdn.net/aha_jasper/article/details/104944929)
2. [Android Gradle Transform 详解](https://www.jianshu.com/p/cf90c557b866)
3. [Gradle 学习之 Android 插件的 Transform API](https://juejin.im/post/6844903891138674696)
* ASM使用

1. [ASM 系列详细教程](https://blog.csdn.net/ryo1060732496/article/details/103655505)
2. [深入理解Transform](https://juejin.im/post/6844903829671002126)
以上需要是在写之前需要了解的知识点，不用太纠结细节，了解清楚每个流程即可，下面直接上写法

## Coding

* 创建一个JavaLib，一定要命名为buildSrc
![buildSrc目录.png](https://upload-images.jianshu.io/upload_images/2822814-fffa10cd708c3922.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)

* 删除所有文件，保留一个build.gradle，完成相关配置
``` gradle
apply plugin: 'groovy'
apply plugin: 'java'
apply plugin: 'kotlin'
apply plugin: 'kotlin-android-extensions'

ext {
    kt_v = "1.3.50"
}

buildscript {
    ext.kt_v = "1.3.50"
    repositories {
        maven {
            url 'https://maven.aliyun.com/repository/jcenter'
        }
        maven {
            url 'https://maven.aliyun.com/nexus/content/repositories/google'
        }
        google()
        jcenter()
        mavenCentral() //必须
    }

    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${this.ext.kt_v}"
    }
}

sourceSets {
    main {
        groovy {
            srcDir '../buildSrc/src/main/groovy'
        }
        java {
            srcDir '../buildSrc/src/main/java'
        }
        kotlin {
            srcDir '../buildSrc/src/main/kotlin'
        }
        resources {
            srcDir '../buildSrc/src/main/resources'
        }
    }
}

repositories {
    maven {
        url 'https://maven.aliyun.com/repository/jcenter'
    }
    maven {
        url 'https://maven.aliyun.com/nexus/content/repositories/google'
    }
    google()
    jcenter()
    mavenCentral() //必须
}

dependencies {
    compile gradleApi() //必须
    compile localGroovy() //必须
    implementation 'com.android.tools.build:gradle:4.0.0'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kt_v"

    // ASM 相关
    implementation 'org.ow2.asm:asm:7.2'
    implementation 'org.ow2.asm:asm-util:7.2'
    implementation 'org.ow2.asm:asm-commons:7.2'
}

sourceCompatibility = "8"
targetCompatibility = "8"
```
* 创建插件资源目录，用于标识插件使用
![resource目录.png](https://upload-images.jianshu.io/upload_images/2822814-6bd158ece853f1d5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)
implementation-class内容写您的插件名字即可
``` gradle
implementation-class=com.done.plugin.PagePlugin
```
对应插件类代码
```groovy
package com.done.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class PagePlugin implements Plugin<Project> {

    private Project mProject

    @Override
    void apply(Project project) {
        mProject = project
        project.getExtensions().add(InsectExtension.CONFIG_NAME, InsectExtension)
        def android = project.extensions.findByType(BaseExtension)
        android.registerTransform(new CostTransform(project))
        LogUtilsGv.log("register PagePlugin")
    }
}
```
在插件被调用的时候，gradle会调用apply方法，在这里注册插桩的transform-CostTransform，可以看下transform的配置
```groovy
    /**
     * 执行transform时候task的名字
     * @return
     */
    @Override
    String getName() {
        return this.class.getName()
    }

    /**
     * 只需要class文件输入
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 如果是app使用插件，则传递所有的class进来，如果是lib使用的话，仅传递对应lib project的class过来即可
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        if (mType == PROJECT_TYPE.LIB) {
            return TransformManager.PROJECT_ONLY
        } else {
            return TransformManager.SCOPE_FULL_PROJECT
        }
    }

    /**
     * TODO 暂时不支持增量编译
     * @return
     */
    @Override
    boolean isIncremental() {
        return false
    }
```
上面需要重写的方法主要是表明仅需要class的流输入进来，接下来就是核心的transform方法
```groovy
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        mInsectExtension = mProject.getExtensions().getByName(InsectExtension.CONFIG_NAME)
        if (mInsectExtension == null || mInsectExtension.annotationNames == null || mInsectExtension.annotationNames.length < 1 || !mInsectExtension.isDebug) {
            LogUtilsGv.log("do not execute insert byte code")
            return
        }
        LogUtilsGv.log("startTransform anno:${mInsectExtension.annotationNames}")
        long startTime = System.currentTimeMillis()
        Collection<TransformInput> inputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }
        //处理目录中的文件
        inputs.each {
            TransformInput input ->
                input.directoryInputs.each {
                    DirectoryInput directoryInput ->
                        File dest = outputProvider.getContentLocation(directoryInput.getFile().getAbsolutePath(),
                                directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY)
                        handleDirectoryFile(directoryInput.getFile(), dest, mInsectExtension)
                }
        }
        //处理Jar中的文件
        inputs.each {
            TransformInput input ->
                input.jarInputs.each {
                    JarInput jarInput ->
                        File dest = outputProvider.getContentLocation(jarInput.getFile().getAbsolutePath(),
                                jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR)
                        handleJarFile(jarInput.getFile(), dest)
                }
        }
        LogUtilsGv.log("endTransform cost:" + (System.currentTimeMillis() - startTime))
    }
```
为了方便外部使用，需要对自定义的插件增加一些配置，如标识的注解类和需要调用的方法，插件内部写死传递两个参数，分别是method名称和方法执行的起始时间
```groovy
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
```

* 在对应的Lib或者App下进行使用
```gradle
apply plugin: 'com.done.plugin'
insectConfig {
    annotationNames = ['com.done.asm.Cost',
                       'com.done.asm.KtCost']
    methodOwner = 'com.done.asm.Utils'
    methodName = 'printCost'
    isDebug = true
}
```
集成插件后的能力，可以在修饰对应注解的地方插桩函数耗时的代码
annotationNames 注解类作为数组方法中
isDebug 负责是否进行插桩，用于远程打包的时候配置使用
methodOwner  是调用的静态方法类名
methodName 是调用的静态方法名称

代码量其实很少
![插件全量目录.png](https://upload-images.jianshu.io/upload_images/2822814-cebcdc01b4b64eef.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/320)


