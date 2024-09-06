package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.layer.TFCLayers;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.IdentityHashMap;
import java.util.Map;

public class BiomeColors extends SimpleJsonResourceReloadListener {

    public static final BiomeColors Biomes = new BiomeColors();

    private Map<BiomeExtension, ColorDefinition> colorDefinitions = new IdentityHashMap<>();

    private ColorDefinition unknown = new ColorDefinition(
            0xFFAAAAAA,
            Component.translatable("biome.tfcgenviewer.unknown"),
            100
    );
    private final CacheableSupplier<Component> key = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        colorDefinitions.values().stream().distinct().sorted().forEach(def -> def.appendTo(key));
        unknown.appendTo(key, true);
        return key;
    });

    private BiomeColors() {
        super(Colors.GSON, "tfcgenviewer/biomes");
    }

    public CacheableSupplier<Component> key() {
        return key;
    }

    public int color(int biome) {
        final ColorDefinition def = colorDefinitions.get(TFCLayers.getFromLayerId(biome));
        return def == null ? unknown.color() : def.color();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> colors, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        colorDefinitions = new IdentityHashMap<>(colors.size() - 1); // Account for unknown biome not being in map
        colors.forEach((id, json) -> {
            if (json.isJsonObject()) {
                final JsonObject obj = json.getAsJsonObject();
                if (id.equals(Colors.UNKNOWN)) {
                    unknown = ColorDefinition.parse(obj, unknown.color(), "biomes", id, "biome.tfcgenviewer.unknown");
                } else {
                    if (obj.has("disabled") && obj.get("disabled").isJsonPrimitive() && obj.getAsJsonPrimitive("disabled").getAsBoolean()) return;
                    final BiomeExtension biome = TFCBiomes.getById(id);
                    if (biome != null) {
                        colorDefinitions.put(
                                biome,
                                ColorDefinition.parse(obj, unknown.color(), "biomes", id, Util.makeDescriptionId("biome", id))
                        );
                    } else {
                        TFCGenViewer.LOGGER.warn("Unknown biome \"{}\", skipping", id);
                    }
                }
            } else {
                TFCGenViewer.LOGGER.warn("Biome color \"{}\" was not a json object, skipping", id);
            }
        });
        key.clearCache();
    }
}
