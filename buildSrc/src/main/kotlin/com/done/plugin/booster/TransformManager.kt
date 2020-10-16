package com.done.plugin.booster

import com.android.build.api.transform.QualifiedContent
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet

/**
 * File:com.done.plugin.booster.TransformManager
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/12
 */
val SCOPE_PROJECT: MutableSet<in QualifiedContent.Scope> = TransformManager.PROJECT_ONLY

val SCOPE_FULL_WITH_FEATURES: MutableSet<in QualifiedContent.Scope> = when {
    GTE_V3_2 -> TransformManager.SCOPE_FULL_WITH_FEATURES
    else -> TransformManager.SCOPE_FULL_PROJECT
}

val SCOPE_FULL_LIBRARY_WITH_FEATURES: MutableSet<in QualifiedContent.Scope> = when {
    GTE_V3_2 -> ImmutableSet.Builder<QualifiedContent.ScopeType?>()
            .addAll(TransformManager.SCOPE_FEATURES)
            .add(QualifiedContent.Scope.PROJECT)
            .build()
    else -> TransformManager.PROJECT_ONLY
}
