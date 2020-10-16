package com.done.plugin.transform

import com.android.build.api.transform.TransformInvocation
import com.done.plugin.InsectExtension
import com.done.plugin.constant.InsectPluginConstants
import com.done.plugin.util.PLogger
import org.gradle.api.Project

/**
 * File:com.done.plugin.transform.InsectTransformer
 * Description:transformer
 *
 * @author maruilong
 * @date 2020/10/12
 */
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