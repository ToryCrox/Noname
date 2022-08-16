package com.tory.plugin.aleaf

import org.gradle.api.Action
import org.gradle.api.Project

/**
 * - Author: tory
 * - Date: 2022/8/11
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
open class ALeafExtension {

    var versionCode: Int = 1

    private val dependencyExtension = DependencyExtension()


//    fun dependencies(action: Action<DependencyExtension>) {
//        action.execute(dependencyExtension)
//    }

    fun dependencies(configuration: DependencyHandlerScope.() -> Unit) {
        println("ALeafPlugin: dependencies....")
        DependencyHandlerScope().configuration()
    }


    override fun toString(): String {
        return "ALeafExtension(versionCode=$versionCode)"
    }

}