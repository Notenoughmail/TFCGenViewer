package com.notenoughmail.tfcgenviewer.mixin;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.screen.PreviewGenerationScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public abstract class MoreTabMixin {

    @Inject(method = "<init>", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void tfcgenviewer$AddPreviewButton(CreateWorldScreen parent, CallbackInfo ci, GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(Button.builder(TFCGenViewer.PREVIEW_WORLD, button -> {
            parent.getMinecraft().setScreen(new PreviewGenerationScreen(parent));
        }).width(210).build());
    }
}
