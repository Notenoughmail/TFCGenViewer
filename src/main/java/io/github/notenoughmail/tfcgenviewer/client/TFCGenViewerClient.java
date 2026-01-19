package io.github.notenoughmail.tfcgenviewer.client;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import io.github.notenoughmail.tfcgenviewer.impl.ClimateFeatureCache;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = TFCGenViewer.ID, dist = Dist.CLIENT)
public class TFCGenViewerClient {

    public static ModConfigSpec.BooleanValue dingWhenGenerated, displayGenerationProgress;

    public TFCGenViewerClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::clientReloadListeners);
        modBus.addListener(this::addPackFinders);

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
}
