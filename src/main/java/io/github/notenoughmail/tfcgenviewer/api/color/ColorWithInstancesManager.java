package io.github.notenoughmail.tfcgenviewer.api.color;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.util.data.DataManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ColorWithInstancesManager<T> extends DataManager<ColorWithInstancesManager.ColorWithInstances<T>> {

    private Map<ResourceKey<T>, ColorDefinition> flattened = Map.of();

    public ColorWithInstancesManager(ResourceLocation domain, ResourceKey<? extends Registry<T>> registry) {
        super(domain, ColorWithInstances.codec(registry));
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
        flattened = ImmutableMap.copyOf(map);
    }

    @Nullable
    public ColorDefinition getInstanceColor(ResourceKey<T> key) {
        return flattened.get(key);
    }

    public ColorDefinition getInstanceColor(ResourceKey<T> key, Reference<ColorWithInstances<T>> fallback) {
        final ColorDefinition color = getInstanceColor(key);
        return color == null ? fallback.get().color() : color;
    }

    // TODO: 1.21.1 | Better name than instances
    public record ColorWithInstances<T>(List<ResourceKey<T>> instances, ColorDefinition color, boolean overwrite) implements Comparable<ColorWithInstances<T>> {

        public ColorWithInstances(List<ResourceKey<T>> instances, ColorDefinition color) {
            this(instances, color, false);
        }

        public static <T> Codec<ColorWithInstances<T>> codec(ResourceKey<? extends Registry<T>> registry) {
            return RecordCodecBuilder.create(i -> i.group(
                    ResourceKey.codec(registry).listOf().fieldOf("instances").forGetter(ColorWithInstances::instances),
                    ColorDefinition.CODEC.fieldOf("color").forGetter(ColorWithInstances::color),
                    Codec.BOOL.optionalFieldOf("overwrite", false).forGetter(ColorWithInstances::overwrite)
            ).apply(i, ColorWithInstances::new));
        }

        public void flatten(Map<ResourceKey<T>, ColorDefinition> flattened, ResourceLocation id) {
            for (ResourceKey<T> key : instances) {
                if (flattened.put(key, color) != null && !overwrite) {
                    TFCGenViewer.LOGGER.error("{} redefines color for {}! if this is intentional please add \"overwrite\": true to you json definition", id, key);
                }
            }
        }

        @Override
        public int compareTo(@NotNull ColorWithInstancesManager.ColorWithInstances<T> o) {
            if (overwrite != o.overwrite) {
                return overwrite ? 1 : -1; // TODO: 1.21.1 | Is this backwards? the one that overwrites should be sorted to after
            } else {
                return color.compareTo(o.color);
            }
        }
    }
}
