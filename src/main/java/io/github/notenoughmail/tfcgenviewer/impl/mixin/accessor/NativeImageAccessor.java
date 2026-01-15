package io.github.notenoughmail.tfcgenviewer.impl.mixin.accessor;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NativeImage.class)
public interface NativeImageAccessor {

    @Accessor("pixels")
    long tfcgenviewer$GetPixels();
}
