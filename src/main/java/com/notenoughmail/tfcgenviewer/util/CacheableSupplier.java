package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.core.RegistryAccess;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A clearable reimplementation of {@link net.minecraftforge.common.util.Lazy Lazy}
 * @param <T> the type results returned by the supplier
 */
public class CacheableSupplier<T> implements Supplier<T>, Function<RegistryAccess, T> {

    public static <T> CacheableSupplier<T> of(Supplier<T> supplier) {
        return new CacheableSupplier<>(supplier);
    }

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

    @Override
    public T apply(RegistryAccess registryAccess) {
        return get();
    }
}
