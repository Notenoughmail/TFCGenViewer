package io.github.notenoughmail.tfcgenviewer;

import io.github.notenoughmail.tfcgenviewer.color.ColorProvider;
import io.github.notenoughmail.tfcgenviewer.langs.EN_US;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TFCGenViewer.ID)
public class DataEntry {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            final PackOutput output = event.getGenerator().getPackOutput();
            event.addProvider(new EN_US(output));
            event.addProvider(new ColorProvider(output, event.getLookupProvider()));
        }
    }
}
