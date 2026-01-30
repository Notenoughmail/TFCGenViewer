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
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The primary point for TFCGenViewer's API
 */
public interface GenViewerAPI {

    /**
     * The key for the visualizer type registry
     */
    ResourceKey<Registry<IVisualizerType<?, ?, ?, ?>>> VISUALIZER = ResourceKey.createRegistryKey(TFCGenViewer.id("visualizers"));
    /**
     * The key for the preset gradient registry
     */
    ResourceKey<Registry<Gradient.Preset>> GRADIENT = ResourceKey.createRegistryKey(TFCGenViewer.id("gradient"));
    /**
     * The visualizer type registry. <strong>All</strong> visualizer types must be registered to this registry to be used
     */
    Registry<IVisualizerType<?, ?, ?, ?>> VISUALIZER_REGISTRY = new RegistryBuilder<>(VISUALIZER).create();
    /**
     * The preset gradient registry
     */
    Registry<Gradient.Preset> GRADIENT_REGISTRY = new RegistryBuilder<>(GRADIENT).create();

    /**
     * 
     * @param <V> The {@link IVisualizerType} subtype
     */
    static <V extends IVisualizerType<?, ?, ?, ?>> Supplier<Stream<V>> cachedOfType(Class<V> type, UnaryOperator<Stream<V>> modifySource) {
        final Supplier<List<V>> allOfType = Suppliers.memoize(() -> modifySource.apply(VISUALIZER_REGISTRY.stream()
                        .filter(type::isInstance)
                        .map(type::cast))
                .toList());
        return () -> allOfType.get().stream();
    }

    static <V extends IVisualizerType<?, ?, ?, ?>, C> Supplier<Stream<V>> cachedOfTypeForced(Class<C> type, UnaryOperator<Stream<V>> modifySource) {
        return cachedOfType((Class<V>) type, modifySource);
    }

    static void registerGeneratorVisualizer(IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer) {
        ImplAPI.register(generatorVisualizer);
    }
}
