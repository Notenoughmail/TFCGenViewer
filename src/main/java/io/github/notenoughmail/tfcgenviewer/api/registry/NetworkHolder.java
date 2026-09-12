package io.github.notenoughmail.tfcgenviewer.api.registry;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Holder} which originates from over-the-network via {@link ISyncRegistries#elementCodecForRegistry(SerializationInformation) server information sync}.
 * <p>
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
                NetworkHolder::of
        );
    }

    public static <C> StreamCodec<RegistryFriendlyByteBuf, Holder<C>> slimStreamCodec(StreamCodec<? super RegistryFriendlyByteBuf, C> codec, ResourceKey<? extends Registry<C>> registry) {
        return StreamCodec.of(
                (b, h) -> {
                    final ResourceKey<C> key = h.getKey();
                    if (key != null) {
                        b.writeBoolean(true);
                        b.writeResourceKey(key);
                    } else {
                        b.writeBoolean(false);
                        codec.encode(b, h.value());
                    }
                },
                b -> {
                    final boolean keyOnly = b.readBoolean();
                    if (keyOnly) {
                        final ResourceKey<C> key = b.readResourceKey(registry);
                        return b.registryAccess().holder(key).orElseGet(() -> NetworkHolder.of(key));
                    } else {
                        return new NetworkHolder<>(codec.decode(b), registry);
                    }
                }
        );
    }

    protected NetworkHolder(ResourceKey<T> key, @Nullable T value) {
        super(Type.STAND_ALONE, Universal.owner(), key, value);
    }

    private NetworkHolder(T value, ResourceKey<? extends Registry<T>> registry) {
        super(Type.INTRUSIVE, Universal.owner(), anonymous(registry), value);
    }

    public boolean isAnonymous() {
        return key().location() == ANONYMOUS;
    }

    private static final ResourceLocation ANONYMOUS = TFCGenViewer.id("anonymous");

    private static <T> ResourceKey<T> anonymous(ResourceKey<? extends Registry<T>> registry) {
        return ResourceKey.create(registry, ANONYMOUS);
    }
}
