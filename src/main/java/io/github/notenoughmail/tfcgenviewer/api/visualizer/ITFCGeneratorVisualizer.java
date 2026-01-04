package io.github.notenoughmail.tfcgenviewer.api.visualizer;

import io.github.notenoughmail.tfcgenviewer.api.scale.IScaleGroup;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.settings.Settings;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface ITFCGeneratorVisualizer<S extends IScaleGroup<?>, V extends ITFCVisualizer<?>> extends IGeneratorVisualizer<TFCChunkGenerator, S, V> {

    StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> GENERATOR_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BIOME_SOURCE), TFCChunkGenerator::getBiomeSource,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(NoiseGeneratorSettings.CODEC), g -> g.noiseSettings,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Settings.CODEC.codec()), TFCChunkGenerator::settings,
            TFCChunkGenerator::new
    );

    @Override
    default Class<? extends TFCChunkGenerator> generatorType() {
        return TFCChunkGenerator.class;
    }

    @Override
    default TFCChunkGenerator recreateGenerator(TFCChunkGenerator generator) {
        return new TFCChunkGenerator(generator.getBiomeSource(), generator.noiseSettings, generator.settings());
    }

    @Override
    default StreamCodec<RegistryFriendlyByteBuf, TFCChunkGenerator> generatorCodec() {
        return GENERATOR_CODEC;
    }
}
