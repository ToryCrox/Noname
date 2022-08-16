package com.tory.plugin.aleaf

/**
 * - Author: tory
 * - Date: 2022/8/11
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class DependencyExtension {
    val models = mutableListOf<DependencyModel>()
    val files = mutableListOf<DependencyModel>()
    val projects = mutableListOf<DependencyModel>()


    override fun toString(): String {
        return "DependencyExtension(models=$models, files=$files, projects=$projects)"
    }

}

class DependencyModel