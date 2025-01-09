package com.notenoughmail.tfcgenviewer.mixin;

import com.mojang.serialization.DataResult;
import net.dries007.tfc.world.settings.RockLayerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RockLayerSettings.class, remap = false)
public interface RockLayerSettingsAccessor {

    @Accessor(value = "data", remap = false)
    RockLayerSettings.Data tfcgenviewer$GetData();

    @Invoker(value = "processData", remap = false)
    DataResult<RockLayerSettings> tfcgenviewer$processData(RockLayerSettings.Data data);
}
