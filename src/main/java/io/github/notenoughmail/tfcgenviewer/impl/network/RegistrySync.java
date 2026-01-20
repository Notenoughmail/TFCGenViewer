package io.github.notenoughmail.tfcgenviewer.impl.network;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.SynchronizationRequest;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RegistrySync implements SynchronizationRequest {

    private final RegistryAccess access;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryContents<?>> requestContents;

    public RegistrySync(RegistryAccess access) {
        this.access = access;
        requestContents = new IdentityHashMap<>();
    }

    private <T> RegistryContents<T> getContents(ResourceKey<? extends Registry<T>> registry) {
        return TFCGenViewer.cast(requestContents.computeIfAbsent(registry, RegistryContents::new));
    }

    @Override
    public <T> void request(ResourceKey<? extends Registry<T>> registry, Predicate<Holder<T>> elementFilter) {
        final RegistryContents<T> contents = getContents(registry);
        access.lookupOrThrow(registry)
                .listElements()
                .filter(elementFilter)
                .forEach(contents::putIfAbsent);
    }

    @Override
    public <T> void request(TagKey<T> tag) {
        access.lookupOrThrow(tag.registry()).get(tag).ifPresent(named -> {
            final RegistryContents<T> contents = getContents(tag.registry());
            named.forEach(contents::putIfAbsent);
            contents.addTag(named);
        });
    }

    public List<RegistryContents<?>> toSync() {
        return List.copyOf(requestContents.values());
    }
}
