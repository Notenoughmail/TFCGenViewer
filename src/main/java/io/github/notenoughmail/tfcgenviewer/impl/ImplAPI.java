package io.github.notenoughmail.tfcgenviewer.impl;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ImplAPI {

    public static synchronized void register(IGeneratorVisualizer<? ,? ,?, ?> generatorVisualizer) {
        GENS.computeIfAbsent(generatorVisualizer.generatorType(), c -> new ArrayList<>()).add(generatorVisualizer);
        GEN_IDS.put(generatorVisualizer.id(), generatorVisualizer);
    }

    public static <G extends ChunkGeneratorExtension, V extends IVisualizerType<G, ?, ?, ?>> List<IGeneratorVisualizer<G, ?, ?, V>> getVisualizersFor(G gen) {
        return TFCGenViewer.cast(GENS.get(gen.getClass()));
    }

    public static final Map<ResourceLocation, IGeneratorVisualizer<?, ?, ?, ?>> GEN_IDS = new HashMap<>();

    private static final Map<Class<? extends ChunkGeneratorExtension>, List<IGeneratorVisualizer<?, ?, ?, ?>>> GENS = new IdentityHashMap<>();

    public static final StreamCodec<ByteBuf, IGeneratorVisualizer<?, ?, ?, ?>> GENERATOR_VISUALIZER_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(GEN_IDS::get, IGeneratorVisualizer::id);
}
