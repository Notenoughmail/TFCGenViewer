package io.github.notenoughmail.tfcgenviewer;

import io.github.notenoughmail.tfcgenviewer.color.ColorProvider;
import io.github.notenoughmail.tfcgenviewer.color.GradientProvider;
import io.github.notenoughmail.tfcgenviewer.color.RegistryLinkedColorProvider;
import io.github.notenoughmail.tfcgenviewer.color.SeedMakerRockColorsProvider;
import io.github.notenoughmail.tfcgenviewer.langs.EN_US;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = TFCGenViewer.ID)
public class DataEntry {

    @SubscribeEvent
    private static void gatherData(GatherDataEvent event) {
        TFCGenViewer.LOGGER.info("Running TFCGenViewer data/asset generation");

        final PackOutput output = event.getGenerator().getPackOutput();
        final CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        if (event.includeServer()) {
            event.addProvider(new TagProvider(output, lookup, event.getExistingFileHelper()));
            event.addProvider(new RegistryLinkedColorProvider(output, lookup));
        }

        if (event.includeClient()) {
            event.addProvider(new EN_US(output));
            event.addProvider(new ColorProvider(output, lookup));
            event.addProvider(new SeedMakerRockColorsProvider(output, lookup));
            event.addProvider(new GradientProvider(output, lookup));
        }
    }
}
