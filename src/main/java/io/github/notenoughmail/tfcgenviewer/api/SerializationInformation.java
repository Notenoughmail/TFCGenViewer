package io.github.notenoughmail.tfcgenviewer.api;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface SerializationInformation {

    /**
     * Describe how the contents of the registry are serialized
     */
    <T> void provide(ResourceKey<? extends Registry<T>> registry, Codec<T> codec);
}
