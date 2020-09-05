package com.tory.library.ui.pics

import android.content.Context
import android.content.Intent
import com.tory.library.model.PicItemModel
import com.tory.library.model.PicsModel

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/5
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/5 xutao 1.0
 * Why & What is modified:
 */
object PicsHelper {

    const val KEY_ARGS_PICS = "KEY_ARGS_PICS"


    fun launch(context: Context, picsModel: PicsModel) {
        val intent = Intent(context, PicsActivity::class.java)
        intent.putExtra(KEY_ARGS_PICS, picsModel)
        context.startActivity(intent)
    }

    fun launch(context: Context, picItems: List<PicItemModel>, index: Int = 0) {
        launch(context, PicsModel(picItems, index))
    }
}
