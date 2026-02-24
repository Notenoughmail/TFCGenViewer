package io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor;

import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.noise.Noise2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TFCChunkGenerator.class)
public interface TFCChunkGeneratorAccessor {

    @Accessor("seed")
    void tfcgenviewer$SetSeed(Seed seed);

    @Accessor("tideHeightNoise")
    void tfcgenviewer$SetTideHeightNoise(Noise2D noise);
}
