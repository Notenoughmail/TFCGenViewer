package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridSize;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;

public class TFCRegionVisualizer implements IGeneratorVisualizer<TFCChunkGenerator, GridSize, TFCRegionVisualizer.Scale, IRegionVisualizerType<?, ?>> {

    public static final TFCRegionVisualizer INSTANCE = new TFCRegionVisualizer();

    static final StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> GENERATOR_NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BiomeSource.CODEC.xmap(BiomeSourceExtension.class::cast, BiomeSourceExtension::self)), g -> g.customBiomeSource,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(NoiseGeneratorSettings.CODEC), g -> g.noiseSettings,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Settings.CODEC.codec()), TFCChunkGenerator::settings,
            TFCChunkGenerator::new
    );

    public static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld.region");

    public static final ResourceLocation ID = TFCGenViewer.id("tfc_region");

    private TFCRegionVisualizer() {}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public List<IRegionVisualizerType<?, ?>> allVisualizers() {
        return GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY.stream().toList();
    }

    @Override
    public Scale scale() {
        return Scale.INSTANCE;
    }

    @Override
    public int maximumPreviewOffset() {
        return GridSize._6.sizeInPixels() * 2;
    }

    @Override
    public Component name() {
        return NAME;
    }

    @Override
    public Class<TFCChunkGenerator> generatorType() {
        return TFCChunkGenerator.class;
    }

    @Override
    public boolean supportsRockEditing() {
        return true;
    }

    @Override
    public TFCChunkGenerator recreateGenerator(TFCChunkGenerator generator) {
        return new TFCChunkGenerator(generator.customBiomeSource, generator.noiseSettings, generator.settings());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> generatorNetworkCodec() {
        return GENERATOR_NETWORK_CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, List<IRegionVisualizerType<?, ?>>> visualizerNetworkCodec() {
        return IRegionVisualizerType.NETWORK_CODEC;
    }

    @Override
    public Codec<IRegionVisualizerType<?, ?>> visualizerCodec() {
        return IRegionVisualizerType.CODEC;
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
        public GridSize getDefault() {
            return GridSize._3;
        }


        @Override
        public List<GridSize> sizes() {
            return GridSize.SIZES;
        }

        @Override
        public Codec<GridSize> codec() {
            return GridSize.CODEC;
        }
    }
}
