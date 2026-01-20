package io.github.notenoughmail.tfcgenviewer.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Predicate;

public interface SynchronizationRequest {

    default <T> void request(ResourceKey<? extends Registry<T>> registry) {
        request(registry, h -> true);
    }

    <T> void request(TagKey<T> tag);

    <T> void request(ResourceKey<? extends Registry<T>> registry, Predicate<Holder<T>> elementFilter);
}
