package io.github.notenoughmail.tfcgenviewer.impl;

import com.google.common.base.Suppliers;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.registry.NetworkHolder;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ChunkSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCChunkVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCGeneratorVisualizer;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TFCChunkVisualizer implements ITFCGeneratorVisualizer<ChunkSize, ChunkScale, ITFCChunkVisualizerType<?, ?>> {

    public static final TFCChunkVisualizer INSTANCE = new TFCChunkVisualizer();

    public static final Component NAME = Component.translatable("tfcgenviewer.generator.tfc_overworld.chunk");

    public static final ResourceLocation ID = TFCGenViewer.id("tfc_chunk");

    private static final StreamCodec<ByteBuf, NoiseSettings> NOISE_SETTINGS_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, NoiseSettings::minY,
            ByteBufCodecs.INT, NoiseSettings::height,
            ByteBufCodecs.VAR_INT, NoiseSettings::noiseSizeHorizontal,
            ByteBufCodecs.VAR_INT, NoiseSettings::noiseSizeVertical,
            NoiseSettings::new
    );

    private static final Supplier<NoiseRouter> NOISE_SUPPLIER_UNIT = Suppliers.memoize(() -> new NoiseRouter(
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha(),
            DensityFunctions.blendAlpha()
    ));

    private static final StreamCodec<RegistryFriendlyByteBuf, Holder<NoiseGeneratorSettings>> NOISE_GENERATOR_SETTINGS_CODEC = NetworkHolder.streamCodec(StreamCodec.composite(
            NOISE_SETTINGS_CODEC, NoiseGeneratorSettings::noiseSettings,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(SurfaceRules.RuleSource.CODEC), NoiseGeneratorSettings::surfaceRule,
            (ns, rs) -> new NoiseGeneratorSettings(
                    ns,
                    Blocks.AIR.defaultBlockState(),
                    Blocks.AIR.defaultBlockState(),
                    NOISE_SUPPLIER_UNIT.get(),
                    rs,
                    List.of(),
                    TFCChunkGenerator.SEA_LEVEL_Y,
                    false,
                    false,
                    false,
                    false
            )
    ), Registries.NOISE_SETTINGS);

    private static final StreamCodec<RegistryFriendlyByteBuf, BiomeSourceExtension> BIOME_SOURCE_EXTENSION_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(BiomeSource.CODEC)
            .map(BiomeSourceExtension.class::cast, BiomeSourceExtension::self);

    private static final StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> GENERATOR_CODEC = StreamCodec.composite(
            BIOME_SOURCE_EXTENSION_CODEC, g -> g.customBiomeSource,
            NOISE_GENERATOR_SETTINGS_CODEC, g -> g.noiseSettings,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Settings.CODEC.codec()), TFCChunkGenerator::settings,
            TFCChunkGenerator::new
    );

    private final Supplier<Stream<ITFCChunkVisualizerType<?, ?>>> validVisualizers = GenViewerAPI.cachedOfTypeForced(ITFCChunkVisualizerType.class, Stream::sorted);

    private TFCChunkVisualizer() {}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Stream<ITFCChunkVisualizerType<?, ?>> visualzierStream() {
        return validVisualizers.get();
    }

    @Override
    public ChunkScale scale() {
        return ChunkScale.INSTANCE;
    }

    @Override
    public int maximumPreviewOffset() {
        return ChunkSize._5.sizeInPixels();
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
        return new TFCChunkGenerator(generator.customBiomeSource.copy(), generator.noiseSettings, generator.settings());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> generatorNetworkCodec() {
        return GENERATOR_CODEC;
    }
}
