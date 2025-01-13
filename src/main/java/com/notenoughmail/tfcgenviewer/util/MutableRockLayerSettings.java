package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MutableRockLayerSettings {

    public static MutableRockLayerSettings init(RockLayerSettings settings) {
        final MutableRockLayerSettings builder = new MutableRockLayerSettings();
        final RockLayerSettings.Data data = ((RockLayerSettingsAccessor) (Object) settings).tfcgenviewer$GetData();
        data.rocks().forEach((name, rock) -> builder.rocks.put(name, new MutableRockSettings(rock)));
        builder.bottom.addAll(data.bottom());
        data.layers().forEach(layer -> builder.layers.add(new MutableLayerData(layer)));
        builder.ocean.addAll(data.oceanFloor());
        builder.land.addAll(data.land());
        builder.volcanic.addAll(data.volcanic());
        builder.uplift.addAll(data.uplift());
        return builder;
    }

    public RockLayerSettings.Data build() {
        return new RockLayerSettings.Data(
                Util.make(new HashMap<>(), m -> rocks.forEach((n, mrs) -> m.put(n, mrs.build()))),
                bottom,
                layers.stream().map(MutableLayerData::build).toList(),
                ocean,
                land,
                volcanic,
                uplift
        );
    }

    public final Map<String, MutableRockSettings> rocks = new HashMap<>();
    public final List<String>
            bottom = new ArrayList<>(),
            ocean = new ArrayList<>(),
            land = new ArrayList<>(),
            volcanic = new ArrayList<>(),
            uplift = new ArrayList<>();
    private final List<MutableLayerData> layers = new ArrayList<>();

    public static class MutableRockSettings {

        public Block
                raw,
                hardened,
                gravel,
                cobble,
                sand,
                sandstone;
        @Nullable
        public Block
                spike,
                loose,
                mossyLoose;

        private MutableRockSettings(RockSettings init) {
            raw = init.raw();
            hardened = init.hardened();
            gravel = init.gravel();
            cobble = init.cobble();
            sand = init.sand();
            sandstone = init.sandstone();
            spike = init.spike().orElse(null);
            loose = init.loose().orElse(null);
            mossyLoose = init.mossyLoose().orElse(null);
        }

        public MutableRockSettings() {
            raw = hardened = gravel = cobble = sand = sandstone = Blocks.STONE;
        }

        private RockSettings build() {
            return new RockSettings(
                    raw,
                    hardened,
                    gravel,
                    cobble,
                    sand,
                    sandstone,
                    Optional.ofNullable(spike),
                    Optional.ofNullable(loose),
                    Optional.ofNullable(mossyLoose)
            );
        }
    }

    private static class MutableLayerData {

        public final String id;
        public final Map<String, String> mapping;

        private MutableLayerData(RockLayerSettings.LayerData init) {
            id = init.id();
            mapping = new HashMap<>();
            mapping.putAll(init.layers());
        }

        private RockLayerSettings.LayerData build() {
            return new RockLayerSettings.LayerData(id, mapping);
        }
    }
}
