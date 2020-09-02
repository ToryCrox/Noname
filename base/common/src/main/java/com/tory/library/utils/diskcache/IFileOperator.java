package com.tory.library.utils.diskcache;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/10
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/10 xutao 1.0
 * Why & What is modified:
 */
public interface IFileOperator {
    void write(@NonNull File file, String str) throws IOException;
    String read(@NonNull File file) throws IOException;
}
