package com.notenoughmail.tfcgenviewer.mixin;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.TFCLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TFCLayers.class, remap = false)
public interface TFCLayersAccessor {

    @Accessor(value = "BIOME_LAYERS", remap = false)
    static BiomeExtension[] tfcgenviewer$BiomeLayers() {
        throw new AssertionError();
    }
}
