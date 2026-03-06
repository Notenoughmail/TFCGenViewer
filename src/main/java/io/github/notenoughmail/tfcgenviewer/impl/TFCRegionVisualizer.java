package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.registry.Universal;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.biome.RegionBiomeSource;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TFCRegionVisualizer implements IGeneratorVisualizer<TFCChunkGenerator, GridSize, GridScale, IRegionVisualizerType<?, ?>> {

    public static final TFCRegionVisualizer INSTANCE = new TFCRegionVisualizer();

    private static final BiomeSourceExtension BIOME_UNIT = new RegionBiomeSource(Universal.getter());
    private static final Holder<NoiseGeneratorSettings> NOISE_UNIT = Holder.direct(new NoiseGeneratorSettings(
            new NoiseSettings(-64, 320, 0, 0),
            Blocks.AIR.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            null,
            null,
            List.of(),
            TFCChunkGenerator.SEA_LEVEL_Y,
            false,
            false,
            false,
            false
    ));

    static final StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> GENERATOR_NETWORK_CODEC =
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Settings.CODEC.codec())
                    .map(s -> new TFCChunkGenerator(BIOME_UNIT, NOISE_UNIT, s), TFCChunkGenerator::settings);

    public static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld.region");

    public static final ResourceLocation ID = TFCGenViewer.id("tfc_region");

    private final Supplier<Stream<IRegionVisualizerType<?, ?>>> validVisualizers = GenViewerAPI.cachedOfTypeForced(IRegionVisualizerType.class, Stream::sorted);

    private TFCRegionVisualizer() {}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Stream<IRegionVisualizerType<?, ?>> visualzierStream() {
        return validVisualizers.get();
    }

    @Override
    public GridScale scale() {
        return GridScale.INSTANCE;
    }

    @Override
    public int maximumPreviewOffset() {
        return GridSize._5.sizeInPixels();
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
}
