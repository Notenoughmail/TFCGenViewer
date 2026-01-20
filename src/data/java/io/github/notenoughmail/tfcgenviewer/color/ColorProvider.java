package io.github.notenoughmail.tfcgenviewer.color;

import io.github.notenoughmail.tfcgenviewer.DataManagerProvider;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.RGB;
import io.github.notenoughmail.tfcgenviewer.api.color.manager.ColorManager;
import io.github.notenoughmail.tfcgenviewer.api.cache.ClimateFeatureCache;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Preview;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.BiomeAltitudeVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.region.RiversAndMountainsVisualizer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static net.dries007.tfc.common.blocks.rock.Rock.*;
import static net.dries007.tfc.util.climate.KoppenClimateClassification.*;

public class ColorProvider extends DataManagerProvider {

    static final Map<Rock, RockInfo> ROCK_COLORS = Util.make(new EnumMap<>(Rock.class), m -> {
        rock(m, ANDESITE, 96, 96, 96, 0, 0xBF, 0xFF);
        rock(m, BASALT, 29, 32, 33, 0x41, 0x69, 0xE1);
        rock(m, CHALK, 199, 199, 193, 0x7F, 0xFF, 0);
        rock(m, CHERT, 122, 78, 70, 0, 0, 0xCD);
        rock(m, CLAYSTONE, 141, 102, 68, 0, 0x80, 0x80);
        rock(m, CONGLOMERATE, 111, 113, 101, 0xFF, 0xA5, 0);
        rock(m, DACITE, 122, 123, 123, 0xFF, 0, 0xFF);
        rock(m, DIORITE, 142, 142, 142, 0x8B, 0, 0);
        rock(m, DOLOMITE, 60, 70, 89, 0xFF, 0xFF, 0);
        rock(m, GABBRO, 93, 85, 68, 0x80, 0x80, 0);
        rock(m, GNEISS, 115, 109, 96, 0xEE, 0x82, 0xEE);
        rock(m, GRANITE, 85, 70, 74, 0xD3, 0xD3, 0xD3);
        rock(m, LIMESTONE, 136, 127, 107, 0xFF, 0x45, 0);
        rock(m, MARBLE, 227, 235, 235, 0x7F, 0xFF, 0xD4);
        rock(m, PHYLLITE, 148, 157, 169, 0xFF, 0x14, 0x93);
        rock(m, QUARTZITE, 140, 129, 128, 0xDB, 0x70, 0x93);
        rock(m, RHYOLITE, 115, 98, 103, 0, 0xFF, 0x7F);
        rock(m, SCHIST, 77, 84, 65, 0xFF, 0xA0, 0x7A);
        rock(m, SHALE, 70, 67, 70, 0, 0x80, 0);
        rock(m, SLATE, 125, 116, 103, 0xF0, 0xE6, 0x8C);
    });

    public static String rockKey(Rock rock) {
        return "color.tfc.rock." + rock.getSerializedName();
    }
    private static final Map<KoppenClimateClassification, RGB> KOPPEN_COLORS = Util.make(new EnumMap<>(KoppenClimateClassification.class), m -> {
        rgb(m, AF, 0, 0, 220);
        rgb(m, AS, 0, 100, 240);
        rgb(m, AW, 0, 150, 220);
        rgb(m, AM, 40, 80, 200);
        rgb(m, BWH, 210, 0, 0);
        rgb(m, BSH, 210, 120, 0);
        rgb(m, BWK, 200, 80, 80);
        rgb(m, BSK, 200, 120, 60);
        rgb(m, CSA, 250, 250, 0);
        rgb(m, CSB, 180, 180, 0);
        rgb(m, CSC, 120, 120, 0);
        rgb(m, CWA, 100, 240, 130);
        rgb(m, CWB, 80, 210, 120);
        rgb(m, CWC, 70, 160, 110);
        rgb(m, CFA, 170, 240, 90);
        rgb(m, CFB, 140, 200, 80);
        rgb(m, CFC, 110, 170, 70);
        rgb(m, DSA, 190, 20, 190);
        rgb(m, DSB, 160, 20, 180);
        rgb(m, DSC, 130, 20, 170);
        rgb(m, DSD, 100, 20, 160);
        rgb(m, DFA, 40, 190, 190);
        rgb(m, DFB, 30, 170, 170);
        rgb(m, DFC, 20, 150, 140);
        rgb(m, DFD, 10, 130, 110);
        rgb(m, DWA, 80, 80, 220);
        rgb(m, DWB, 70, 70, 190);
        rgb(m, DWC, 60, 60, 160);
        rgb(m, DWD, 60, 60, 130);
        rgb(m, ET, 190, 190, 190);
        rgb(m, EF, 80, 80, 80);
    });

