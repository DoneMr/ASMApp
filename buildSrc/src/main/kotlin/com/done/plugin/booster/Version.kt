package com.done.plugin.booster

import com.android.builder.model.Version
import com.android.repository.Revision

/**
 * File:com.done.plugin.booster.Version
 * Description:xxx
 *
 * @author maruilong
 * @date 2020/10/12
 */
internal val ANDROID_GRADLE_PLUGIN_VERSION = Revision.parseRevision(Version.ANDROID_GRADLE_PLUGIN_VERSION)

val GTE_V3_X = ANDROID_GRADLE_PLUGIN_VERSION.major >= 3
val GTE_V3_6 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 6
val GTE_V3_5 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 5
val GTE_V3_4 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 4
val GTE_V3_3 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 3
val GTE_V3_2 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 2
val GTE_V3_1 = GTE_V3_X && ANDROID_GRADLE_PLUGIN_VERSION.minor >= 1