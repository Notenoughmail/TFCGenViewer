package io.github.notenoughmail.tfcgenviewer.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.api.cache.ClimateFeatureCache;
import io.github.notenoughmail.tfcgenviewer.impl.ImplAPI;
import io.github.notenoughmail.tfcgenviewer.impl.network.packet.ViewRequestPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Mod(value = TFCGenViewer.ID, dist = Dist.CLIENT)
public class TFCGenViewerClient {

    public static ModConfigSpec.BooleanValue dingWhenGenerated, displayGenerationProgress;
    public static ModConfigSpec.DoubleValue maxPreviewWidth;

    private final KeyMapping openViewer = new KeyMapping("tfcgenviewer.key.open_viewer", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "TFCGenViewer");

    public TFCGenViewerClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::clientReloadListeners);
        modBus.addListener(this::addPackFinders);
        modBus.addListener(this::registerKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onInput);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

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
                        " If the info pane should show a progress bar while a preview is being generated",
                        ""
                ).define("displayGenerationProgress", true);
        maxPreviewWidth = configBuilder
                .comment(
                        "",
                        " The maximum portion of the screen the world preview may take up.",
                        " The preview will always fit into the largest square between this portion of the screen with and the majority of the screen height",
                        ""
                ).defineInRange("maxPreviewWidth", 0.5D, 0.25D, 0.75D);
        container.registerConfig(ModConfig.Type.CLIENT, configBuilder.build());
    }

    private void clientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ColorKey.ReloadListener.INSTANCE);
        event.registerReloadListener(Colors.BIOME_COLORS);
        event.registerReloadListener(Colors.ROCK_COLORS);
        event.registerReloadListener(Colors.KOPPEN_COLORS);
        event.registerReloadListener(Colors.MISC_COLORS);
        event.registerReloadListener(Colors.MISC_GRADIENTS);
        event.registerReloadListener(ClimateFeatureCache.FEATURES);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(
                TFCGenViewer.id("resourcepacks/tfc_seed_maker"),
                PackType.CLIENT_RESOURCES,
                Component.literal("TFCSeedMaker Rock Preview Colors"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openViewer);
    }

    private static final Component TFCGV_ABSENT = Component.translatable("tfcgenviewer.network.view_request.response.absent");

    private void onInput(InputEvent.Key event) {
        final ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (openViewer.isDown() && clientPacketListener != null) {
            if (clientPacketListener.hasChannel(ViewRequestPacket.TYPE)) {
                PacketDistributor.sendToServer(new ViewRequestPacket(ImplAPI.GEN_IDS.keySet()));
            } else {
                Minecraft.getInstance().getChatListener().handleSystemMessage(TFCGV_ABSENT, false);
            }
        }
    }
}
