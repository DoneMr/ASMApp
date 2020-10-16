package com.done.plugin.transform

import com.android.build.api.transform.*
import com.done.plugin.InsectExtension
import com.done.plugin.util.PLogger
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * File:com.done.plugin.transform.InsectTransformHelper
 * Description:核心transform实现
 *
 * @author maruilong
 * @date 2020/10/13
 */
class InsectTransformHelper(val insectExtension: InsectExtension) : ITransform {

    override fun doTransform(transformInvocation: TransformInvocation, input: QualifiedContent) {
        val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
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