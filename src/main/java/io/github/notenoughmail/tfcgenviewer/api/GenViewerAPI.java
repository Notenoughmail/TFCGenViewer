package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public interface GenViewerAPI {

    ResourceKey<Registry<IVisualizerType<?, ?, ?, ?>>> VISUALIZER = ResourceKey.createRegistryKey(TFCGenViewer.id("visualizers"));
    ResourceKey<Registry<Gradient.Preset>> GRADIENT = ResourceKey.createRegistryKey(TFCGenViewer.id("gradient"));
    Registry<IVisualizerType<?, ?, ?, ?>> VISUALIZER_REGISTRY = new RegistryBuilder<>(VISUALIZER).create();
    Registry<Gradient.Preset> GRADIENT_REGISTRY = new RegistryBuilder<>(GRADIENT).create();

    static void registerGeneratorVisualizer(IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer) {
        ImplAPI.register(generatorVisualizer);
    }
}
