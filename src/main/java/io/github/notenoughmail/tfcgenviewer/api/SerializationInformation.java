package io.github.notenoughmail.tfcgenviewer.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public interface SerializationInformation {

    /**
     * Describe how the contents of the registry are serialized
     */
    default <T> void provide(ResourceKey<? extends Registry<T>> registry, Codec<T> codec) {
        provide(registry, ByteBufCodecs.fromCodecWithRegistriesTrusted(codec));
    }

    <T> void provide(ResourceKey<? extends Registry<T>> registry, StreamCodec<? super RegistryFriendlyByteBuf, T> codec);
}
