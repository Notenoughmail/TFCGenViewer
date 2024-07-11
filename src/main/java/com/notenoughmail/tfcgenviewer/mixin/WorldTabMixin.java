package com.notenoughmail.tfcgenviewer.mixin;

import com.notenoughmail.tfcgenviewer.util.ISeedSetter;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class WorldTabMixin implements ISeedSetter {

    @Final
    @Shadow
    private EditBox seedEdit;

    @Override
    public void tfcgenviewer$SetSeed(String seed) {
        seedEdit.setValue(seed);
    }
}
