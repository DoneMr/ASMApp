package com.done.plugin.transform

import com.android.build.api.transform.*
import com.done.plugin.extension.PExtensionWrapper
import com.done.plugin.util.PLogger
import com.done.plugin.util.ThreadPoolMngr
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable

/**
 * File:com.done.plugin.transform.InsectTransformHelper
 * Description:核心transform实现
 *
 * @author maruilong
 * @date 2020/10/13
 */
class InsectTransformHelper(val insectExtension: PExtensionWrapper) : ITransform {

    override fun doTransform(transformInvocation: TransformInvocation, input: QualifiedContent) {
        val format = when (input) {
            is DirectoryInput -> Format.DIRECTORY
            is JarInput -> Format.JAR
            else -> null
        }
        when {
            format == Format.DIRECTORY && input is DirectoryInput -> transformDirectory(transformInvocation, input as DirectoryInput)
            format == Format.JAR && input is JarInput -> transformJar(transformInvocation, input as JarInput)
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
                if (transformInvocation.isIncremental) {
                    //增量编译
                    val changedFiles = input.changedFiles
                    val src = input.file
                    for (changedFile in changedFiles) {
                        val status = changedFile.value ?: return
                        //兼容java异常
                        val file = changedFile.key
                        when (status) {
                            Status.NOTCHANGED -> {
                            }
                            Status.REMOVED -> {
                                FileUtils.forceDelete(file)
                                FileUtils.forceDelete(dest)
                            }
                            Status.CHANGED, Status.ADDED -> {
                                val destPath = src.absolutePath.replace(src.absolutePath, dest.absolutePath)
                                val destFile = File(destPath)
                                _transformClass(src, destFile)
                            }
                        }
                    }
                } else {
                    _transformDirectory(input.file, dest)
                }
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
        var submit = ThreadPoolMngr.submit(TransformTask(object : TransformTask.OnExecuteListener<Boolean> {
            override fun onExecute(): Boolean {
                var ret = false
                try {
                    if (src != null && dest != null) {
                        FileUtils.copyFile(src, dest)
                    }
                    ret = true
                } catch (e: Exception) {
                }
                return ret
            }
        }))
        submit.invoke()
    }

    private fun _transformClass(src: File, dest: File) {
        val submit = ThreadPoolMngr.submit(TransformTask(object : TransformTask.OnExecuteListener<Boolean> {
            override fun onExecute(): Boolean {
                FileUtils.touch(dest)
                var fos = FileOutputStream(dest)
                var fis = FileInputStream(src)
                var result = false
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
                    result = true
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    IOUtils.closeQuietly(fos)
                    IOUtils.closeQuietly(fis)
                }
                return result
            }
        }))
        submit.invoke()
    }

}