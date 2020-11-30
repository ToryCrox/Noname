package com.tory.library.utils.json;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/9/21
 * Description: 基础类型的帮助类
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/9/21 xutao 1.0
 * Why & What is modified:
 */
class PrimitiveHelper {

    //所有的数字类型
    private static final Class<?>[] PRIMITIVE_NUMBER_TYPES = { int.class, long.class, short.class,
        float.class, double.class, byte.class, Integer.class, Long.class,
        Short.class, Float.class, Double.class, Byte.class };

    //所有基本类型
    private static final Class<?>[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
        float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
        Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };

    //是否为Number类型
    public static <T> boolean isPrimitiveNumber(Class<T> rawType) {
        return isTargetType(PRIMITIVE_NUMBER_TYPES, rawType);
    }

    //是否为基本类型
    public static <T> boolean isPrimitive(Class<T> rawType) {
        return isTargetType(PRIMITIVE_TYPES, rawType);
    }

    //是否为目标类型
    private static <T> boolean isTargetType(Class<?>[] targets, Class<T> rawType) {
        for (Class<?> standardPrimitive : targets) {
            if (standardPrimitive.isAssignableFrom(rawType)) {
                return true;
            }
        }
        return false;
    }
}
