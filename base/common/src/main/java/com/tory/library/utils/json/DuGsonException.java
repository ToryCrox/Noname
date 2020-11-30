package com.tory.library.utils.json;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/10/10
 * Description: 解析报错Exception
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/10/10 xutao 1.0
 * Why & What is modified:
 */
class DuGsonException extends RuntimeException {

    public DuGsonException(String message, Throwable cause) {
        super(GsonHelper.TAG + " " + message, cause);
    }
}