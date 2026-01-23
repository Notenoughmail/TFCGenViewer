package io.github.notenoughmail.tfcgenviewer.api;

import com.google.common.base.Suppliers;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface GenViewerAPI {

    ResourceKey<Registry<IVisualizerType<?, ?, ?, ?>>> VISUALIZER = ResourceKey.createRegistryKey(TFCGenViewer.id("visualizers"));
    ResourceKey<Registry<Gradient.Preset>> GRADIENT = ResourceKey.createRegistryKey(TFCGenViewer.id("gradient"));
    Registry<IVisualizerType<?, ?, ?, ?>> VISUALIZER_REGISTRY = new RegistryBuilder<>(VISUALIZER).create();
    Registry<Gradient.Preset> GRADIENT_REGISTRY = new RegistryBuilder<>(GRADIENT).create();

    static <V extends IVisualizerType<?, ?, ?, ?>> Supplier<Stream<V>> cachedOfType(Class<V> type) {
        final Supplier<List<V>> allOfType = Suppliers.memoize(() -> VISUALIZER_REGISTRY.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList());
        return () -> allOfType.get().stream();
    }

    static <V extends IVisualizerType<?, ?, ?, ?>, C> Supplier<Stream<V>> cachedOfTypeForced(Class<C> type) {
        return cachedOfType((Class<V>) type);
    }

    static void registerGeneratorVisualizer(IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer) {
        ImplAPI.register(generatorVisualizer);
    }
}
