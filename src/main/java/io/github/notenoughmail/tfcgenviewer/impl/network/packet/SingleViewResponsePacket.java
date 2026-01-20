package io.github.notenoughmail.tfcgenviewer.impl.network.packet;

import com.mojang.serialization.Codec;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import io.github.notenoughmail.tfcgenviewer.impl.network.RegistryContents;
import io.netty.buffer.ByteBuf;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SingleViewResponsePacket(
        IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer,
        ChunkGeneratorExtension generator,
        List<IVisualizerType<?, ?, ?, ?>> visualizerTypes,
        List<RegistryContents<?>> registries,
        boolean allowSpawnDraw,
        boolean allowExport,
        boolean allowCoordinates,
        long worldSeed,
        int xOrigin,
        int zOrigin
) implements CustomPacketPayload {

    private static final Map<ResourceKey<? extends Registry<?>>, Reg<?>> NETWORKED_REGISTRIES = new IdentityHashMap<>();

    private static final StreamCodec<ByteBuf, List<IVisualizerType<?, ?, ?, ?>>> VISUALIZER_TYPE_CODEC =
            ResourceLocation.STREAM_CODEC
                    .<IVisualizerType<?, ?, ?, ?>>map(GenViewerAPI.VISUALIZER_REGISTRY::get, GenViewerAPI.VISUALIZER_REGISTRY::getKey)
                    .apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, SingleViewResponsePacket> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ImplAPI.GENERATOR_VISUALIZER_STREAM_CODEC.encode(buf, p.generatorVisualizer);
                p.generatorVisualizer.generatorNetworkCodec().encode(buf, TFCGenViewer.cast(p.generator));
                VISUALIZER_TYPE_CODEC.encode(buf, p.visualizerTypes);
                registriesFromVisualizers(p.visualizerTypes).encode(buf, p.registries);
                buf.writeBoolean(p.allowSpawnDraw);
                buf.writeBoolean(p.allowExport);
                buf.writeBoolean(p.allowCoordinates);
                buf.writeLong(p.worldSeed);
                buf.writeInt(p.xOrigin);
                buf.writeInt(p.zOrigin);
            },
            buf -> {
                final IGeneratorVisualizer<?, ?, ?, ?> generatorVisualizer = ImplAPI.GENERATOR_VISUALIZER_STREAM_CODEC.decode(buf);
                final ChunkGeneratorExtension ext = generatorVisualizer.generatorNetworkCodec().decode(buf);
                final List<IVisualizerType<?, ?, ?, ?>> visualizerTypes = VISUALIZER_TYPE_CODEC.decode(buf);
                final List<RegistryContents<?>> registries = registriesFromVisualizers(visualizerTypes).decode(buf);

                return new SingleViewResponsePacket(
                        generatorVisualizer,
                        ext,
                        visualizerTypes,
                        registries,
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readLong(),
                        buf.readInt(),
                        buf.readInt()
                );
            }
    );

    private static StreamCodec<FriendlyByteBuf, List<RegistryContents<?>>> registriesFromVisualizers(List<IVisualizerType<?, ?, ?, ?>> visualizerTypes) {
        return RegistryContents.REGISTRY_KEY_CODEC
                .dispatch(RegistryContents::registry, key -> NETWORKED_REGISTRIES.computeIfAbsent(key, Reg::of).findFrom(visualizerTypes))
                .apply(ByteBufCodecs.list());
    }

    public static final Type<SingleViewResponsePacket> TYPE = new Type<>(TFCGenViewer.id("single_view_response"));

    @Override
    public Type<SingleViewResponsePacket> type() {
        return TYPE;
    }

    private static class Reg<T> extends IdentityHashMap<IVisualizerType<?, ?, ?, ?>, StreamCodec<FriendlyByteBuf, RegistryContents<T>>> {

        static Reg<?> of(ResourceKey<? extends Registry<?>> key) {
            return new Reg<>(TFCGenViewer.cast(key));
        }

        Reg(ResourceKey<? extends Registry<T>> key) {
            GenViewerAPI.VISUALIZER_REGISTRY.forEach(viz -> {
                final Codec<T> codec = viz.elementCodecForRegistry(key);
                if (codec != null) {
                    put(viz, RegistryContents.registryStreamCodec(codec, key));
                }
            });
        }

        StreamCodec<FriendlyByteBuf, RegistryContents<?>> findFrom(List<IVisualizerType<?, ?, ?, ?>> visualizerTypes) {
            return visualizerTypes.stream()
                    .map(this::get)
                    .filter(Objects::nonNull)
                    .<StreamCodec<FriendlyByteBuf, RegistryContents<?>>>map(TFCGenViewer::cast)
                    .findFirst()
                    .orElseThrow();
        }
    }
}
