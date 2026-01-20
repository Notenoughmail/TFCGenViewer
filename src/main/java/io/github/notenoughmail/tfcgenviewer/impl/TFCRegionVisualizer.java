package io.github.notenoughmail.tfcgenviewer.impl;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.GridSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TFCRegionVisualizer implements IGeneratorVisualizer<TFCChunkGenerator, GridSize, GridScale, IRegionVisualizerType<?, ?>> {

    public static final TFCRegionVisualizer INSTANCE = new TFCRegionVisualizer();

    static final StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> GENERATOR_NETWORK_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(BiomeSource.CODEC.xmap(BiomeSourceExtension.class::cast, BiomeSourceExtension::self)), g -> g.customBiomeSource,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(NoiseGeneratorSettings.CODEC), g -> g.noiseSettings,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Settings.CODEC.codec()), TFCChunkGenerator::settings,
            TFCChunkGenerator::new
    );

    public static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld.region");

    public static final ResourceLocation ID = TFCGenViewer.id("tfc_region");

    private final Supplier<List<IRegionVisualizerType<?, ?>>> validVisualizers =
            Suppliers.memoize(() -> GenViewerAPI.VISUALIZER_REGISTRY.stream()
                    .filter(IRegionVisualizerType.class::isInstance)
                    .<IRegionVisualizerType<?, ?>>map(IRegionVisualizerType.class::cast)
                    .sorted(Comparator.comparing(IRegionVisualizerType::sort))
                    .toList());

    private TFCRegionVisualizer() {}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Stream<IRegionVisualizerType<?, ?>> visualzierStream() {
        return validVisualizers.get().stream();
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
