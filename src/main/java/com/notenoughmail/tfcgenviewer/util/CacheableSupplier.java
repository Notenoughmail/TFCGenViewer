package com.notenoughmail.tfcgenviewer.util;

import java.util.function.Supplier;

/**
 * A clearable reimplementation of {@link net.minecraftforge.common.util.Lazy Lazy}
 * @param <T> the type results returned by the supplier
 */
public class CacheableSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T value;

    public CacheableSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
        value = null;
    }

    public void clearCache() {
        value = null;
    }

    @Override
    public T get() {
        return value == null ? value = supplier.get() : value;
    }
}
