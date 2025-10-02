package com.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.feature.vein.IVeinConfig;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.placement.ClimatePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public class FeatureColors extends UnregisteredColorsHandler<Map<ResourceKey<PlacedFeature>, ColorDefinition>> implements Function<RegistryAccess, Component> {

    public static final FeatureColors Features = new FeatureColors();

    public static final Component MULTIPLE_FEATURES = Component.translatable("tfcgenviewer.climate_features.multiple_features_present");

    private static final String LANG_KEY_TYPE = TFCGenViewer.ID + ".feature";

    private final Map<ResourceKey<PlacedFeature>, ColorDefinition> definitions = new IdentityHashMap<>();

    private Map<ResourceKey<PlacedFeature>, Pair<FeatureConfiguration, ClimatePlacement>> placements;
    private HolderLookup<Biome> biomeLookup;

    protected FeatureColors() {
        super("features");
    }

    // TODO: 1.5.1 | Is there any way some of this information can be cached in #prime
    public List<ColorDefinition> search(int biome, float temperature, float rainfall) {
        return placements.entrySet().stream()
                .filter(entry -> {
                    final ClimatePlacement placement = entry.getValue().getSecond();
                    final FeatureConfiguration featureConfig = entry.getValue().getFirst();
                    final ResourceKey<PlacedFeature> featureKey = entry.getKey();

                    final BiomeExtension biomeExt = TFCLayers.getFromLayerId(biome);
                    final Holder<Biome> biomeHolder = biomeLookup.getOrThrow(biomeExt.key());
                    boolean valid = biomeHolder.get().getGenerationSettings().features()
                            .stream()
                            .flatMap(HolderSet::stream)
                            .map(h -> h.unwrapKey().orElse(null))
                            .filter(Objects::nonNull)
                            .anyMatch(key -> key == featureKey);

                    if (featureConfig instanceof IVeinConfig vein) {
                        valid &= vein.config().biomes().map(biomeHolder::is).orElse(true);
                    }

                    final boolean
                            minTemp = temperature > placement.getMinTemp(),
                            maxTemp = temperature < placement.getMaxTemp(),
                            minRain = rainfall > placement.getMinRainfall(),
                            maxRain = rainfall < placement.getMaxRainfall();
                    return valid && minRain && minTemp && maxRain && maxTemp;
                })
                .map(Map.Entry::getKey)
                .map(definitions::get)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    public void prime(RegistryAccess registryAccess) {
        if (placements == null) {
            biomeLookup = registryAccess.lookupOrThrow(Registries.BIOME);
            placements = TFCGenViewer.ofEntryStream(TFCGenViewer.cast(
                    registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                            .get(TFCGenViewer.VISUALIZABLE_FEATURES)
                            .stream()
                            .flatMap(HolderSet::stream)
                            .map(holder -> {
                                final ClimatePlacement placement = findFirst(holder);
                                if (placement != null) {
                                    return Map.entry(
                                            ((Holder.Reference<PlacedFeature>) holder).key(),
                                            Pair.of(holder.get().feature().get().config(), placement)
                                    );
                                }
                                return null;
                            })
                            .filter(Objects::nonNull)
            ));
        }
    }

    @Override
    protected Map<ResourceKey<PlacedFeature>, ColorDefinition> handle(Set<Map.Entry<ResourceLocation, Resource>> entries, ProfilerFiller profiler) {
        profiler.push("feature-colors");
        final Map<ResourceKey<PlacedFeature>, ColorDefinition> defs = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : entries) {
            final ResourceLocation loc = entry.getKey();
            final Resource resource = entry.getValue();
            try (final Reader reader = resource.openAsReader()) {
                final JsonObject json = parse(reader);
                if (ColorDefinition.isDisabled(json)) continue;
                final ColorDefinition def = ColorDefinition.parse(json, loc.toLanguageKey(LANG_KEY_TYPE));
                defs.put(ResourceKey.create(Registries.PLACED_FEATURE, loc), def);
            } catch (Exception e) {
                TFCGenViewer.LOGGER.warn("TFCGenViewer Feature '%s' failed to parse".formatted(loc), e);
            }
        }
        profiler.pop();
        return defs;
    }

    @Override
    protected void apply(Map<ResourceKey<PlacedFeature>, ColorDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        definitions.clear();
        definitions.putAll(map);
    }

    @Override
    public Component apply(RegistryAccess registryAccess) {
        placements = null;
        biomeLookup = null;
        final MutableComponent key = Component.empty();
        registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                .get(TFCGenViewer.VISUALIZABLE_FEATURES)
                .stream()
                .flatMap(HolderSet::stream)
                .map(holder -> {
                    if (findFirst(holder) != null) {
                        final ResourceKey<PlacedFeature> loc = ((Holder.Reference<PlacedFeature>) holder).key();
                        return definitions.get(loc);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(c -> c.appendTo(key));
        Colors.RT_LAND.get().appendTo(key);
        Colors.FILL_OCEAN.get().appendTo(key, true);
        return key;
    }

    @Nullable
    private static ClimatePlacement findFirst(Holder<PlacedFeature> holder) {
        return holder.get().placement().stream()
                .filter(mod -> mod instanceof ClimatePlacement)
                .map(ClimatePlacement.class::cast)
                .findFirst()
                .orElse(null);
    }
}
