package com.tory.demo.diskcache;

import androidx.annotation.NonNull;

import java.io.File;
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
class ChannelFileOperator implements IFileOperator {

    @Override
    public void write(@NonNull File file, String value) throws IOException {
        FileChannel channel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            byte[] bytes =  value.getBytes(CacheConstants.CHARSET);
            channel.write(ByteBuffer.wrap(bytes));
            channel.truncate(bytes.length);
            channel.force(true);
        } finally {
            CacheUtil.closeQuietly(channel);
        }
    }

    @Override
    public String read(@NonNull File file) throws IOException {
        FileChannel channel = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();
            long len = channel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) len);
            channel.read(byteBuffer);
            return new String(byteBuffer.array(), CacheConstants.CHARSET);
        } finally {
            CacheUtil.closeQuietly(channel);
        }
    }
}
