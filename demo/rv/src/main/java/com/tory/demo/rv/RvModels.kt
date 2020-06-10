package com.tory.demo.rv

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/6/6
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/6/6 xutao 1.0
 * Why & What is modified:
 */

data class ListModel(
        val title: String,
        val list: List<TextModel>
)

data class TextModel(
        val index: Int,
        val text: String
)
