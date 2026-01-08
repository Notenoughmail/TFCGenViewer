package io.github.notenoughmail.tfcgenviewer.client;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.color.ColorKey;
import io.github.notenoughmail.tfcgenviewer.api.color.Colors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@Mod(value = TFCGenViewer.ID, dist = Dist.CLIENT)
public class TFCGenViewerClient {

    public TFCGenViewerClient(IEventBus modBus) {
        modBus.addListener(this::clientReloadListeners);
    }

    private void clientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ColorKey.ReloadListener.INSTANCE);
        event.registerReloadListener(Colors.BIOME_COLORS);
        event.registerReloadListener(Colors.ROCK_COLORS);
    }
}
