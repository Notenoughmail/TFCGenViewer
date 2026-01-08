package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridSize;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import net.dries007.tfc.world.region.Units;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class TFCRegionVisualizer implements ITFCGeneratorVisualizer<TFCRegionVisualizer.Scale, ITFCVisualizer<?>> {

    public static final TFCRegionVisualizer INSTANCE = new TFCRegionVisualizer();

    private static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld");

    private TFCRegionVisualizer() {}

    @Override
    public List<ITFCVisualizer<?>> allVisualizers() {
        return GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY.stream().toList();
    }

    @Override
    public Scale scaleGroup() {
        return Scale.INSTANCE;
    }

    @Override
    public List<? extends ITFCVisualizer<?>> allowedVisualizers(ServerPlayer player) {
        return GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY.stream().filter(v -> v.isPermitted(player)).toList();
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

    public enum Scale implements IScale<GridSize> {
        INSTANCE;

        @Override
        public int blocksPerPixel() {
            return Units.GRID_WIDTH_IN_BLOCK;
        }

        @Override
        public Component formatSize(GridSize size) {
            return size.display();
        }

        @Override
        public List<? extends GridSize> sizes() {
            return GridSize.SIZES;
        }

        @Override
        public Codec<GridSize> codec() {
            return GridSize.CODEC;
        }
    }
}
