package io.github.notenoughmail.tfcgenviewer.api.color.manager;

import com.google.gson.JsonElement;
import io.github.notenoughmail.tfcgenviewer.api.color.CachedColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.dries007.tfc.util.data.DataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.function.Function;

public class ColorManager extends DataManager<ColorDefinition> implements CachedColorKey {

    private final Function<ColorManager, Component> colorKeySource;
    private final Reference<ColorDefinition> unknown;
    private Component colorKey;

    public ColorManager(ResourceLocation domain) {
        this(domain, ColorManager::createColorKey);
    }

    public ColorManager(ResourceLocation domain, Function<ColorManager, Component> colorKeySource) {
        super(domain, ColorDefinition.CODEC);
        this.colorKeySource = colorKeySource;
        unknown = getReference(Colors.UNKNOWN);
    }

    public ColorDefinition unknown() {
        return unknown.get();
    }

    public ColorDefinition getOrUnknown(ResourceLocation id) {
        final ColorDefinition def = get(id);
        return def == null ? unknown() : def;
    }

    @Override
    public void clearCache() {
        colorKey = null;
    }

    @Override
    public Component colorKey() {
        if (colorKey == null) {
            colorKey = colorKeySource.apply(this);
        }
        return colorKey;
    }

    private Component createColorKey() {
        final MutableComponent key = Component.empty();
        getValues().stream()
                .distinct()
                .sorted()
                .filter(d -> d != unknown())
                .forEach(d -> d.appendTo(key));
        unknown().appendTo(key, true);
        return key;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        super.apply(elements, resourceManagerIn, profilerIn);
        clearCache();
    }
}
