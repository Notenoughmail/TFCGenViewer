package io.github.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import io.github.notenoughmail.tfcgenviewer.api.GenViewerAPI;
import io.github.notenoughmail.tfcgenviewer.impl.TFCGenViewerRegistration;
import io.github.notenoughmail.tfcgenviewer.impl.TFCRegionVisualizer;
import net.dries007.tfc.world.Seed;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCGenViewer(IEventBus modBus) {
        TFCGenViewerRegistration.init(modBus);
        modBus.addListener(this::newRegistries);
        GenViewerAPI.registerGeneratorVisualizer(TFCRegionVisualizer.INSTANCE);
    }

    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static void skip(Seed seed, int skip) {
        for (int s = 0 ; s < skip ; s++) seed.next();
    }

    private void newRegistries(NewRegistryEvent event) {
        event.register(GenViewerAPI.TFC_REGION_VISUALIZER_REGISTRY);
        event.register(GenViewerAPI.GRADIENT_REGISTRY);
    }
}
