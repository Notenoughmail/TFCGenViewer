package io.github.notenoughmail.tfcgenviewer.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.*;

public record RegistryContents<T>(ResourceKey<? extends Registry<T>> registry, Map<ResourceKey<T>, T> values, Map<ResourceLocation, List<ResourceKey<T>>> tags) {

    public static final RegistrationInfo REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    public static final StreamCodec<FriendlyByteBuf, ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = StreamCodec.of(FriendlyByteBuf::writeResourceKey, FriendlyByteBuf::readRegistryKey);

    public RegistryContents(ResourceKey<? extends Registry<?>> registry) {
        this(TFCGenViewer.cast(registry), new IdentityHashMap<>(), new HashMap<>());
    }

    public void putIfAbsent(Holder<T> holder) {
        values.computeIfAbsent(holder.getKey(), k -> holder.value());
    }

    public void addTag(HolderSet.Named<T> named) {
        tags.put(named.key().location(), named.stream().map(Holder::getKey).toList());
    }

    public RegistryAccess.RegistryEntry<T> asRegistry() {
        final MappedRegistry<T> reg = new MappedRegistry<>(registry, Lifecycle.experimental());
        values.forEach((key, value) -> reg.register(key, value, REGISTRATION_INFO));
        reg.bindTags(TFCGenViewer.mapFromEntries(
                tags.entrySet().stream()
                        .map(e -> Map.entry(
                                TagKey.create(this.registry, e.getKey()),
                                e.getValue().stream()
                                        .<Holder<T>>map(reg::getHolderOrThrow)
                                        .toList()
                        )),
                IdentityHashMap::new
        ));
        return new RegistryAccess.RegistryEntry<>(registry, reg);
    }

    public static <T> StreamCodec<FriendlyByteBuf, RegistryContents<T>> registryStreamCodec(Codec<T> codec, ResourceKey<? extends Registry<T>> registry) {
        final StreamCodec<FriendlyByteBuf, ResourceKey<? extends Registry<T>>> registryKeyCodec = TFCGenViewer.cast(REGISTRY_KEY_CODEC);
        final StreamCodec<FriendlyByteBuf, Map<ResourceKey<T>, T>> valuesCodec = ByteBufCodecs.map(
                IdentityHashMap::new,
                StreamCodec.of(FriendlyByteBuf::writeResourceKey, b -> b.readResourceKey(registry)),
                ByteBufCodecs.fromCodec(codec)
        );
        final StreamCodec<FriendlyByteBuf, Map<ResourceLocation, List<ResourceKey<T>>>> tagsCodec = ByteBufCodecs.map(
                HashMap::new,
                ResourceLocation.STREAM_CODEC,
                ResourceKey.streamCodec(registry).apply(ByteBufCodecs.list())
        );
        return StreamCodec.composite(
                registryKeyCodec, RegistryContents::registry,
                valuesCodec, RegistryContents::values,
                tagsCodec, RegistryContents::tags,
                RegistryContents::new
        );
    }
}
