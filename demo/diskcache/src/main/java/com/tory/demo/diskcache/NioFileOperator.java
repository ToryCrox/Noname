package com.tory.demo.diskcache;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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
class NioFileOperator implements IFileOperator {

    @Override
    public void write(@NonNull File file, String value) throws IOException {
        FileChannel channel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            byte[] bytes = value.getBytes(CacheConstants.CHARSET);
            long len = bytes.length;
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, len);
            byteBuffer.put(bytes);
            byteBuffer.force();
            channel.truncate(len);
        } finally {
            channel.close();
        }
    }

    @Override
    public String read(@NonNull File file) throws IOException {
        FileInputStream inputStream = null;
        FileChannel channel = null;
        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();

            long len = channel.size();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, len);
            byte[] bytes = new byte[(int) len];
            byteBuffer.get(bytes);
            return new String(bytes, CacheConstants.CHARSET);
//            ByteBuffer byteBuffer = ByteBuffer.allocate((int) len);
//            channel.read(byteBuffer);
//            return new String(byteBuffer.array());
        } finally {
            channel.close();
        }
    }
}
