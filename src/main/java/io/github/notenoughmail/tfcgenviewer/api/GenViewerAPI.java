package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.ITFCVisualizer;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public interface GenViewerAPI {

    ResourceKey<Registry<ITFCVisualizer<?>>> TFC_VISUALIZERS = ResourceKey.createRegistryKey(TFCGenViewer.id("visualizers/tfc"));
    ResourceKey<Registry<Gradient.Registry>> GRADIENT = ResourceKey.createRegistryKey(TFCGenViewer.id("gradient"));
    Registry<ITFCVisualizer<?>> TFC_VISUALIZER_REGISTRY = new RegistryBuilder<>(TFC_VISUALIZERS).create();
    Registry<Gradient.Registry> GRADIENT_REGISTRY = new RegistryBuilder<>(GRADIENT).create();

    static void registerGeneratorVisualizer(IGeneratorVisualizer<?, ?, ?> generatorVisualizer) {
        Hidden.TYPES.computeIfAbsent(generatorVisualizer.generatorType(), c -> new ArrayList<>()).add(generatorVisualizer);
    }

    static <G extends ChunkGeneratorExtension> List<IGeneratorVisualizer<G, ?, ?>> getVisualizersFor(Class<G> clazz) {
        return TFCGenViewer.cast(Hidden.TYPES.get(clazz));
    }

    @ApiStatus.Internal
    class Hidden {
        static final Map<Class<? extends ChunkGeneratorExtension>, List<IGeneratorVisualizer<?, ?, ?>>> TYPES = new IdentityHashMap<>();
    }
}
