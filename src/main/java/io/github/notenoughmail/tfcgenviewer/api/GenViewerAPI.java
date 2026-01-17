package io.github.notenoughmail.tfcgenviewer.api;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.Gradient;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IRegionVisualizerType;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public interface GenViewerAPI {

    ResourceKey<Registry<IRegionVisualizerType<?, ?>>> TFC_REGION_VISUALIZER = ResourceKey.createRegistryKey(TFCGenViewer.id("visualizers/region"));
    ResourceKey<Registry<Gradient.Preset>> GRADIENT = ResourceKey.createRegistryKey(TFCGenViewer.id("gradient"));
    Registry<IRegionVisualizerType<?, ?>> TFC_REGION_VISUALIZER_REGISTRY = new RegistryBuilder<>(TFC_REGION_VISUALIZER).create();
    Registry<Gradient.Preset> GRADIENT_REGISTRY = new RegistryBuilder<>(GRADIENT).create();

    static void registerGeneratorVisualizer(IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer) {
        synchronized (Hidden.class) {
            Hidden.GENS.computeIfAbsent(generatorVisualizer.generatorType(), c -> new ArrayList<>()).add(generatorVisualizer);
            Hidden.GEN_IDS.put(generatorVisualizer, generatorVisualizer.id());
            Hidden.GEN_IDS_TO_TYPE_IDS.put(generatorVisualizer.id(), generatorVisualizer.allVisualizers().stream().map(IVisualizerType::id).toList());
        }
    }

    static <G extends ChunkGeneratorExtension> List<IGeneratorVisualizer<G, ?, ?, ?>> getVisualizersFor(Class<G> clazz) {
        return TFCGenViewer.cast(Hidden.GENS.get(clazz));
    }

    @ApiStatus.Internal
    class Hidden {
        static final Map<Class<? extends ChunkGeneratorExtension>, List<IGeneratorVisualizer<?, ?, ?, ?>>> GENS = new IdentityHashMap<>();
        static final Map<IGeneratorVisualizer<?, ?, ?, ?>, ResourceLocation> GEN_IDS = new IdentityHashMap<>();
        static final Map<ResourceLocation, List<ResourceLocation>> GEN_IDS_TO_TYPE_IDS = new HashMap<>();
    }
}
