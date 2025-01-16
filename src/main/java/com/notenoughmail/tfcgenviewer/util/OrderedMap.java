package com.notenoughmail.tfcgenviewer.util;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface OrderedMap<K, V> extends Map<K, V> {

    int indexOf(K key);

    boolean remove(int index);

    @Nullable
    K getKey(int index);

    @Nullable
    default V getValue(int index) {
        final K key = getKey(index);
        if (key != null) {
            return get(key);
        }
        return null;
    }

    @Override
    @Nullable
    default V put(K key, V value) {
        return put(-1, key, value);
    }

    @Nullable
    V put(int index, K key, V value);

    @Nullable
    default V putBefore(K beforeKey, K key, V value) {
        return put(indexOf(beforeKey), key, value);
    }

    @Nullable
    default V putAfter(K afterKey, K key, V value) {
        int prevKeyIndex = indexOf(afterKey);
        if (prevKeyIndex != -1) {
            prevKeyIndex++;
        }
        return put(prevKeyIndex, key, value);
    }
}
