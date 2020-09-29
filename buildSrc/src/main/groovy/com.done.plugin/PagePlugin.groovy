package com.done.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class PagePlugin implements Plugin<Project> {

    private Project mProject

    @Override
    void apply(Project project) {
        mProject = project
        project.getExtensions().add(InsectExtension.CONFIG_NAME, InsectExtension)
        def android = project.extensions.findByType(BaseExtension)
        android.registerTransform(new CostTransform(project))
        LogUtilsGv.log("register PagePlugin")
    }

}