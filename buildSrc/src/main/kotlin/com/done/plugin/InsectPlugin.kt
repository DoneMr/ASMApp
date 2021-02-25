package com.done.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.done.plugin.booster.getAndroid
import com.done.plugin.constant.InsectPluginConstants
import com.done.plugin.extension.PExtensionWrapper
import com.done.plugin.transform.InsectTransformer
import com.done.plugin.util.PLogger
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * File:com.done.plugin.InsectPlugin
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/12
 */
class InsectPlugin : Plugin<Project> {

    private lateinit var mProject: Project

    override fun apply(project: Project) {
        configProj(project)
        initTransformPlugin(project)
    }

    private fun configProj(project: Project) {
        mProject = project
                .also {
//                    it.extensions.add(InsectPluginConstants.INSECT_CONFIG, InsectExtension())
//                    it.extensions.add(InsectPluginConstants.BLOCK_CONFIG, BlockExtension())
                    it.extensions.add(InsectPluginConstants.INSECT_CONFIG, PExtensionWrapper::class.java)
                }
    }

    private fun initTransformPlugin(project: Project) {
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
}
