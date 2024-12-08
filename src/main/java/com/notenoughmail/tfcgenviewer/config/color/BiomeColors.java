package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonElement;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.mixin.TFCLayersAccessor;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import com.notenoughmail.tfcgenviewer.util.ColorUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.util.RegisteredDataManager;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.TFCLayers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;
import java.util.function.Supplier;

public class BiomeColors extends RegisteredDataManager<ColorDefinition> {

    public static final BiomeColors Biomes = new BiomeColors();

    private static ColorDefinition unknown = new ColorDefinition(
            0xFFAAAAAA,
            Component.translatable("biome.tfcgenviewer.unknown"),
            100
    );
    private final CacheableSupplier<Component> key = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        types.values().stream().map(Entry::get).distinct().sorted().forEach(def -> {
            if (def != unknown) {
                def.appendTo(key);
            }
        });
        unknown.appendTo(key, true);
        return key;
    });

    private final Supplier<ColorDefinition> unknownGetter;
    private final Int2ObjectOpenHashMap<Component> colorDescriptors;

    private BiomeColors() {
        super(
                (id, json) -> ColorDefinition.parse(
                        json,
                        id.toLanguageKey("biome")
                ),
                id -> new ColorDefinition(
                        ColorUtil.randomColor(),
                        Component.translatable("tfcgenviewer.could_not_parse.biome", id.toString()),
                        1000
                ),
                TFCGenViewer.identifier("biomes"),
                "TFCGenViewer Biome"
        );
        unknownGetter = register(Colors.UNKNOWN);
        for (BiomeExtension ext : TFCLayersAccessor.tfcgenviewer$BiomeLayers()) {
            if (ext == null) break; // Encountering a null value means all registered biomes have been visited
            register(ext.key().location());
        }
        colorDescriptors = new Int2ObjectOpenHashMap<>(types.size());
    }

    public CacheableSupplier<Component> key() {
        return key;
    }

    public Component getColorDescriptor(int color) {
        return colorDescriptors.getOrDefault(color, unknown.descriptor());
    }

    public int color(int biome) {
        return getOrThrow(TFCLayers.getFromLayerId(biome).key().location()).get().color();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> colors, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        super.apply(colors, pResourceManager, pProfiler);
        unknown = unknownGetter.get();
        key.clearCache();
        colorDescriptors.clear();
        types.forEach((rl, def) -> colorDescriptors.put(def.get().color(), def.get().descriptor()));
    }
}
