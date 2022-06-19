package com.tory.library.utils.json;

import androidx.annotation.NonNull;

import com.google.gson.stream.JsonWriter;

import java.io.BufferedWriter;
import java.io.Writer;

/**
 * - Author: xutao
 * - Date: 2021/9/9
 * - Description:
 */
public class DuJsonWriter extends JsonWriter {

    private boolean isSafeWrite = false;

    /**
     * Creates a new instance that writes a JSON-encoded stream to {@code out}.
     * For best performance, ensure {@link Writer} is buffered; wrapping in
     * {@link BufferedWriter BufferedWriter} if necessary.
     *
     * @param out
     */
    public DuJsonWriter(Writer out, boolean safeWrite) {
        super(out);
        isSafeWrite = safeWrite;
    }

    public static boolean isSafeWriter(@NonNull JsonWriter writer) {
        return writer instanceof DuJsonWriter && ((DuJsonWriter)writer).isSafeWrite;
    }
}