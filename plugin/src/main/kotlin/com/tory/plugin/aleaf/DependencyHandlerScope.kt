package com.tory.plugin.aleaf

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * - Author: tory
 * - Date: 2022/8/11
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
open class DependencyHandlerScope {

    operator fun String.invoke(dependencyNotation: Any) {
        println("ALeafPlugin: $this:$dependencyNotation")
    }

    inline operator fun invoke(configuration: DependencyHandlerScope.() -> Unit) =
        this.configuration()
}