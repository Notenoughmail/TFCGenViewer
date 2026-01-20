package io.github.notenoughmail.tfcgenviewer.impl.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.screen.PreviewScreen;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
public abstract class MoreTabMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private <
            V extends IVisualizerType<G, C, S, O>,
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<I>,
            O extends IVisualizerType.Options<O>,
            I extends ImageSize
            > void tfcgenviewer$AddPreviewButton(CreateWorldScreen parent, CallbackInfo ci, @Local GridLayout.RowHelper rowHelper) {
        parent.getUiState()
                .getSettings()
                .selectedDimensions()
                .dimensions()
                .forEach((key, value) -> {
                    if (value.generator() instanceof ChunkGeneratorExtension ext) {
                        ImplAPI.getVisualizersFor(ext).forEach(viz -> rowHelper.addChild(
                                Button.builder(
                                        Component.translatable(
                                                "tfcgenviewer.button.preview",
                                                Component.translatable(key.location().toLanguageKey(ILevelExtension.TRANSLATION_PREFIX)),
                                                viz.name()
                                        ),
                                        b -> parent.getMinecraft().setScreen(new PreviewScreen<>(
                                                (G) ext,
                                                (IGeneratorVisualizer<G, I, S, V>) viz,
                                                parent,
                                                key
                                        ))
                                ).width(210).build()
                        ));
                    }
                });
    }
}
