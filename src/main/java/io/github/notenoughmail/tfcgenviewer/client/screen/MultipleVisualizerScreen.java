package io.github.notenoughmail.tfcgenviewer.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MultipleVisualizerScreen extends Screen {

    public static final Component TITLE = Component.translatable("tfcgenviewer.screen.multiple_generator_visualizers.title");

    private final List<ResourceLocation> visualizers;

    public MultipleVisualizerScreen(List<ResourceLocation> visualizers) {
        super(TITLE);
        this.visualizers = visualizers;
    }
}
