package io.github.notenoughmail.tfcgenviewer.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Predicate;

public final class SynchronizationRequest {

    public <T> void request(ResourceKey<? extends Registry<T>> registry, StreamCodec<RegistryFriendlyByteBuf, T> codec) {

    }

    public <T> void request(TagKey<T> tag, StreamCodec<RegistryFriendlyByteBuf, T> codec) {

    }

    public <T> void request(ResourceKey<? extends Registry<T>> registry, StreamCodec<RegistryFriendlyByteBuf, T> codec, Predicate<Holder<T>> elementFilter) {

    }

    <T> void syncContents(TagKey<T> tag) {

    }
}
