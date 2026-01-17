package io.github.notenoughmail.tfcgenviewer.color;

import io.github.notenoughmail.tfcgenviewer.DataManagerProvider;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorWithInstancesManager;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.impl.ClimateFeatureCache;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.dries007.tfc.world.layer.TFCLayers.*;

public class ColorWithInstancesProvider extends DataManagerProvider {

    public ColorWithInstancesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, "Colors with instances");
    }

    @Override
    protected void make(HolderLookup.Provider l) {

        makeFor(ClimateFeatureCache.FEATURES, features -> {
            featureInst(
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
            featureInst(
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
            biomes.accept("unknown", new ColorWithInstancesManager.ColorWithInstances<>(
                    List.of(),
                    ColorDefinition.of(
                            0, 0, 0,
                            Component.translatable("biome.tfcgenviewer.unknown"),
                            null
                    )
            ));
            // Mostly based on colors & biomes of RegionGeneratorTests, but some merging has occurred
            // TODO: 1.21.1 | Tweak unions
            biomeInst(
                    biomes,
                    "ocean",
                    0, 0, 220,
                    "biome.tfc.ocean",
                    OCEAN
            );
            biomeInst(
                    biomes,
                    "ocean_reef",
                    70, 160, 250,
                    "biome.tfc.ocean_reef",
                    OCEAN_REEF
            );
            biomeInst(
                    biomes,
                    "deep_ocean",
                    70, 160, 250,
                    "biome.tfc.deep_ocean",
                    DEEP_OCEAN
            );
            biomeInst(
                    biomes,
                    "deep_ocean_trench",
                    0, 0, 80,
                    "biome.tfc.deep_ocean_trench",
                    DEEP_OCEAN_TRENCH
            );
            biomeInst(
                    biomes,
                    "lake",
                    30, 30, 255,
                    "biome.tfc.lake",
                    LAKE,
                    MELTWATER_LAKE
            );
            biomeInst(
                    biomes,
                    "mountain_lake",
                    20, 180, 255,
                    "biome.tfc.mountain_lake",
                    MOUNTAIN_LAKE,
                    OCEANIC_MOUNTAIN_LAKE,
                    OLD_MOUNTAIN_LAKE,
                    VOLCANIC_MOUNTAIN_LAKE,
                    PLATEAU_LAKE,
                    VOLCANIC_OCEANIC_MOUNTAIN_LAKE
            );
            biomeInst(
                    biomes,
                    "river",
                    0, 200, 255,
                    "biome.tfc.river",
                    RIVER
            );
            biomeInst(
                    biomes,
                    "oceanic_mountains",
                    255, 0, 255,
                    "biome.tfc.oceanic_mountains",
                    OCEANIC_MOUNTAINS,
                    VOLCANIC_OCEANIC_MOUNTAINS
            );
            biomeInst(
                    biomes,
                    "canyons",
                    180, 60, 255,
                    "biome.tfc.canyons",
                    CANYONS,
                    TOWER_KARST_CANYONS,
                    SHILIN_CANYONS,
                    DOLINE_CANYONS,
                    CENOTE_CANYONS,
                    LOW_CANYONS,
                    WHORLED_CANYONS,
                    STAIR_STEP_CANYONS
            );
            biomeInst(
                    biomes,
                    "lowlands",
                    220, 150, 230,
                    "biome.tfc.lowlands",
                    LOWLANDS,
                    TOWER_KARST_BAY,
                    SALT_MARSH,
                    TOWER_KARST_LAKE
            );
            biomeInst(
                    biomes,
                    "mountains",
                    255, 50, 50,
                    "biome.tfc.mountains",
                    MOUNTAINS,
                    VOLCANIC_MOUNTAINS,
                    OLD_MOUNTAINS,
                    EXTREME_DOLINE_MOUNTAINS
            );
            biomeInst(
                    biomes,
                    "plateau",
                    190, 120, 120,
                    "biome.tfc.plateau",
                    PLATEAU,
                    EXTREME_DOLINE_PLATEAU,
                    CENOTE_PLATEAU,
                    DOLINE_PLATEAU,
                    SHILIN_PLATEAU,
                    BURREN_PLATEAU,
                    ROCKY_PLATEAU,
                    PLATEAU_WIDE
            );
            biomeInst(
                    biomes,
                    "badlands",
                    205, 160, 50,
                    "biome.tfc.badlands",
                    BADLANDS,
                    BURREN_BADLANDS,
                    BURREN_BADLANDS_TALL
            );
            biomeInst(
                    biomes,
                    "mesas",
                    150, 160, 0,
                    "biome.tfc.mesas",
                    MESAS,
                    HOODOOS,
                    BUTTES
            );
            biomeInst(
                    biomes,
                    "dunes",
                    250, 210, 140,
                    "biome.tfc.dune_sea",
                    DUNE_SEA,
                    GRASSY_DUNES
            );
            biomeInst(
                    biomes,
                    "salt_flats",
                    190, 190, 190,
                    "biome.tfc.slat_flats",
                    SALT_FLATS,
                    GUANO_ISLAND
            );
            biomeInst(
                    biomes,
                    "mud_flats",
                    190, 120, 100,
                    "biome.tfc.mud_flats",
                    MUD_FLATS
            );
            biomeInst(
                    biomes,
                    "shore",
                    230, 210, 130,
                    "biome.tfc.shore",
                    SHORE
            );
            biomeInst(
                    biomes,
                    "highlands",
                    20, 80, 30,
                    "biome.tfc.highlands",
                    HIGHLANDS,
                    SHILIN_HIGHLANDS,
                    TOWER_KARST_HIGHLANDS,
                    DOLINE_HIGHLANDS,
                    CENOTE_HIGHLANDS
            );
            biomeInst(
                    biomes,
                    "rolling_hills",
                    50, 100, 50,
                    "biome.tfc.rolling_hills",
                    ROLLING_HILLS,
                    DOLINE_ROLLING_HILLS,
                    CENOTE_ROLLING_HILLS
            );
            biomeInst(
                    biomes,
                    "hills",
                    80, 130, 80,
                    "biome.tfc.hills",
                    HILLS,
                    SHILIN_HILLS,
                    TOWER_KARST_HILLS,
                    DOLINE_HILLS,
                    CENOTE_HILLS
            );
            biomeInst(
                    biomes,
                    "plains",
                    100, 200, 100,
                    "biome.tfc.plains",
                    PLAINS,
                    BURREN_PLAINS,
                    TOWER_KARST_PLAINS,
                    DOLINE_PLAINS,
                    CENOTE_PLAINS,
                    SHILIN_PLAINS
            );
            biomeInst(
                    biomes,
                    "active_volcano",
                    255, 85, 0,
                    "biome.tfc.active_shield_volcano",
                    ACTIVE_SHIELD_VOLCANO
            );
            biomeInst(
                    biomes,
                    "dormant_volcano",
                    255, 105, 0,
                    "biome.tfc.dormant_shield_volcano",
                    DORMANT_SHIELD_VOLCANO
            );
            biomeInst(
                    biomes,
                    "extinct_volcano",
                    255, 135, 0,
                    "biome.tfc.extinct_volcano",
                    EXTINCT_SHIELD_VOLCANO
            );
            biomeInst(
                    biomes,
                    "ancient_volcano",
                    255, 155, 0,
                    "biome.tfc.ancient_shield_volcano",
                    ANCIENT_SHIELD_VOLCANO,
                    SUNKEN_SHIELD_VOLCANO
            );
            biomeInst(
                    biomes,
                    "ice_sheet",
                    255, 255, 255,
                    "biome.tfc.ice_sheet",
                    ICE_SHEET,
                    SUBGLACIAL_LAKE,
                    ICE_SHEET_OCEANIC,
                    ICE_SHEET_TUYAS
            );
            biomeInst(
                    biomes,
                    "ice_sheet_edge",
                    255, 195, 255,
                    "biome.tfc.ice_sheet_edge",
                    ICE_SHEET_EDGE,
                    ICE_SHEET_SHORE
            );
            biomeInst(
                    biomes,
                    "ice_sheet_mountains",
                    255, 195, 195,
                    "biome.tfc.ice_sheet_mountains",
                    ICE_SHEET_MOUNTAINS,
                    ICE_SHEET_MOUNTAINS_EDGE,
                    ICE_SHEET_OCEANIC_MOUNTAINS,
                    ICE_SHEET_OCEANIC_MOUNTAINS_EDGE,
                    ICE_SHEET_SHIELD_VOLCANO
            );
            biomeInst(
                    biomes,
                    "glaciated_mountains",
                    255, 165, 255,
                    "biome.tfc.glaciated_mountains",
                    GLACIATED_MOUNTAINS,
                    GLACIATED_OCEANIC_MOUNTAINS,
                    GLACIATED_SHIELD_VOLCANO,
                    GLACIALLY_CARVED_MOUNTAINS,
                    GLACIALLY_CARVED_OCEANIC_MOUNTAINS
            );
            biomeInst(
                    biomes,
                    "patterned_ground",
                    115, 145, 115,
                    "biome.tfc.patterned_ground",
                    PATTERNED_GROUND,
                    INVERTED_PATTERNED_GROUND,
                    STONE_CIRCLES
            );
            biomeInst(
                    biomes,
                    "moutonee",
                    135, 165, 135,
                    "biome.tfc.burren_roche_moutonee",
                    // Really not the same...
                    BURREN_ROCHE_MOUTONEE,
                    DRUMLINS,
                    TUYAS,
                    KNOB_AND_KETTLE
            );
        });
    }

    private static void featureInst(Provider<ColorWithInstancesManager.ColorWithInstances<PlacedFeature>> provider, String name, ColorDefinition color, ResourceLocation... ids) {
        provider.accept(name, new ColorWithInstancesManager.ColorWithInstances<>(
                instances(Registries.PLACED_FEATURE, ids),
                color
        ));
    }

    private static void biomeInst(Provider<ColorWithInstancesManager.ColorWithInstances<Biome>> provider, String name, int r, int g, int b, String key, int... biomes) {
        provider.accept(Helpers.identifier(name), biomeInst(ColorDefinition.of(
                r, g, b,
                Component.translatable(key),
                null
        ), biomes));
    }

    private static ColorWithInstancesManager.ColorWithInstances<Biome> biomeInst(ColorDefinition color, int... biomes) {
        return new ColorWithInstancesManager.ColorWithInstances<>(
                Arrays.stream(biomes)
                        .mapToObj(TFCLayers::getFromLayerId)
                        .map(BiomeExtension::key)
                        .toList(),
                color
        );
    }

    static <T> List<ResourceKey<T>> instances(ResourceKey<? extends Registry<T>> reg, ResourceLocation... ids) {
        return Arrays.stream(ids)
                .map(i -> ResourceKey.create(reg, i))
                .toList();
    }
}
