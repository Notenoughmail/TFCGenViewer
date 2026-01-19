package io.github.notenoughmail.tfcgenviewer.api.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record RegistryLinkedColor<T>(List<ResourceKey<T>> keys, ColorDefinition color, boolean overwrite) implements Comparable<RegistryLinkedColor<T>> {

    public RegistryLinkedColor(List<ResourceKey<T>> instances, ColorDefinition color) {
        this(instances, color, false);
    }

    public static <T> Codec<RegistryLinkedColor<T>> codec(ResourceKey<? extends Registry<T>> registry) {
        return RecordCodecBuilder.create(i -> i.group(
                ResourceKey.codec(registry).listOf().fieldOf("keys").forGetter(RegistryLinkedColor::keys),
                ColorDefinition.CODEC.fieldOf("color").forGetter(RegistryLinkedColor::color),
                Codec.BOOL.optionalFieldOf("overwrite", false).forGetter(RegistryLinkedColor::overwrite)
        ).apply(i, RegistryLinkedColor::new));
    }

    public void flatten(Map<ResourceKey<T>, ColorDefinition> flattened, ResourceLocation id) {
        for (ResourceKey<T> key : keys) {
            if (flattened.put(key, color) != null && !overwrite) {
                TFCGenViewer.LOGGER.error("{} redefines color for {}! If this is intentional please add '\"overwrite\": true' to the json definition", id, key);
            }
        }
    }

    @Override
    public int compareTo(@NotNull RegistryLinkedColor<T> o) {
        if (overwrite != o.overwrite) {
            return overwrite ? 1 : -1;
        } else {
            return color.compareTo(o.color);
        }
    }
}
