package com.notenoughmail.tfcgenviewer.util;

import com.mojang.serialization.Codec;
import com.notenoughmail.tfcgenviewer.color.ColorDefinition;
import com.notenoughmail.tfcgenviewer.color.FeatureColors;
import io.github.notenoughmail.tfcgenviewer.api.MutableImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.region.Region;
import net.minecraft.client.OptionInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.notenoughmail.tfcgenviewer.color.Colors.*;
import static com.notenoughmail.tfcgenviewer.color.FeatureColors.Features;
import static com.notenoughmail.tfcgenviewer.util.ColorUtil.*;
import static com.notenoughmail.tfcgenviewer.util.Permissions.*;

public enum VisualizerType implements IExtensibleEnum {
    ;

    public static final VisualizerType[] VALUES = values();
    public static final Codec<VisualizerType> CODEC = Codec.intRange(0, VALUES.length - 1).xmap(b -> VALUES[b], Enum::ordinal);

    private final byte permission;
    private final Component name;
    private final DrawFunction drawer;
    private final Function<RegistryAccess, Component> colorKey;

    VisualizerType(int permission, String name, DrawFunction drawer, Function<RegistryAccess, Component> colorKey) {
        this.permission = (byte) permission;
        this.name = Component.translatable("tfcgenviewer.preview_world.visualizer_type." + name);
        this.drawer = drawer;
        this.colorKey = colorKey;
    }

    public static OptionInstance<VisualizerType> option(List<VisualizerType> visualizers) {
        return new OptionInstance<>(
                "tfcgenviewer.preview_world.visualizer_type",
                OptionInstance.noTooltip(),
                (caption, task) -> task.getName(),
                new OptionInstance.Enum<>(visualizers, VisualizerType.CODEC),
                visualizers.contains(VisualizerType.RIVERS) ? VisualizerType.RIVERS : visualizers.get(0),
                task -> {}
        );
    }

    public static List<VisualizerType> getVisualizers(byte permission) {
        final List<VisualizerType> visualizers = new ArrayList<>();
        for (VisualizerType type : VALUES) {
            if ((type.permission & permission) != 0) visualizers.add(type);
        }
        if (!FMLEnvironment.production) {
            visualizers.add(valueOf("DEV"));
            visualizers.add(valueOf("BORDER"));
            visualizers.add(valueOf("RIVER_EDGES"));
        }
        return visualizers;
    }

    public Component getName() {
        return name;
    }

    public Component getColorKey(RegistryAccess registryAccess) {
        return colorKey.apply(registryAccess);
    }

    public void draw(
            int x,
            int y,
            int xPos,
            int zPos,
            RegionChunkDataGenerator generator,
            Region region,
            Region.Point point,
            MutableImage image,
            Int2ObjectOpenHashMap<Component> colorDescriptors,
            RegistryAccess registryAccess
    ) {
        drawer.draw(
                x,
                y,
                xPos,
                zPos,
                generator,
                region,
                point,
                image,
                colorDescriptors,
                registryAccess
        );
    }

    static VisualizerType create(String title, int permission, String name, DrawFunction drawer, Function<RegistryAccess, Component> colorKey) {
        throw new IllegalStateException("VisualizerType not extended");
    }

    @FunctionalInterface
    public interface DrawFunction {
        void draw(
                int x,
                int y,
                int xPos,
                int zPos,
                RegionChunkDataGenerator generator,
                Region region,
                Region.Point point,
                MutableImage image,
                Int2ObjectOpenHashMap<Component> colorDescriptors,
                RegistryAccess registryAccess
        );
    }
}
