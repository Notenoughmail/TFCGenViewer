package io.github.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.client.ClientPacketHandler;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGVCommands;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.MultiViewResponsePacket;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.SingleViewResponsePacket;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.ViewRequestPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NETWORK_VERSION = ModList.get().getModFileById(ID).versionString();

    public static final Component TRUE = Component.translatable("tfcgenviewer.option.true");
    public static final Component FALSE = Component.translatable("tfcgenviewer.option.false");

    public TFCGenViewer(IEventBus modBus) {
        TFCGenViewerRegistration.init(modBus);
        modBus.addListener(this::newRegistries);
        modBus.addListener(this::registerPayloadHandlers);
        GenViewerAPI.registerGeneratorVisualizer(TFCRegionVisualizer.INSTANCE);
        NeoForge.EVENT_BUS.addListener(TFCGVCommands::registerCommands);
    }

    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static <K, V> Map<K, V> mapFromEntries(Stream<Map.Entry<K, V>> entries, @Nullable Supplier<Map<K, V>> origin) {
        if (origin == null) {
            return Map.ofEntries(entries.<Map.Entry<K, V>>toArray(Map.Entry[]::new));
        }
        return entries.collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                origin
        ));
    }

    private void newRegistries(NewRegistryEvent event) {
        event.register(GenViewerAPI.VISUALIZER_REGISTRY);
        event.register(GenViewerAPI.GRADIENT_REGISTRY);
        event.register(GenViewerAPI.DISPATCH_GRADIENT_REGISTRY);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();

        registrar.playToServer(
                ViewRequestPacket.TYPE,
                ViewRequestPacket.STREAM_CODEC,
                ViewRequestPacket::handleOnServerMainThread
        );

        registrar.playToClient(
                SingleViewResponsePacket.TYPE,
                SingleViewResponsePacket.STREAM_CODEC,
                ClientPacketHandler::onViewResponse
        );
        registrar.playToClient(
                MultiViewResponsePacket.TYPE,
                MultiViewResponsePacket.STREAM_CODEC,
                ClientPacketHandler::onMultiViewResponse
        );
    }
}
