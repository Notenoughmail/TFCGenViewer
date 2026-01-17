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
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = TFCGenViewer.ID, dist = Dist.CLIENT)
public class TFCGenViewerClient {

    public TFCGenViewerClient(IEventBus modBus) {
        modBus.addListener(this::clientReloadListeners);
        modBus.addListener(this::addPackFinders);
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
