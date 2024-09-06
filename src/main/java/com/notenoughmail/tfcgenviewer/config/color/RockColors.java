package com.notenoughmail.tfcgenviewer.config.color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.IdentityHashMap;
import java.util.Map;

public class RockColors extends SimpleJsonResourceReloadListener {

    public static final RockColors Rocks = new RockColors();

    private Map<Block, ColorDefinition> colorDefinitions = new IdentityHashMap<>();
    private ColorDefinition unknown = new ColorDefinition(
            0xFFE358FF,
            Component.translatable("rock.tfcgenviewer.unknown"),
            100
    );
    private final CacheableSupplier<Component> key = new CacheableSupplier<>(() -> {
        final MutableComponent key = Component.empty();
        colorDefinitions.values().stream().distinct().sorted().forEach(def -> def.appendTo(key));
        unknown.appendTo(key, true);
        return key;
    });

    private RockColors() {
        super(Colors.GSON, "tfcgenviewer/rocks");
    }

    public CacheableSupplier<Component> key() {
        return key;
    }

    public int color(Block raw) {
        final ColorDefinition def = colorDefinitions.get(raw);
        return def == null ? unknown.color() : def.color();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> colors, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        colorDefinitions = new IdentityHashMap<>(colors.size() - 1); // Account for unknown rock not being in map
        colors.forEach((id, json) -> {
            if (json.isJsonObject()) {
                final JsonObject obj = json.getAsJsonObject();
                if (id.equals(Colors.UNKNOWN)) {
                    unknown = ColorDefinition.parse(obj, unknown.color(), "rocks", id, "rock.tfcgenviewer.unknown");
                } else {
                    if (obj.has("disabled") && obj.get("disabled").isJsonPrimitive() && obj.getAsJsonPrimitive("disabled").getAsBoolean()) return;
                    final Block raw = ForgeRegistries.BLOCKS.getValue(id);
                    if (raw != null) {
                        colorDefinitions.put(
                                raw,
                                ColorDefinition.parse(obj, unknown.color(), "rocks", id, raw.getDescriptionId())
                        );
                    } else {
                        TFCGenViewer.LOGGER.warn("Unknown block \"{}\", skipping", id);
                    }
                }
            } else {
                TFCGenViewer.LOGGER.warn("Rock color \"{}\" was not a json object, skipping", id);
            }
        });
        key.clearCache();
    }
}
