package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.RegistryLinkedColor;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.RegistryLinkedColorManager;
import net.dries007.tfc.util.data.DataManager;
import net.dries007.tfc.world.placement.ClimatePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClimateFeatureCache<C> {

    public static final DataManager.Reference<ColorDefinition> LAND = Colors.MISC_COLORS.getReference(TFCGenViewer.id("visualizable_feature_land"));
    public static final RegistryLinkedColorManager<PlacedFeature> FEATURES = new RegistryLinkedColorManager<>(TFCGenViewer.id("visualizable_feature_color"), Registries.PLACED_FEATURE);
    public static final TagKey<PlacedFeature> VISUALIZABLE_FEATURES = TagKey.create(Registries.PLACED_FEATURE, TFCGenViewer.id("visualizable_features"));

    private final Map<ResourceKey<Biome>, Set<ClimateSpace>> climates;
    public final C innerCache;

    public ClimateFeatureCache(RegistryAccess access, C innerCache) {
        this.innerCache = innerCache;
        climates = new IdentityHashMap<>();

        final Map<ResourceKey<PlacedFeature>, ClimateSpace> featureClimates = new IdentityHashMap<>();

        access.lookupOrThrow(Registries.PLACED_FEATURE)
                .get(VISUALIZABLE_FEATURES)
                .stream()
                .flatMap(HolderSet::stream)
                .forEach(holder -> {
                    final ClimatePlacement placement = findFirst(holder);
                    final ResourceKey<PlacedFeature> key = holder.unwrapKey().orElseThrow();
                    final ColorDefinition color = FEATURES.getInstanceColor(key);
                    if (placement != null && color != null) {
                        featureClimates.computeIfAbsent(key, k -> new ClimateSpace(
                                placement.getMinTemp(),
                                placement.getMaxTemp(),
                                placement.getMinGroundwater(),
                                placement.getMaxGroundwater(),
                                placement.getMinRainVariance(),
                                placement.getMaxRainVariance(),
                                color
                        ));
                    }
                });

        access.lookupOrThrow(Registries.BIOME)
                .listElements()
                .forEach(holder -> {
                    final ResourceKey<Biome> key = holder.key();
                    holder.value()
                            .getGenerationSettings()
                            .features()
                            .stream()
                            .flatMap(HolderSet::stream)
                            .map(Holder::unwrapKey)
                            .map(Optional::orElseThrow)
                            .map(featureClimates::get)
                            .filter(Objects::nonNull)
                            .forEach(space -> climates.computeIfAbsent(key, k -> new HashSet<>()).add(space));
                });
    }

    public List<ColorDefinition> search(ResourceKey<Biome> biome, float temperature, float rainfall, float rainfallVariance) {
        return climates.getOrDefault(biome, Set.of())
                .stream()
                .filter(climateSpace -> climateSpace.check(temperature, rainfall, rainfallVariance))
                .map(ClimateSpace::color)
                .toList();
    }

    public Component colorKey() {
        final MutableComponent key = Component.empty();
        FEATURES.getValues()
                .stream()
                .sorted()
                .map(RegistryLinkedColor::color)
                .forEach(color -> color.appendTo(key));
        LAND.get().appendTo(key);
        Colors.OCEAN.get().appendTo(key, true);
        return key;
    }

    @Nullable
    private static ClimatePlacement findFirst(Holder<PlacedFeature> holder) {
        return findFirst(holder.value().placement())
                .orElse(null);
    }

    private static Optional<ClimatePlacement> findFirst(List<PlacementModifier> modifiers) {
        return modifiers.stream()
                .filter(ClimatePlacement.class::isInstance)
                .map(ClimatePlacement.class::cast)
                .findFirst();
    }

    record ClimateSpace(float minTemp, float maxTemp, float minRain, float maxRain, float minRainVar, float maxRainVar, ColorDefinition color) {

        boolean check(float temperature, float rainfall, float rainVar) {
            return  temperature < maxTemp &&
                    temperature > minTemp &&
                    rainfall < maxRain &&
                    rainfall > minRain &&
                    rainVar < maxRainVar &&
                    rainVar > minRainVar;
        }
    }
}
