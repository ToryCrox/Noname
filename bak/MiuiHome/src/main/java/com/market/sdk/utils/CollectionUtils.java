package com.market.sdk.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionUtils {
    public static <T, K> HashMap<T, K> newHashMap() {
        return new HashMap();
    }

    public static <T, K> ConcurrentHashMap<T, K> newConconrrentHashMap() {
        return new ConcurrentHashMap();
    }

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList();
    }

    public static <T> HashSet<T> newHashSet() {
        return new HashSet();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
