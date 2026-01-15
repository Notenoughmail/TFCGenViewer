package com.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.CacheableSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class RockColors extends UnregisteredColorsHandler<RockColors.Processed> {

    public static final RockColors Rocks = new RockColors();

    protected RockColors() {
        super("rocks");
    }

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

    @Override
    protected Processed handle(Set<Map.Entry<ResourceLocation, Resource>> entries, ProfilerFiller profiler) {
        profiler.push("rock-colors");
        final Map<Block, ColorDefinition> definitions = new IdentityHashMap<>();
        @Nullable
        ColorDefinition unknown = null;

        for (Map.Entry<ResourceLocation, Resource> entry : entries) {
            final ResourceLocation loc = entry.getKey();
            final Resource resource = entry.getValue();
            if (loc.equals(Colors.UNKNOWN)) {
                try (final Reader reader = resource.openAsReader()) {
                    unknown = ColorDefinition.parse(parse(reader), "rock.tfcgenviewer.unknown");
                } catch (Exception e) {
                    TFCGenViewer.LOGGER.warn("TFCGenViewer Rock 'tfcgenviewer:unknown; failed to parse. Keeping previous value", e);
                }
            } else {
                final Block raw = ForgeRegistries.BLOCKS.getValue(loc);
                if (raw != null) {
                    try (final Reader reader = resource.openAsReader()) {
                        final JsonObject json = parse(reader);
                        if (ColorDefinition.isDisabled(json)) continue;
                        final ColorDefinition def = ColorDefinition.parse(json, raw.getDescriptionId());
                        definitions.put(raw, def);
                    } catch (Exception e) {
                        TFCGenViewer.LOGGER.warn("TFCGenViewer Rock '%s' failed to parse".formatted(loc), e);
                    }
                } else {
                    TFCGenViewer.LOGGER.warn("Unknown block '{}', skipping", loc);
                }
            }
        }
        profiler.pop();
        return new Processed(unknown, definitions);
    }

    @Override
    protected void apply(Processed processed, ResourceManager resourceManager, ProfilerFiller profiler) {
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
