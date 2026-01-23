package io.github.notenoughmail.tfcgenviewer.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Holder} which originates from over-the-network via {@link io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType#elementCodecForRegistry(ResourceKey) server information sync}.
 * Network holders are not required to have a value, and exist to fulfill the requirements of some registry elements
 * while maintaining TFCGenViewer's dedication to minimizing packet sizes.
 */
public class NetworkHolder<T> extends Holder.Reference<T> {

    public static <T> NetworkHolder<T> of(ResourceKey<T> key, @Nullable T value) {
        return new NetworkHolder<>(key, value);
    }

    public static <T> NetworkHolder<T> of(ResourceKey<T> key) {
        return of(key, null);
    }

    protected NetworkHolder(ResourceKey<T> key, @Nullable T value) {
        super(Type.STAND_ALONE, Universal.owner(), key, value);
    }
}
