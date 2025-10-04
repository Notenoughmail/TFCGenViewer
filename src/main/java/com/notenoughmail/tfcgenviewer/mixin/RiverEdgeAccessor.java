package com.notenoughmail.tfcgenviewer.mixin;

import net.dries007.tfc.world.region.RiverEdge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RiverEdge.class, remap = false)
public interface RiverEdgeAccessor {

    @Accessor(remap = false)
    int getMinPartX();

    @Accessor(remap = false)
    int getMaxPartX();

    @Accessor(remap = false)
    int getMinPartZ();

    @Accessor(remap = false)
    int getMaxPartZ();

}
