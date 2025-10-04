package com.notenoughmail.tfcgenviewer.color;

import com.google.gson.JsonObject;
import com.notenoughmail.tfcgenviewer.TFCGenViewer;
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
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.*;
import java.util.function.Function;

public class FeatureColors extends UnregisteredColorsHandler<Map<ResourceKey<PlacedFeature>, ColorDefinition>> implements Function<RegistryAccess, Component> {

    public static final FeatureColors Features = new FeatureColors();

    public static final Component MULTIPLE_FEATURES = Component.translatable("tfcgenviewer.climate_features.multiple_features_present");

    private static final String LANG_KEY_TYPE = TFCGenViewer.ID + ".feature";

    private final Map<ResourceKey<PlacedFeature>, ColorDefinition> definitions = new IdentityHashMap<>();

    private HolderLookup<Biome> biomeLookup;
    private Map<ResourceKey<Biome>, Set<ClimateSpace>> climateCache;

    protected FeatureColors() {
        super("features");
    }

    public List<ColorDefinition> search(int biome, float temperature, float rainfall) {
        final ResourceKey<Biome> biomeKey = TFCLayers.getFromLayerId(biome).key();
        return climateCache.getOrDefault(biomeKey, Set.of())
                .stream()
                .filter(climateSpace -> climateSpace.check(temperature, rainfall))
                .map(climateSpace -> {
                    final Holder<PlacedFeature> featureHolder = climateSpace.feature();
                    if (featureHolder.get().feature().get().config() instanceof IVeinConfig vein) {
                        final Holder<Biome> biomeHolder = biomeLookup.getOrThrow(biomeKey);
                        if (!vein.config().biomes().map(biomeHolder::is).orElse(true)) {
                            return null;
                        }
                    }
                    return definitions.get(featureHolder.unwrapKey().orElseThrow());
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void prime(RegistryAccess registryAccess) {
        if (climateCache == null) {
            climateCache = new IdentityHashMap<>();
            biomeLookup = registryAccess.lookupOrThrow(Registries.BIOME);

            final Map<ResourceKey<PlacedFeature>, ClimateSpace> featureClimates = new IdentityHashMap<>();

            registryAccess.lookupOrThrow(Registries.PLACED_FEATURE)
                    .get(TFCGenViewer.VISUALIZABLE_FEATURES)
                    .stream()
                    .flatMap(HolderSet::stream)
                    .forEach(holder -> {
                        final ClimatePlacement placement = findFirst(holder);
                        if (placement != null) {
                            featureClimates.computeIfAbsent(holder.unwrapKey().orElseThrow(), key -> new ClimateSpace(
                                    placement.getMinTemp(),
                                    placement.getMaxTemp(),
                                    placement.getMinRainfall(),
                                    placement.getMaxRainfall(),
                                    holder
                            ));
                        }
                    });

            biomeLookup.listElements()
                    .forEach(holder -> {
                        final ResourceKey<Biome> key = holder.key();
                        final Biome biome = holder.get();
                        biome.getGenerationSettings()
                                .features()
                                .stream()
                                .flatMap(HolderSet::stream)
                                .map(h -> h.unwrapKey().orElseThrow())
                                .map(featureClimates::get)
                                .filter(Objects::nonNull)
                                .forEach(space -> climateCache.computeIfAbsent(key, k -> new HashSet<>())
                                        .add(space));
                    });
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
        climateCache = null;
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
        return findFirst(holder.get().placement())
                .orElse(null);
    }

    public static Optional<ClimatePlacement> findFirst(List<PlacementModifier> modifiers) {
        return modifiers.stream()
                .filter(ClimatePlacement.class::isInstance)
                .map(ClimatePlacement.class::cast)
                .findFirst();
    }

    private record ClimateSpace(float minTemp, float maxTemp, float minRain, float maxRain, Holder<PlacedFeature> feature) {

        boolean check(float temperature, float rainfall) {
            return  temperature < maxTemp &&
                    temperature > minTemp &&
                    rainfall < maxRain &&
                    rainfall > minRain;
        }

    }
}
