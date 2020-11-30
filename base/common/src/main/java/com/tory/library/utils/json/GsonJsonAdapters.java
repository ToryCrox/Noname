package com.tory.library.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/16
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/16 xutao 1.0
 * Why & What is modified:
 */
class GsonJsonAdapters {

    /**
     * Boolean的解析
     */
    public static final TypeAdapter<Boolean> JSON_BOOLEAN = new TypeAdapter<Boolean>() {
        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (in.peek() == JsonToken.STRING) {
                // support strings for compatibility with GSON 1.7
                return Boolean.parseBoolean(in.nextString());
            } else if (token == JsonToken.NUMBER) {
                try {
                    return in.nextDouble() != 0;
                } catch (Exception e) {
                    GsonHelper.bug(e, in, "GsonJsonAdapters can not cast to Boolean, type: " + token);
                    return false;
                }
            }
            return in.nextBoolean();
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Number> JSON_INTEGER = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (token == JsonToken.BOOLEAN) { //SafeJsonFactory对其它的已经做了处理了
                in.skipValue();
                return null;
            }
            try {
                return in.nextInt();
            } catch (NumberFormatException e) {
                //throw new JsonSyntaxException(e);
                in.skipValue();
                GsonHelper.bug(e, in, "warning !! can not to integer, skip it " + token);
                return null;
            }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Number> JSON_LONG = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else if (token == JsonToken.BOOLEAN) { //SafeJsonFactory对其它的已经做了处理了
                in.skipValue();
                return null;
            }
            try {
                return in.nextLong();
            } catch (NumberFormatException e) {
                //throw new JsonSyntaxException(e);
                in.skipValue();
                GsonHelper.bug(e, in, "warning !! can not parse to long, may double " + token);
                return null;
            }
        }

        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }
    };

    public static final TypeAdapter<Number> JSON_FLOAT = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return (float) in.nextDouble();
        }
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value != null && value.floatValue() == value.longValue()) {
                out.value(value.longValue());
            } else {
                out.value(value);
            }
        }
    };

    public static final TypeAdapter<Number> JSON_DOUBLE = new TypeAdapter<Number>() {
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return in.nextDouble();
        }
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value != null && value.doubleValue() == value.longValue()) {
                out.value(value.longValue());
            } else {
                out.value(value);
            }
        }
    };

    /**
     * string的解析器，兼容JSONObject和JSONArray
     */
    public static final TypeAdapter<String> JSON_STRING = new TypeAdapter<String>() {
        final TypeAdapter<String> defaultAdapter = TypeAdapters.STRING;
        final TypeAdapter<JsonElement> elementAdapter = TypeAdapters.JSON_ELEMENT;

        @Override
        public void write(JsonWriter out, String value) throws IOException {
            defaultAdapter.write(out, value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            JsonToken p = in.peek();
            if (p == JsonToken.BEGIN_OBJECT) {
                JsonElement element = elementAdapter.read(in);
                //JSONObject object = readObject(in);
                if (element != null) {
                    return element.toString();
                }
                return null;
            } else if (p == JsonToken.BEGIN_ARRAY) {
                //JSONArray array = readArray(in);
                JsonElement array = elementAdapter.read(in);
                if (array != null) {
                    return array.toString();
                }
                return null;
            }
            return defaultAdapter.read(in);
        }
    };

    public static final TypeAdapter<JSONObject> JSON_OBJECT = new TypeAdapter<JSONObject>() {

        @Override
        public void write(JsonWriter out, JSONObject value) throws IOException {
            if (value != null) {
                out.jsonValue(value.toString());
            } else {
                out.nullValue();
            }
        }

        @Override
        public JSONObject read(JsonReader in) throws IOException {
            JsonToken p = in.peek();
            if (p == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return readObject(in);
        }
    };

    public static final TypeAdapter<JSONArray> JSON_ARRAY = new TypeAdapter<JSONArray>() {

        @Override
        public void write(JsonWriter out, JSONArray value) throws IOException {
            if (value != null) {
                out.jsonValue(value.toString());
            } else {
                out.nullValue();
            }
        }

        @Override
        public JSONArray read(JsonReader in) throws IOException {
            JsonToken p = in.peek();
            if (p == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return readArray(in);
        }
    };

    /**
     * 读取所有类型
     *
     * @throws IOException
     */
    private static Object readAny(JsonReader in) throws IOException {
        switch (in.peek()) {
            case STRING:
            case NUMBER:
                return in.nextString();
            case BOOLEAN:
                return in.nextBoolean();
            case NULL:
                in.nextNull();
                return null;
            case BEGIN_ARRAY:
                return readArray(in);
            case BEGIN_OBJECT:
                return readObject(in);
            case END_DOCUMENT:
            case NAME:
            case END_OBJECT:
            case END_ARRAY:
            default:
                throw new IllegalArgumentException();
        }
    }

    static JSONObject readObject(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.BEGIN_OBJECT) {
            in.skipValue();
            return null;
        }
        JSONObject object = new JSONObject();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            Object value = readAny(in);
            if (name != null && value != null) {
                try {
                    object.put(name, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        in.endObject();
        return object;
    }

    /**
     * 读取JSONArray
     *
     * @throws IOException
     */
    static JSONArray readArray(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.BEGIN_ARRAY) {
            in.skipValue();
            return null;
        }
        JSONArray array = new JSONArray();
        in.beginArray();
        while (in.hasNext()) {
            Object next = readAny(in);
            if (next != null) {
                array.put(next);
            }
        }
        in.endArray();
        return array;
    }
}
