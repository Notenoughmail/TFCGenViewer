package com.notenoughmail.tfcgenviewer.mixin;

import net.dries007.tfc.world.settings.RockLayerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RockLayerSettings.class, remap = false)
public interface RockLayerSettingsAccessor {

    @Accessor(value = "data", remap = false)
    RockLayerSettings.Data tfcgenviewer$GetData();
}
