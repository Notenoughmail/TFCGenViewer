package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.*;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScaleGroup;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import net.dries007.tfc.world.region.Units;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class TFCVisualizer implements ITFCGeneratorVisualizer<TFCVisualizer.ScaleGroup, ITFCVisualizer<?>> {

    public static final TFCVisualizer INSTANCE = new TFCVisualizer();

    private static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld");

    private TFCVisualizer() {}

    @Override
    public List<ITFCVisualizer<?>> allVisualizers() {
        return GenViewerAPI.TFC_VISUALIZER_REGISTRY.stream().toList();
    }

    @Override
    public ScaleGroup scaleGroup() {
        return ScaleGroup.INSTANCE;
    }

    @Override
    public List<? extends ITFCVisualizer<?>> allowedVisualizers(ServerPlayer player) {
        return GenViewerAPI.TFC_VISUALIZER_REGISTRY.stream().filter(v -> v.isPermitted(player)).toList();
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public boolean supportsRockEditing() {
        return true;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, List<ITFCVisualizer<?>>> visualizerCodec() {
        return ITFCVisualizer.CODEC;
    }

    public enum ScaleGroup implements IScaleGroup<GridScale> {
        INSTANCE;

        @Override
        public int blocksPerPixel() {
            return Units.GRID_WIDTH_IN_BLOCK;
        }

        @Override
        public Component formatScale(GridScale scale) {
            return scale.display();
        }

        @Override
        public List<GridScale> scales() {
            return GridScale.SCALES;
        }

        @Override
        public Codec<GridScale> codec() {
            return GridScale.CODEC;
        }
    }
}
