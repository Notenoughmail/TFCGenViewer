package io.github.notenoughmail.tfcgenviewer.api.registry;

import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Holder} which originates from over-the-network via {@link ISyncRegistries#elementCodecForRegistry(SerializationInformation) server information sync}.
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

    public static <B extends ByteBuf, C> StreamCodec<B, Holder<C>> streamCodec(StreamCodec<B, C> codec, ResourceKey<? extends Registry<C>> registry) {
        return StreamCodec.composite(
                ResourceKey.streamCodec(registry), Holder::getKey,
                codec, Holder::value,
                NetworkHolder::new
        );
    }

    protected NetworkHolder(ResourceKey<T> key, @Nullable T value) {
        super(Type.STAND_ALONE, Universal.owner(), key, value);
    }
}
