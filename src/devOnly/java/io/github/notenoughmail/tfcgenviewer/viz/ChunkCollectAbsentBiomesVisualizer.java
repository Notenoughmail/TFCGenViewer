package io.github.notenoughmail.tfcgenviewer.viz;

import io.github.notenoughmail.tfcgenviewer.api.DrawParallelism;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.impl.visualizers.chunk.ChunkBiomeVisualizer;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public class ChunkCollectAbsentBiomesVisualizer extends ChunkBiomeVisualizer {

    @Override
    public Cache createCache(RegistryAccess registryAccess, TFCChunkGenerator generator, ImageSize size, long worldSeed, NoneOpt options, DrawParallelism parallelism) {
        return super.createCache(registryAccess, generator, size, worldSeed, options, parallelism).collectAbsent();
    }

    @Override
    public Component name() {
        return Component.literal("Collect Absent Biomes");
    }
}
