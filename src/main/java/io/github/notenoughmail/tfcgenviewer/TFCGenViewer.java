package io.github.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.client.ClientBridge;
import io.github.notenoughmail.tfcgenviewer.impl.TFCChunkVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGVCommands;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.MultiViewResponsePacket;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.SingleViewResponsePacket;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.ViewRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String NETWORK_VERSION = "2.0.1";

    public static ModConfigSpec.BooleanValue dingWhenGenerated, displayGenerationProgress, disableParallelGeneration;
    public static ModConfigSpec.DoubleValue maxPreviewWidth;
    public static ModConfigSpec.IntValue absoluteMaximumMicrosToDrawPixel, maximumNumberOfParallelDrawOperations;

    public TFCGenViewer(IEventBus modBus, ModContainer container) {
        TFCGenViewerRegistration.init(modBus);
        modBus.addListener(this::newRegistries);
        modBus.addListener(this::registerPayloadHandlers);
        GenViewerAPI.registerGeneratorVisualizer(TFCRegionVisualizer.INSTANCE);
        GenViewerAPI.registerGeneratorVisualizer(TFCChunkVisualizer.INSTANCE);
        NeoForge.EVENT_BUS.addListener(TFCGVCommands::registerCommands);

        final ModConfigSpec.Builder configBuilder = new ModConfigSpec.Builder();
        dingWhenGenerated = configBuilder
                .comment(
                        "",
                        " If a sound should be played when a preview finishes generating",
                        ""
                ).define("dingWhenGenerated", true);
        displayGenerationProgress = configBuilder
                .comment(
                        "",
                        " If a progress bar should be displayed while a preview is generating",
                        ""
                ).define("displayGenerationProgress", true);
        maxPreviewWidth = configBuilder
                .comment(
                        "",
                        " The maximum portion of the screen the world preview may take up",
                        "",
                        " The preview will always fit into the largest square between this portion of the screen width",
                        " and the majority of the screen height",
                        ""
                ).defineInRange("maxPreviewWidth", 0.5D, 0.25D, 0.75D);
        disableParallelGeneration = configBuilder
                .comment(
                        "",
                        " If parallel generation should be forcefully disabled, regardless of a visualizer's request",
                        ""
                ).define("disableParallelGeneration", false);
        maximumNumberOfParallelDrawOperations = configBuilder
                .comment(
                        "",
                        " The maximum number of parallel draw operations that may occur if parallel generation is enabled",
                        ""
                ).defineInRange("maximumNumberOfParallelDrawOperations", 5, 2, Integer.MAX_VALUE); // Go at it
        absoluteMaximumMicrosToDrawPixel = configBuilder
                .comment(
                        "",
                        " The absolute maximum number of microseconds the image generator will process a single pixel before",
                        " cancelling the operation, filling the pixel with a default color, and logging an error",
                        ""
                ).defineInRange("absoluteMaximumMicrosToDrawPixel", 1000, 1, Integer.MAX_VALUE);
        container.registerConfig(ModConfig.Type.CLIENT, configBuilder.build());
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

    public static <T, I> Consumer<T> transformConsumer(Function<T, I> mapper, Consumer<I> consumer) {
        return t -> consumer.accept(mapper.apply(t));
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
                ClientBridge::singleViewResponse
        );
        registrar.playToClient(
                MultiViewResponsePacket.TYPE,
                MultiViewResponsePacket.STREAM_CODEC,
                ClientBridge::multiViewResponse
        );
    }
}
