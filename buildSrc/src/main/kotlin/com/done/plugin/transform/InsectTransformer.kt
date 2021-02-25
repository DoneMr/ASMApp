package com.done.plugin.transform

import com.android.build.api.transform.TransformInvocation
import com.done.plugin.constant.InsectPluginConstants
import com.done.plugin.extension.BlockExtension
import com.done.plugin.extension.MethodCostExtension
import com.done.plugin.extension.PExtensionWrapper
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
        initExtension()
        val insectExtension = mPEx?.methodCostEx
        if (insectExtension?.isDebug == true || mPEx?.blockEx?.isDebug == true) {
            mHelper = InsectTransformHelper(mPEx!!)
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

    private fun initExtension() {
        mPEx = mProject.extensions.findByType(PExtensionWrapper::class.java)
        mPEx?.blockEx?.let { blockConfig ->
            var sb = StringBuilder(" pkg whitelist:")
            if (!blockConfig.packageWhitelist.isNullOrEmpty()) {
                blockConfig.packageWhitelist!!.forEach { pkg ->
                    sb.append("'$pkg' ")
                }
            }
            sb.append("\n class whitelist:")
            if (!blockConfig.classWhitelist.isNullOrEmpty()) {
                blockConfig.classWhitelist!!.forEach { pkg ->
                    sb.append("'$pkg' ")
                }
            }
            sb.append("\n method whitelist:")
            if (!blockConfig.methodWhitelist.isNullOrEmpty()) {
                blockConfig.methodWhitelist!!.forEach { pkg ->
                    sb.append("'$pkg' ")
                }
            }
            PLogger.log("过滤白名单：\n$sb")
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