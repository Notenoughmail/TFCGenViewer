package com.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import net.dries007.tfc.util.DataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.IdentityHashMap;
import java.util.Map;

public class RockColors extends SimplePreparableReloadListener<RockColors.Processed> {

    public static final RockColors Rocks = new RockColors();
    public static final String DIRECTORY = "tfcgenviewer/rocks";

    private Map<Block, ColorDefinition> colorDefinitions = new IdentityHashMap<>();
    private ColorDefinition unknown = new ColorDefinition(
            0xFFE358FF,
            Component.translatable("rock.tfcgenviewer.unknown"),
            100
    );
    private final CacheableSupplier<Component> key = CacheableSupplier.of(() -> {
        final MutableComponent key = Component.empty();
        colorDefinitions.values().stream().filter(ColorDefinition::enabled).distinct().sorted().forEach(def -> def.appendTo(key));
        unknown.appendTo(key, true);
        return key;
    });

    private RockColors() {
    }

    @Override
    protected Processed prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        final FileToIdConverter converter = FileToIdConverter.json(DIRECTORY);
        final Map<Block, ColorDefinition> definitions = new IdentityHashMap<>();
        @Nullable
        ColorDefinition unknown = null;

        for (Map.Entry<ResourceLocation, Resource> entry : converter.listMatchingResources(pResourceManager).entrySet()) {
            final ResourceLocation loc = entry.getKey();
            if (loc.equals(Colors.UNKNOWN)) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    unknown = ColorDefinition.parse(parse(reader), "rock.tfcgenviewer.unknown");
                } catch (Exception e) {
                    TFCGenViewer.LOGGER.warn("TFCGenViewer Rock 'tfcgenviewer:unknown' failed to parse. {}: {}, keeping previous value", e.getClass().getSimpleName(), e.getMessage());
                }
            } else {
                final Block raw = ForgeRegistries.BLOCKS.getValue(loc);
                if (raw != null) {
                    try (Reader reader = entry.getValue().openAsReader()) {
                        final JsonObject json = parse(reader);
                        if (json.has("disabled") && json.get("disabled").isJsonPrimitive() && json.get("disabled").getAsBoolean()) continue;
                        var def = ColorDefinition.parse(json, raw.getDescriptionId());
                        definitions.put(raw, def);
                    } catch (Exception e) {
                        TFCGenViewer.LOGGER.warn("TFCGenViewer Rock '{}', failed to parse. {}: {}", loc, e.getClass().getSimpleName(), e.getMessage());
                    }
                } else {
                    TFCGenViewer.LOGGER.warn("Unknown block \"{}\", skipping", loc);
                }
            }
        }
        return new Processed(unknown, definitions);
    }

    private static JsonObject parse(Reader reader) {
        return GsonHelper.fromJson(DataManager.GSON, reader, JsonObject.class);
    }

    @Override
    protected void apply(Processed processed, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        colorDefinitions = processed.colors();
        if (processed.unknown() != null) {
            unknown = processed.unknown();
        }
        key.clearCache();
    }

    public CacheableSupplier<Component> key() {
        return key;
    }

    public ColorDefinition color(Block raw) {
        return colorDefinitions.getOrDefault(raw, unknown);
    }

    protected record Processed(@Nullable ColorDefinition unknown, Map<Block, ColorDefinition> colors) {}
}
