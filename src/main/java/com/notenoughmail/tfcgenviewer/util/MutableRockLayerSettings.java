package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.mixin.RockLayerSettingsAccessor;
import com.notenoughmail.tfcgenviewer.util.custom.rock.LayerType;
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
        builder.layers.get(LayerType.BOTTOM).addAll(data.bottom());
        data.layers().forEach(layer -> builder.layerDefs.put(layer.id(), new MutableLayerData(layer)));
        builder.layers.get(LayerType.OCEAN).addAll(data.oceanFloor());
        builder.layers.get(LayerType.LAND).addAll(data.land());
        builder.layers.get(LayerType.VOLCANIC).addAll(data.volcanic());
        builder.layers.get(LayerType.UPLIFT).addAll(data.uplift());
        return builder;
    }

    public RockLayerSettings.Data build() {
        return new RockLayerSettings.Data(
                Util.make(new HashMap<>(), m -> rocks.forEach((n, mrs) -> m.put(n, mrs.build()))),
                layers.get(LayerType.BOTTOM),
                buildLayerDefs(),
                layers.get(LayerType.OCEAN),
                layers.get(LayerType.LAND),
                layers.get(LayerType.VOLCANIC),
                layers.get(LayerType.UPLIFT)
        );
    }

    private List<RockLayerSettings.LayerData> buildLayerDefs() {
        final List<RockLayerSettings.LayerData> layers = new ArrayList<>();
        layerDefs.forEach((id, layer) -> layers.add(layer.build()));
        return layers;
    }

    public final Map<String, MutableRockSettings> rocks = new HashMap<>();
    public final Map<LayerType, List<String>> layers = Util.make(new EnumMap<>(LayerType.class), m -> {
        m.put(LayerType.BOTTOM, new ArrayList<>());
        m.put(LayerType.OCEAN, new ArrayList<>());
        m.put(LayerType.LAND, new ArrayList<>());
        m.put(LayerType.VOLCANIC, new ArrayList<>());
        m.put(LayerType.UPLIFT, new ArrayList<>());
    });
    public final OrderedMap<String, MutableLayerData> layerDefs = new OrderedMapImpl<>();

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

        public void clear() {
            raw = hardened = gravel = cobble = sand = sandstone = Blocks.STONE;
            spike = loose = mossyLoose = null;
        }
    }

    public static class MutableLayerData {

        public String id;
        public final Map<String, String> mapping;

        private MutableLayerData(RockLayerSettings.LayerData init) {
            id = init.id();
            mapping = new HashMap<>();
            mapping.putAll(init.layers());
        }

        public MutableLayerData(String id) {
            this.id = id;
            mapping = new HashMap<>();
        }

        private RockLayerSettings.LayerData build() {
            return new RockLayerSettings.LayerData(id, mapping);
        }

        public boolean mapsTo(String layer) {
            return mapping.containsValue(layer);
        }
    }
}
