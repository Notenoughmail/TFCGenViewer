package io.github.notenoughmail.tfcgenviewer.color;

import io.github.notenoughmail.tfcgenviewer.DataManagerProvider;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorDefinition;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorManager;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.color.RGB;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.KoppenClimateClassification;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static net.dries007.tfc.util.climate.KoppenClimateClassification.*;
import static net.dries007.tfc.common.blocks.rock.Rock.*;

public class ColorProvider extends DataManagerProvider<ColorDefinition> {

    private static final Map<Rock, RGB> ROCK_COLORS = Util.make(new EnumMap<>(Rock.class), m -> {
        rgb(m, ANDESITE, 96, 96, 96);
        rgb(m, BASALT, 29, 32, 33);
        rgb(m, CHALK, 199, 199, 193);
        rgb(m, CHERT, 122, 78, 70);
        rgb(m, CLAYSTONE, 141, 102, 68);
        rgb(m, CONGLOMERATE, 111, 113, 101);
        rgb(m, DACITE, 122, 123, 123);
        rgb(m, DIORITE, 142, 142, 142);
        rgb(m, DOLOMITE, 60, 70, 89);
        rgb(m, GABBRO, 93, 85, 68);
        rgb(m, GNEISS, 115, 109, 96);
        rgb(m, GRANITE, 85, 70, 74);
        rgb(m, LIMESTONE, 136, 127, 107);
        rgb(m, MARBLE, 227, 235, 235);
        rgb(m, PHYLLITE, 148, 157, 169);
        rgb(m, QUARTZITE, 140, 129, 128);
        rgb(m, RHYOLITE, 115, 98, 103);
        rgb(m, SCHIST, 77, 84, 65);
        rgb(m, SHALE, 70, 67, 70);
        rgb(m, SLATE, 125, 116, 103);
    });
    public static String rockName(Rock rock) {
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
    public static String koppenName(KoppenClimateClassification koppen) {
        return "color." + TFCGenViewer.ID + ".koppen_classification." + koppen.getSerializedName();
    }

    public ColorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void make() {
        makeFor(Colors.BIOME_COLORS, biomes -> {

        });
        makeFor(
                Colors.ROCK_COLORS,
                ROCK_COLORS,
                r -> Helpers.identifier(r.getSerializedName()),
                ColorProvider::rockName,
                Enum::ordinal,
                t -> null,
                new RGB(
                        255,
                        88,
                        227
                ),
                "rock"
        );
        makeFor(
                Colors.KOPPEN_COLORS,
                KOPPEN_COLORS,
                k -> Colors.KOPPENS.get(k).id(),
                ColorProvider::koppenName,
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
        return "color." + TFCGenViewer.ID + type + ".unknown";
    }

    private <T> void makeFor(
            ColorManager manager,
            Map<T, RGB> map,
            Function<T, ResourceLocation> id,
            Function<T, String> name,
            ToIntFunction<T> sort,
            Function<T, @Nullable String> tooltip,
            RGB unknown,
            String type
    ) {
        makeFor(manager, m -> {
                    map.forEach((t, rgb) ->
                            m.accept(
                                    id.apply(t),
                                    new ColorDefinition(
                                            rgb,
                                            Component.translatable(name.apply(t)),
                                            sort.applyAsInt(t),
                                            Optional.ofNullable(tooltip.apply(t))
                                                    .map(Component::translatable)
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
}
