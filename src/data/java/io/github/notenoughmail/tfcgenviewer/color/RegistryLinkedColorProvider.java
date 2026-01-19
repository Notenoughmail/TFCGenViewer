package io.github.notenoughmail.tfcgenviewer.color;

import io.github.notenoughmail.tfcgenviewer.DataManagerProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.RGB;
import io.github.notenoughmail.tfcgenviewer.api.color.RegistryLinkedColor;
import io.github.notenoughmail.tfcgenviewer.impl.ClimateFeatureCache;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.TFCLayers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.dries007.tfc.world.layer.TFCLayers.*;

public class RegistryLinkedColorProvider extends DataManagerProvider {

    public RegistryLinkedColorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, "RegistryLinkedColor");
    }

    @Override
    protected void make(HolderLookup.Provider l) {

        makeFor(ClimateFeatureCache.FEATURES, features -> {
            feature(
                    features,
                    "coral",
                    ColorDefinition.of(
                            255, 153, 22,
                            Component.translatable("tfcgenviewer.visualized_feature.coral"),
                            null
                    ),
                    Helpers.identifier("coral_mushroom"),
                    Helpers.identifier("coral_tree"),
                    Helpers.identifier("coral_claw")
            );
            feature(
                    features,
                    "kaolin",
                    ColorDefinition.of(
                            255, 114, 156,
                            Component.translatable("tfcgenviewer.visualized_feature.kaolin"),
                            null
                    ),
                    Helpers.identifier("vein/kaolin_disc")
            );
        });
        makeFor(Colors.BIOME_COLORS, biomes -> {
            biomes.accept("unknown", new RegistryLinkedColor<>(
                    List.of(),
                    ColorDefinition.of(
                            0, 0, 0,
                            Component.translatable("biome.tfcgenviewer.unknown")
                    )
            ));
            // Mostly based on colors & biomes of RegionGeneratorTests, but some merging has occurred
            biome(
                    biomes,
                    "ocean",
                    0, 0, 220,
                    OCEAN
            );
            biome(
                    biomes,
                    "ocean_reef",
                    70, 160, 250,
                    OCEAN_REEF
            );
            biome(
                    biomes,
                    "deep_ocean",
                    0, 0, 160,
                    DEEP_OCEAN
            );
            biome(
                    biomes,
                    "deep_ocean_trench",
                    0, 0, 80,
                    DEEP_OCEAN_TRENCH
            );
            biome(
                    biomes,
                    "mountain_lake",
                    20, 180, 255,
                    MOUNTAIN_LAKE,
                    OCEANIC_MOUNTAIN_LAKE,
                    OLD_MOUNTAIN_LAKE,
                    VOLCANIC_MOUNTAIN_LAKE,
                    PLATEAU_LAKE,
                    VOLCANIC_OCEANIC_MOUNTAIN_LAKE
            );
            biome(
                    biomes,
                    "lake",
                    30, 30, 255,
                    LAKE,
                    MELTWATER_LAKE
            );
            biome(
                    biomes,
                    "river",
                    0, 200, 255,
                    RIVER
            );
            biome(
                    biomes,
                    "oceanic_mountains",
                    255, 0, 255,
                    OCEANIC_MOUNTAINS,
                    VOLCANIC_OCEANIC_MOUNTAINS
            );
            biome(
                    biomes,
                    "canyons",
                    180, 60, 255,
                    CANYONS,
                    TOWER_KARST_CANYONS,
                    SHILIN_CANYONS,
                    DOLINE_CANYONS,
                    CENOTE_CANYONS
            );
            biome(
                    biomes,
                    "low_canyons",
                    200, 110, 255,
                    LOW_CANYONS
            );
            biome(
                    biomes,
                    "lowlands",
                    220, 150, 230,
                    LOWLANDS,
                    TOWER_KARST_BAY,
                    SALT_MARSH,
                    TOWER_KARST_LAKE
            );
            biome(
                    biomes,
                    "mountains",
                    255, 50, 50,
                    MOUNTAINS,
                    VOLCANIC_MOUNTAINS
            );
            biome(
                    biomes,
                    "old_mountains",
                    240, 100, 100,
                    OLD_MOUNTAINS,
                    EXTREME_DOLINE_MOUNTAINS
            );
            biome(
                    biomes,
                    "plateau",
                    190, 120, 120,
                    PLATEAU,
                    EXTREME_DOLINE_PLATEAU,
                    CENOTE_PLATEAU,
                    DOLINE_PLATEAU,
                    SHILIN_PLATEAU,
                    BURREN_PLATEAU,
                    PLATEAU_WIDE
            );
            biome(
                    biomes,
                    "highlands",
                    20, 80, 30,
                    HIGHLANDS,
                    SHILIN_HIGHLANDS,
                    TOWER_KARST_HIGHLANDS,
                    DOLINE_HIGHLANDS,
                    CENOTE_HIGHLANDS
            );
            biome(
                    biomes,
                    "rolling_hills",
                    50, 100, 50,
                    ROLLING_HILLS,
                    DOLINE_ROLLING_HILLS,
                    CENOTE_ROLLING_HILLS
            );
            biome(
                    biomes,
                    "hills",
                    80, 130, 80,
                    HILLS,
                    SHILIN_HILLS,
                    TOWER_KARST_HILLS,
                    DOLINE_HILLS,
                    CENOTE_HILLS
            );
            biome(
                    biomes,
                    "grassy_dunes",
                    90, 165, 90,
                    GRASSY_DUNES
            );
            biome(
                    biomes,
                    "plains",
                    100, 200, 100,
                    PLAINS,
                    BURREN_PLAINS,
                    TOWER_KARST_PLAINS,
                    DOLINE_PLAINS,
                    CENOTE_PLAINS,
                    SHILIN_PLAINS
            );
            biome(
                    biomes,
                    "badlands",
                    205, 160, 50,
                    BADLANDS,
                    BURREN_BADLANDS,
                    BURREN_BADLANDS_TALL
            );
            biome(
                    biomes,
                    "stair_step_canyons",
                    250, 190, 0,
                    STAIR_STEP_CANYONS
            );
            biome(
                    biomes,
                    "hoodoos",
                    230, 180, 0,
                    HOODOOS
            );
            biome(
                    biomes,
                    "mesas",
                    150, 160, 0,
                    MESAS
            );
            biome(
                    biomes,
                    "buttes",
                    190, 160, 0,
                    BUTTES
            );
            biome(
                    biomes,
                    "whorled_canyons",
                    250, 215, 0,
                    WHORLED_CANYONS
            );
            biome(
                    biomes,
                    "rocky_plateau",
                    180, 160, 110,
                    ROCKY_PLATEAU
            );
            biome(
                    biomes,
                    "dune_sea",
                    250, 210, 140,
                    DUNE_SEA
            );
            biome(
                    biomes,
                    "salt_flats",
                    190, 190, 190,
                    SALT_FLATS
            );
            biome(
                    biomes,
                    "mud_flats",
                    190, 120, 100,
                    MUD_FLATS
            );
            biome(
                    biomes,
                    "shore",
                    230, 210, 130,
                    SHORE
            );
            biome(
                    biomes,
                    "guano_island",
                    120, 120, 50,
                    GUANO_ISLAND
            );
            biome(
                    biomes,
                    "active_shield_volcano",
                    255, 85, 0,
                    ACTIVE_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "dormant_shield_volcano",
                    255, 105, 0,
                    DORMANT_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "extinct_shield_volcano",
                    255, 135, 0,
                    EXTINCT_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "ancient_shield_volcano",
                    255, 155, 0,
                    ANCIENT_SHIELD_VOLCANO,
                    SUNKEN_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "tuyas",
                    115, 145, 115,
                    TUYAS
            );
            biome(
                    biomes,
                    "drumlins",
                    135, 165, 135,
                    DRUMLINS,
                    BURREN_ROCHE_MOUTONEE
            );
            biome(
                    biomes,
                    "knob_and_kettle",
                    115, 115, 115,
                    KNOB_AND_KETTLE
            );
            biome(
                    biomes,
                    "patterned_ground",
                    135, 135, 135,
                    PATTERNED_GROUND,
                    INVERTED_PATTERNED_GROUND,
                    STONE_CIRCLES
            );
            biome(
                    biomes,
                    "ice_sheet_edge",
                    165, 165, 165,
                    ICE_SHEET_EDGE,
                    ICE_SHEET_SHORE
            );
            biome(
                    biomes,
                    "ice_sheet",
                    255, 255, 255,
                    ICE_SHEET,
                    SUBGLACIAL_LAKE
            );
            biome(
                    biomes,
                    "ice_sheet_oceanic",
                    215, 215, 215,
                    ICE_SHEET_OCEANIC
            );
            biome(
                    biomes,
                    "ice_sheet_tuyas",
                    235, 235, 235,
                    ICE_SHEET_TUYAS,
                    ICE_SHEET_TUYAS_EDGE
            );
            biome(
                    biomes,
                    "ice_sheet_mountains",
                    255, 195, 195,
                    ICE_SHEET_MOUNTAINS,
                    ICE_SHEET_MOUNTAINS_EDGE
            );
            biome(
                    biomes,
                    "ice_sheet_oceanic_mountains",
                    255, 195, 255,
                    ICE_SHEET_OCEANIC_MOUNTAINS,
                    ICE_SHEET_OCEANIC_MOUNTAINS_EDGE
            );
            biome(
                    biomes,
                    "ice_sheet_shield_volcano",
                    255, 215, 185,
                    ICE_SHEET_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "glaciated_mountains",
                    255, 165, 165,
                    GLACIATED_MOUNTAINS
            );
            biome(
                    biomes,
                    "glaciated_oceanic_mountains",
                    255, 165, 255,
                    GLACIATED_OCEANIC_MOUNTAINS
            );
            biome(
                    biomes,
                    "glaciated_shield_volcano",
                    255, 185, 125,
                    GLACIATED_SHIELD_VOLCANO
            );
            biome(
                    biomes,
                    "glacially_carved_mountains",
                    255, 135, 135,
                    GLACIALLY_CARVED_MOUNTAINS
            );
            biome(
                    biomes,
                    "glacially_carved_oceanic_mountains",
                    255, 135, 255,
                    GLACIALLY_CARVED_OCEANIC_MOUNTAINS
            );
            verifyFuzzyUniqueness(biomeColors);
        });
    }

    private static void feature(Provider<RegistryLinkedColor<PlacedFeature>> provider, String name, ColorDefinition color, ResourceLocation... ids) {
        provider.accept(name, new RegistryLinkedColor<>(
                keys(Registries.PLACED_FEATURE, ids),
                color
        ));
    }

    private static void biome(Provider<RegistryLinkedColor<Biome>> provider, String name, int r, int g, int b, int... biomes) {
        biome(provider, name, color(r, g, b, "biome.tfc." + name, sort()), biomes);
    }

    private static void biome(Provider<RegistryLinkedColor<Biome>> provider, String name, ColorDefinition color, int... biomes) {
        provider.accept(Helpers.identifier(name), biome(color, biomes));
        verifyUniqueColor(color.color(), biomeColors);
    }

    private static RegistryLinkedColor<Biome> biome(ColorDefinition color, int... biomes) {
        return new RegistryLinkedColor<>(
                Arrays.stream(biomes)
                        .mapToObj(TFCLayers::getFromLayerId)
                        .map(BiomeExtension::key)
                        .toList(),
                color
        );
    }

    private static <T> List<ResourceKey<T>> keys(ResourceKey<? extends Registry<T>> reg, ResourceLocation... ids) {
        return Arrays.stream(ids)
                .map(i -> ResourceKey.create(reg, i))
                .toList();
    }

    private static ColorDefinition color(int r, int g, int b, String key, int sort) {
        return ColorDefinition.of(r, g, b, Component.translatable(key), sort);
    }

    private static int s = 1;

    private static int sort() {
        return s += 10;
    }

    private static final IntList biomeColors = new IntArrayList();
    private static void verifyUniqueColor(RGB rgb, IntList colors) {
        final int color = rgb.abgr();
        if (colors.contains(color)) {
            throw new IllegalArgumentException("Duplicate color for %s".formatted(rgb));
        }
        colors.add(color);
    }

    private static void verifyFuzzyUniqueness(IntList colors) {
        final int minDifference = 10;
        final IntList[] tooSimilar = new IntList[colors.size()];
        for (int i = 0 ; i < colors.size() ; i++) {
            final int color = colors.getInt(i);
            final int j = i;
            colors.intStream().forEach(o -> {
                // Should already be handled by #verifyUniqueColor
                if (o != color) {
                    final double difference = Math.sqrt(
                            Math.pow(FastColor.ABGR32.red(color) - FastColor.ABGR32.red(o), 2) +
                                    Math.pow(FastColor.ABGR32.green(color) - FastColor.ABGR32.green(o), 2) +
                                    Math.pow(FastColor.ABGR32.blue(color) - FastColor.ABGR32.blue(o), 2)
                    );
                    if (difference < minDifference) {
                        if (tooSimilar[j] == null) {
                            tooSimilar[j] = new IntArrayList();
                        }
                        tooSimilar[j].add(o);
                    }
                }
            });
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i < colors.size() ; i++) {
            final IntList similar = tooSimilar[i];
            if (similar != null) {
                builder.append("\n")
                        .append(Integer.toHexString(colors.getInt(i)))
                        .append(" is similar to ")
                        .append(similar.intStream().mapToObj(Integer::toHexString).collect(Collectors.joining(",")));
            }
        }
        if (!builder.isEmpty()) {
            throw new IllegalStateException(builder.toString());
        }
    }
}