    public ColorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, "Colors");
    }

    @Override
    protected void make(HolderLookup.Provider lookup) {
        makeFor(Colors.ROCK_COLORS, colors -> {
            ROCK_COLORS.forEach((r, i) -> i.make(r, true, colors::accept));
            colors.accept(Colors.UNKNOWN, ColorDefinition.of(
                    227, 88, 255,
                    Component.translatable("color.tfcgenviewer.rock.unknown")
            ));
        });
        makeFor(Colors.MISC_COLORS, colors -> {
            colors.accept(ClimateFeatureCache.LAND, ColorDefinition.of(
                    32, 168, 72,
                    Component.translatable("tfcgenviewer.climate_features.land")
            ));
            colors.accept(RiversAndMountainsVisualizer.RIVER, ColorDefinition.of(
                    100, 180, 250,
                    Component.translatable("biome.tfc.river")
            ));
            colors.accept(RiversAndMountainsVisualizer.COASTAL_MOUNTAIN, ColorDefinition.of(
                    150, 240, 150,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.coastal_mountain")
            ));
            colors.accept(RiversAndMountainsVisualizer.INLAND_MOUNTAIN, ColorDefinition.of(
                    150, 150, 150,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.inland_mountain")
            ));
            colors.accept(RiversAndMountainsVisualizer.HOT_SPOT_AGE_4, ColorDefinition.of(
                    190, 180, 0,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.hot_spot_age_4")
            ));
            colors.accept(RiversAndMountainsVisualizer.HOT_SPOT_AGE_3, ColorDefinition.of(
                    220, 110, 0,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.hot_spot_age_3")
            ));
            colors.accept(RiversAndMountainsVisualizer.HOT_SPOT_AGE_2, ColorDefinition.of(
                    240, 20, 0,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.hot_spot_age_2")
            ));
            colors.accept(RiversAndMountainsVisualizer.HOT_SPOT_AGE_1, ColorDefinition.of(
                    240, 0, 180,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.hot_spot_age_1")
            ));
            colors.accept(RiversAndMountainsVisualizer.LAND, ColorDefinition.of(
                    0, 148, 0,
                    Component.translatable("color.tfcgenviewer.rivers_and_mountains.land")
            ));
            colors.accept(BiomeAltitudeVisualizer.MOUNTAIN, ColorDefinition.of(
                    80, 200, 80,
                    Component.translatable("color.tfcgenviewer.biome_altitude.mountain")
            ));
            colors.accept(BiomeAltitudeVisualizer.HIGH, ColorDefinition.of(
                    53, 166, 53,
                    Component.translatable("color.tfcgenviewer.biome_altitude.high")
            ));
            colors.accept(BiomeAltitudeVisualizer.MID, ColorDefinition.of(
                    26, 133, 26,
                    Component.translatable("color.tfcgenviewer.biome_altitude.mid")
            ));
            colors.accept(BiomeAltitudeVisualizer.LOW, ColorDefinition.of(
                    0, 100, 0,
                    Component.translatable("color.tfcgenviewer.biome_altitude.low")
            ));
            colors.accept(BiomeAltitudeVisualizer.SHALLOW, ColorDefinition.of(
                    150, 160, 255,
                    Component.translatable("color.tfcgenviewer.biome_altitude.shallow")

            ));
            colors.accept(BiomeAltitudeVisualizer.DEEP, ColorDefinition.of(
                    120, 120, 240,
                    Component.translatable("color.tfcgenviewer.biome_altitude.deep")
            ));
            colors.accept(BiomeAltitudeVisualizer.VERY_DEEP, ColorDefinition.of(
                    100, 100, 200,
                    Component.translatable("color.tfcgenviewer.biome_altitude.very_deep")
            ));
            colors.accept(Preview.SPAWN_BORDER, ColorDefinition.of(
                    50, 50, 50,
                    Component.translatable("color.tfcgenviewer.spawn.border")
            ));
            colors.accept(Preview.SPAWN_RETICULE, ColorDefinition.of(
                    198, 15, 48,
                    Component.translatable("color.tfcgenviewer.spawn.reticule")
            ));
        });
        makeForFull(
                Colors.KOPPEN_COLORS,
                KOPPEN_COLORS,
                k -> Colors.KOPPEN_CLASSIFICATIONS.get(k).id(),
                Helpers::translateEnum,
                Enum::ordinal,
                t -> null,
                new RGB(
                        0,
                        0,
                        0
                ),
                "koppen_classification"
        );
    }

    public static String unknownKey(String type) {
        return "color." + TFCGenViewer.ID + "." + type + ".unknown";
    }

    private <T> void makeForFull(
            ColorManager manager,
            Map<T, RGB> map,
            Function<T, ResourceLocation> id,
            Function<T, Component> name,
            ToIntFunction<T> sort,
            Function<T, @Nullable Component> tooltip,
            RGB unknown,
            String type
    ) {
        makeFor(manager, m -> {
                    map.forEach((t, rgb) ->
                            m.accept(
                                    id.apply(t),
                                    new ColorDefinition(
                                            rgb,
                                            name.apply(t),
                                            sort.applyAsInt(t),
                                            Optional.ofNullable(tooltip.apply(t))
                                    )
                            )
                    );
                    final String unknownKey = unknownKey(type);
                    m.accept(
                            "unknown",
                            new ColorDefinition(
                                    unknown,
                                    Component.translatable(unknownKey),
                                    100,
                                    Optional.empty()
                            )
                    );
                }
        );
    }

    private static <T> void rgb(Map<T, RGB> map, T t, int r, int g, int b) {
        map.put(t, new RGB(r, g, b));
    }

    private static void rock(Map<Rock, RockInfo> map, Rock rock, int rs, int gs, int bs, int rm, int gm, int bm) {
        map.put(rock, new RockInfo(
                new RGB(rs, gs, bs),
                new RGB(rm, gm, bm)
        ));
    }

    public record RockInfo(RGB standard, RGB seedMaker) {
        public void make(Rock rock, boolean standard, BiConsumer<ResourceLocation, ColorDefinition> ret) {
            final ColorDefinition color = new ColorDefinition(
                    standard ? this.standard : seedMaker,
                    Component.translatable(ColorProvider.rockKey(rock)),
                    rock.ordinal(),
                    Optional.empty()
            );
            final ResourceLocation id = TFCBlocks.ROCK_BLOCKS.get(rock).get(BlockType.RAW).getId();
            ret.accept(id, color);
        }
    }
}
