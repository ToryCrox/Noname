package com.aleaf.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project



public class ALeafPlugin implements  Plugin<Project> {




    @Override
    void apply(Project project) {

        project.extensions.create("ALeaf", ALeafExtension)


        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                def variantName = variant.name.capitalize()
                //createTask(project, variantName)
                println("ALeafPlugin variantName="+variantName)
            }

            android.buildTypes.all { buildType ->
                def buildTypeName = buildType.name.capitalize()
                //createTask(project, buildTypeName)
                println("ALeafPlugin buildTypeName="+buildTypeName)
            }

            android.productFlavors.all { flavor ->
                def flavorName = flavor.name.capitalize()
                //createTask(project, flavorName)
                println("ALeafPlugin flavorName="+flavorName)
            }
        }
    }
}