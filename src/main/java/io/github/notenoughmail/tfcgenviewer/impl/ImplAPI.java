package io.github.notenoughmail.tfcgenviewer.impl;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.SerializationInformation;
import io.github.notenoughmail.tfcgenviewer.api.registry.ISyncRegistries;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.function.BiConsumer;

public class ImplAPI {

    public static synchronized void register(IGeneratorVisualizer<? ,? ,?, ?> generatorVisualizer) {
        GENS.computeIfAbsent(generatorVisualizer.generatorType(), c -> new ArrayList<>()).add(generatorVisualizer);
        GEN_IDS.put(generatorVisualizer.id(), generatorVisualizer);
    }

    public static <
            G extends ChunkGeneratorExtension,
            V extends IVisualizerType<G, ?, ?, ?>
            > List<IGeneratorVisualizer<G, ?, ?, V>> getVisualizersFor(G gen) {
        return TFCGenViewer.cast(GENS.get(gen.getClass()));
    }

    // I wish I could say this is the worst thing in the mod, but it's not
    // It is however *in service* of the worst thing in this mod
    public static <T> void getAllSync(ResourceKey<? extends Registry<T>> key, BiConsumer<ISyncRegistries, Codec<T>> ret) {
        class Info implements SerializationInformation {
            final MutableObject<ISyncRegistries> sync = new MutableObject<>();
            @Override
            public <R> void provide(ResourceKey<? extends Registry<R>> registry, Codec<R> codec) {
                if (registry == key) {
                    ret.accept(sync.getValue(), TFCGenViewer.cast(codec));
                }
            }
            void sync(ISyncRegistries sync) {
                this.sync.setValue(sync);
                sync.elementCodecForRegistry(this);
            }
        }
        final Info info = new Info();

        GENS.values().stream().flatMap(List::stream).forEach(info::sync);
        GenViewerAPI.VISUALIZER_REGISTRY.forEach(info::sync);
    }

    public static final Map<ResourceLocation, IGeneratorVisualizer<?, ?, ?, ?>> GEN_IDS = new HashMap<>();

    private static final Map<Class<? extends ChunkGeneratorExtension>, List<IGeneratorVisualizer<?, ?, ?, ?>>> GENS = new IdentityHashMap<>();

    public static final StreamCodec<ByteBuf, IGeneratorVisualizer<?, ?, ?, ?>> GENERATOR_VISUALIZER_STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(GEN_IDS::get, IGeneratorVisualizer::id);
}
