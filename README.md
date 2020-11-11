# ASM自定义函数耗时


[TOC]

[ASM自定义函数耗时插件（一）](https://www.jianshu.com/p/1a7ea8cddec5)
[ASM自定义函数耗时插件（二）](https://www.jianshu.com/p/c6d8aa2671ff)

## 简介

[本插件源码地址~](https://github.com/DoneMr/ASMApp)

使用ASM技术，在android transform过程中完成对Java或者kotlin方法的函数耗时代码插桩，用于解决性能问题做函数耗时计算查看代码优化成果的辅助工具

之所以写这篇二是在上一篇基础上用kotlin完善和修复了一些bug，实现的更加优雅，欢迎大家下载下来体验

## 技术前置了解能力

惯例需要了解这些前提知识，一定要仔细研读，不然就会出现老板上次一期的问题（手动笑哭）~

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

首先就是按照规范实现android的Plugin并注册

```kotlin
class InsectPlugin : Plugin<Project> {

    private lateinit var mProject: Project

    override fun apply(project: Project) {
        configProj(project)
        when {
            //在app中依赖
            project.plugins.hasPlugin("com.android.application") || project.plugins.hasPlugin("com.android.dynamic-feature") -> {
                project.getAndroid<AppExtension>().let { androidExt ->
                    PLogger.log("application register plugin")
                    androidExt.registerTransform(InsectTransformer(project))
                }
            }
            //在lib中依赖
            project.plugins.hasPlugin("com.android.library") -> {
                project.getAndroid<LibraryExtension>().let { libExt ->
                    PLogger.log("lib register plugin")
                    libExt.registerTransform(InsectTransformer(project))
                }
            }
        }
    }

    private fun configProj(project: Project) {
        mProject = project
                .also {
                    it.extensions.add(InsectPluginConstants.INSECT_CONFIG, InsectExtension())
                }
    }
}
```

然后注册android的transformer

```kotlin
class InsectTransformer(project: Project) : BaseTransformer(project) {

    private lateinit var mHelper: InsectTransformHelper

    override fun transform(transformInvocation: TransformInvocation?) {
        val insectExtension = mProject.extensions.findByName(InsectPluginConstants.INSECT_CONFIG) as InsectExtension
        if (insectExtension.isDebug) {
            mHelper = InsectTransformHelper(insectExtension)
            //TODO 以下增加了增量编译后再进行编写
//            if (isIncremental) {
//                PLogger.log("增量编译，走父类的transform流程")
//                super.transform(transformInvocation)
//            } else {
//                if (transformInvocation != null) {
//                    transformInvocation.outputProvider?.deleteAll()
//                    PLogger.log("命中transform， 走transform逻辑")
//                    doFullTransform(transformInvocation)
//                } else {
//                    PLogger.log("transformInvocation为空，走父类的transform流程")
//                    super.transform(transformInvocation)
//                }
//            }
            if (transformInvocation != null) {
                transformInvocation.outputProvider?.deleteAll()
                doFullTransform(transformInvocation)
            } else {
                PLogger.e("transformInvocation为空，走父类的transform流程")
                super.transform(transformInvocation)
            }
        } else {
            PLogger.e("release下编译，不进行字节码插桩，走父类的transform")
            super.transform(transformInvocation)
        }
    }

    private fun doFullTransform(transformInvocation: TransformInvocation) {
        transformInvocation.inputs?.let { it ->
            it.map {
                it.jarInputs + it.directoryInputs
            }.flatten().forEach { input ->
                mHelper.doTransform(transformInvocation, input)
            }
        }
        super.transform(transformInvocation)
    }
}
```

最核心的实现其实是咱们的transformerHelper这个类，主要实现以下的逻辑完成插桩

* 遍历所有input输出，判断是文件还是目录
* 如果是目录，则继续遍历到文件为止
* 判断文件是jar还是class文件，然后分别处理jar和class
* 如果是jar则复制文件即可
* 如果是class，则进行插桩

接下来我们看一下对文件判断处理的类InsectTransformHelper

```kotlin
class InsectTransformHelper(val insectExtension: InsectExtension) : ITransform {

    override fun doTransform(transformInvocation: TransformInvocation, input: QualifiedContent) {
        val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
        when {
            format == Format.DIRECTORY && input is DirectoryInput -> transformDirectory(transformInvocation, input)
            format == Format.JAR && input is JarInput -> transformJar(transformInvocation, input)
        }
    }

    private fun transformJar(transformInvocation: TransformInvocation, input: JarInput) {
        transformInvocation.outputProvider?.let { provider ->
            PLogger.log("transform jar ${input.file.absolutePath}")
            val contentLocation = provider.getContentLocation(input.file.absolutePath, input.contentTypes, input.scopes, Format.JAR)
            _transformJar(input.file, contentLocation)
        }
    }

    private fun transformDirectory(transformInvocation: TransformInvocation, input: DirectoryInput) {
        transformInvocation.outputProvider.let { provider ->
            provider.getContentLocation(input.file.absolutePath, input.contentTypes, input.scopes, Format.DIRECTORY)?.let { dest ->
                _transformDirectory(input.file, dest)
            }
        }
    }

    private fun _transformDirectory(src: File, dest: File) {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        src.listFiles()?.forEach {
            val destPath = it.absolutePath.replace(src.absolutePath, dest.absolutePath)
            val destFile = File(destPath)
            when {
                it.isDirectory -> _transformDirectory(it, destFile)
                it.isFile -> when (it.extension) {
                    "class" -> _transformClass(it, destFile)
                    "jar" -> _transformJar(it, destFile)
                    else -> PLogger.log("啥也不是，啥也不干 src:$it, dest:$destFile")
                }
            }
        }

    }

    private fun _transformJar(src: File?, dest: File?) {
        if (src != null && dest != null) {
            FileUtils.copyFile(src, dest)
        }
    }

    private fun _transformClass(src: File, dest: File) {
        FileUtils.touch(dest)
        var fos = FileOutputStream(dest)
        var fis = FileInputStream(src)
        try {
            fos.also { os ->
                ClassReader(fis).also { reader ->
                    val classNode = InsectClassVisitor(insectExtension)
                    reader.accept(classNode, ClassReader.EXPAND_FRAMES)
                    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                    classNode.accept(classWriter)
                    os.write(classWriter.toByteArray())
                    os.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(fos)
            IOUtils.closeQuietly(fis)
        }
    }

}
```

然后是具体插桩代码的逻辑：

* 判断是否有标记过的注解

```kotlin
override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
  var hasMethod = !insectExtension.annotationNames.isNullOrEmpty()
  if (hasMethod) {
    insectExtension.annotationNames?.forEach { annotation ->
                                              val relpaceAnno = "L${annotation.replace(".", "/")};"
                                              mDoCost = mDoCost || relpaceAnno == descriptor ?: "nonono"
                                             }
  }
  if (mDoCost) {
    PLogger.i("${this@InsectClassVisitor.name}.${this.mMethodName} 可以开始进行插桩")
  }
  return super.visitAnnotation(descriptor, visible)
}
```

* 分别在方法开始和结束的地方插入对应的代码

```kotlin
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
    val name = "${this@InsectClassVisitor.name}.${this.mMethodName} => cost ".replace("/", ".")
    super.mv.visitLdcInsn(name)
    super.mv.visitVarInsn(LLOAD, mStartVar!!)
    super.mv.visitMethodInsn(INVOKESTATIC, insectExtension.methodOwner.replace(".", "/"),
                             insectExtension.methodName, "(Ljava/lang/String;J)V", false)
  }
}
```

本来还有一种极其简单的方式，就是通过classNode的methods.find函数来进行插桩，结果在选择存储临时变量即方法开始的时间戳的时候，进行LLOAD操作，变量的index因为不熟悉的原因调试了很久都没成功，才改用现在的手写MethodVisitor来实现，希望有知道这里知识的大佬能指导下。。。关于ASM的知识网上大多都很零散，特别的ASM不同版本的方法都有一定的差异。。。这里用的是gradle4自带的ASM7版本

---
2020年11月12日01:29:31 更新日历
* 增加判断抽象方法的耗时不予插桩
* 对象成员方法的类传递使用object.getClass.getName方式获取类名，原有的实现方式可能会传递基类的类名，不是很友好
* 进一步封装methodVisitor插桩，支持横向扩展插桩能力

新增一个支持横向扩展能力的methodVisitor接口，抽取抽来通用的方法，在使用时候一定要注意，adviceAdpater提供给实现避免开发者自行维护索引的情况
```kotlin
interface IVisitMethod {

    fun onVisitAnnotation(annotationVisitor: AnnotationVisitor?, descriptor: String?, visible: Boolean)

    fun onMethodEnter(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?)

    fun onMethodExit(adviceAdapter: AdviceAdapter?, methodVisitor: MethodVisitor?, opcode: Int)

    fun getMaxStack(): Int

    fun getMaxLocals(): Int
}
```
而原有的内部类改为静态内部类实现，仅仅是个"广义责任链"的分发者，利用小控制反转的方式方便开发者专注于插桩的实现
```kotlin
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
```