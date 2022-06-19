package com.tory.library.utils.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class DuByteReader extends InputStreamReader {

    private byte[] bytes;
    private Charset charset;

    public DuByteReader(byte[] bytes, Charset cs) {
        super(new ByteArrayInputStream(bytes), cs);
        this.bytes = bytes;
        this.charset = cs;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContent() {
        return new String(bytes, charset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.bytes = null;
    }
}