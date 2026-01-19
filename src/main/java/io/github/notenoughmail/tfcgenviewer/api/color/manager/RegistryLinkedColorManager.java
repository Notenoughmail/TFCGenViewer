package io.github.notenoughmail.tfcgenviewer.api.color.manager;

import com.google.gson.JsonElement;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.RegistryLinkedColor;
import net.dries007.tfc.util.data.DataManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class RegistryLinkedColorManager<T> extends DataManager<RegistryLinkedColor<T>> {

    private Map<ResourceKey<T>, ColorDefinition> flattened = Map.of();

    public RegistryLinkedColorManager(ResourceLocation domain, ResourceKey<? extends Registry<T>> registry) {
        super(domain, RegistryLinkedColor.codec(registry));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        super.apply(elements, resourceManagerIn, profilerIn);
        final Map<ResourceKey<T>, ColorDefinition> map = new IdentityHashMap<>();
        getElements()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> e.getValue().flatten(map, e.getKey()));
        flattened = Collections.unmodifiableMap(map);
    }

    @Nullable
    public ColorDefinition getInstanceColor(ResourceKey<T> key) {
        return flattened.get(key);
    }

    public ColorDefinition getInstanceColor(ResourceKey<T> key, Reference<RegistryLinkedColor<T>> fallback) {
        final ColorDefinition color = getInstanceColor(key);
        return color == null ? fallback.get().color() : color;
    }
}
