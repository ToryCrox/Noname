package com.tory.dmzj.comic.api

import retrofit2.http.Field
import retrofit2.http.POST

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
interface UserService {

    @POST("/loginV2/m_confirm")
    suspend fun login(@Field("nickname") nickname: String,
        @Field("passwd") pwd: String)
}
