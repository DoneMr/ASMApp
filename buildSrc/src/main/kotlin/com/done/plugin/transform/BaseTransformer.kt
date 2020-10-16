package com.done.plugin.transform

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformManager
import com.done.plugin.booster.SCOPE_FULL_WITH_FEATURES
import com.done.plugin.booster.SCOPE_PROJECT
import com.done.plugin.util.PLogger
import org.gradle.api.Project

/**
 * File:com.done.plugin.transform.BaseTransformer
 * Description:transformer基类，为您完成注册相关的通用代码
 * 默认不支持增量编译，会影响编译速度，后期可以考虑支持下增量编译
 *
 * @author maruilong
 * @date 2020/10/12
 */
abstract class BaseTransformer(project: Project) : Transform() {

    protected var mProject: Project = project

    override fun getName(): String = this::class.java.name

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental(): Boolean = false

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        return when {
            mProject.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
            mProject.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
            mProject.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
            else -> return super.getReferencedScopes()
        }
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = when {
        mProject.plugins.hasPlugin("com.android.library") -> SCOPE_PROJECT
        mProject.plugins.hasPlugin("com.android.application") -> TransformManager.SCOPE_FULL_PROJECT
        mProject.plugins.hasPlugin("com.android.dynamic-feature") -> SCOPE_FULL_WITH_FEATURES
        else -> {
            PLogger.log("it is not a android project")
            mutableSetOf()
        }
    }
}

