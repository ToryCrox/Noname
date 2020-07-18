package com.tory.demo.diskcache;

import androidx.annotation.NonNull;

import com.tory.demo.diskcache.lrucache.Util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

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
public class IoFileOperator implements IFileOperator {


    @Override
    public void write(@NonNull File file, String value) throws IOException {
        Writer writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new OutputStreamWriter(os, CacheConstants.CHARSET);
            writer.write(value);
        } finally {
            CacheUtil.closeQuietly(writer);
        }
    }

    @Override
    public String read(@NonNull File file) throws IOException {
        InputStream in = new FileInputStream(file);
        Reader reader = null;
        try {
            reader = new InputStreamReader(in, CacheConstants.CHARSET);
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            CacheUtil.closeQuietly(reader);
        }

    }

}
