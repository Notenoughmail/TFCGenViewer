package io.github.notenoughmail.tfcgenviewer.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.function.Predicate;

public interface SynchronizationRequest {

    /**
     * Request the contents of the given registry to be synced
     */
    default <T> void request(ResourceKey<? extends Registry<T>> registry) {
        request(registry, h -> true);
    }

    /**
     * Request the ids and values of the tag contents to be synced
     */
    <T> void request(TagKey<T> tag);

    /**
     * Request a sub-set of the given registry to be synced
     */
    <T> void request(ResourceKey<? extends Registry<T>> registry, Predicate<Holder<T>> elementFilter);
}
