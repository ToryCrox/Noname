package com.tory.dmzj.dbase.tools

import com.tencent.mmkv.MMKV
import com.tory.library.utils.AppUtils

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
object SpHelper {
    private val mmapId = "dmzj"

    private val mmkv: MMKV by lazy {
        MMKV.initialize(AppUtils.getContext())
        MMKV.mmkvWithID(mmapId)
    }


}
