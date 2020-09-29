package com.done.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class CostTransform extends Transform {

    private Project mProject

    private InsectExtension mInsectExtension

    private PROJECT_TYPE mType = PROJECT_TYPE.NONE

    private enum PROJECT_TYPE {
        PROJECT,
        LIB,
        NONE
    }

    CostTransform(Project project) {
        this.mProject = project
        mProject.plugins.withId("com.android.application", new Action<Plugin>() {
            @Override
            void execute(Plugin plugin) {
                mType = PROJECT_TYPE.PROJECT
                LogUtilsGv.log("在app中使用插件:$plugin")
            }
        })
        mProject.plugins.withId("com.android.library", new Action<Plugin>() {
            @Override
            void execute(Plugin plugin) {
                mType = PROJECT_TYPE.LIB
                LogUtilsGv.log("在lib中使用插件:$plugin")
            }
        })
    }

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

    private static void handleDirectoryFile(File input, File dest, InsectExtension insectExtension) {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        String srcDirPath = input.getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        for (File file : input.listFiles()) {
            String destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            if (file.isDirectory()) {
                handleDirectoryFile(file, destFile, insectExtension)
            } else if (file.isFile()) {
                if (file.getName().endsWith(".class")) {
                    LogUtilsGv.log("deal file: ${file.getName()}")
                    FileUtils.touch(destFile)
                    transformSingleFile(file, destFile, insectExtension)
                }
            }
        }
    }

    private static void transformSingleFile(File input, File dest, InsectExtension insectExtension) {
        weave(input.getAbsolutePath(), dest.getAbsolutePath(), insectExtension)
    }

    private static void weave(String inputPath, String outputPath, InsectExtension insectExtension) {
        try {
            FileInputStream is = new FileInputStream(inputPath)
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
            LifecycleMethodClassAdapter adapter = new LifecycleMethodClassAdapter(Opcodes.ASM7, cw, insectExtension)
            cr.accept(adapter, ClassReader.EXPAND_FRAMES)
            FileOutputStream fos = new FileOutputStream(outputPath)
            fos.write(cw.toByteArray())
            fos.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }

    private static void handleJarFile(File input, File dest) {
        FileUtils.copyFile(input, dest)
    }
}