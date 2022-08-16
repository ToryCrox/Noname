package com.tory.plugin.aleaf


import org.gradle.api.Plugin
import org.gradle.api.Project

class ALeafPlugin : Plugin<Project> {

    companion object {
        const val TAG = "ALeafPlugin"
    }


    override fun apply(project: Project) {

        val extension = project.extensions.create("aleaf", ALeafExtension::class.java)

        project.afterEvaluate {
            println("$TAG extension: $extension")
        }
    }

}