package io.github.notenoughmail.tfcgenviewer.client;

import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IGeneratorVisualizer;
import io.github.notenoughmail.tfcgenviewer.api.visualizer.IVisualizerType;
import io.github.notenoughmail.tfcgenviewer.client.screen.ViewWorldScreen;
import io.github.notenoughmail.tfcgenviewer.impl.network.RegistryContents;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.SingleViewResponsePacket;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.stream.Stream;

public class ClientPacketHandler {

    public static <
            V extends IVisualizerType<G, C, S, O>,
            G extends ChunkGeneratorExtension,
            C,
            S extends IScale<I>,
            O extends IVisualizerType.Options<O>,
            I extends ImageSize
            > void onViewResponse(SingleViewResponsePacket response, IPayloadContext ctx) {
        final RegistryAccess combinedRegistryAccess = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
                ctx.player().registryAccess().registries(),
                response.registries().stream().map(RegistryContents::asRegistry)
        )).freeze();
        Minecraft.getInstance().setScreen(new ViewWorldScreen<>(
                (G) response.generator(),
                (IGeneratorVisualizer<G, I, S, V>) response.generatorVisualizer(),
                (List<V>) response.visualizerTypes(),
                combinedRegistryAccess,
                response.allowSpawnDraw(),
                response.allowExport(),
                response.allowCoordinates(),
                response.worldSeed(),
                response.xOrigin(),
                response.zOrigin()
        ));
    }
}
