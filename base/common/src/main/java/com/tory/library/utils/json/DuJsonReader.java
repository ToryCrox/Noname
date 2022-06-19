package com.tory.library.utils.json;


import androidx.annotation.NonNull;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.Annotation;

/**
 * Author: tory
 * Date: 2020/10/21
 * Description:
 */
public class DuJsonReader extends JsonReader {

    private byte[]  bytes;
    private Annotation[] netAnnotations;

    /**
     * Creates a new instance that reads a JSON-encoded stream from {@code in}.
     *
     * @param in
     */
    public DuJsonReader(@NonNull Reader in, byte[]  bytes, Annotation[] netAnnotations) {
        super(in);
        this.bytes = bytes;
        this.netAnnotations = netAnnotations;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Annotation[] getNetAnnotations() {
        return netAnnotations;
    }

    @Override
    public void close() throws IOException {
        super.close();
        bytes = null;
    }
}