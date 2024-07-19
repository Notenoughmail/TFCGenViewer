package com.notenoughmail.tfcgenviewer;

import com.notenoughmail.tfcgenviewer.config.BiomeColors;
import com.notenoughmail.tfcgenviewer.config.Colors;
import com.notenoughmail.tfcgenviewer.config.RockColors;
import com.notenoughmail.tfcgenviewer.config.RockTypeColors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;

import java.util.Map;

public class EventHandler {

    public static void init() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandler::reloadAssets);
        modBus.addListener(EventHandler::addPackFinders);
    }

    private static void reloadAssets(ModelEvent.RegisterAdditional e) {
        final ResourceManager rm = Minecraft.getInstance().getResourceManager();

        RockColors.clear();
        final Map<ResourceLocation, Resource> rocks = rm.listResources("tfcgenviewer/rocks", rl -> rl.getPath().endsWith(".json"));
        rocks.forEach(RockColors::assignColor);

        BiomeColors.clear();
        final Map<ResourceLocation, Resource> biomes = rm.listResources("tfcgenviewer/biomes", rl -> rl.getPath().endsWith(".json"));
        biomes.forEach(BiomeColors::assignColor);

        final Map<ResourceLocation, Resource> rockTypes = rm.listResources("tfcgenviewer/rock_types", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        rockTypes.forEach(RockTypeColors::assignGradient);

        final Map<ResourceLocation, Resource> colors = rm.listResources("tfcgenviewer/colors", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().equals("tfcgenviewer/colors/colors.json"));
        colors.forEach(Colors::assign);

    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            final IModFileInfo info = ModList.get().getModFileById(TFCGenViewer.ID);
            if (info != null) {
                final IModFile file = info.getFile();
                event.addRepositorySource(packConsumer -> {
                    final Pack pack = Pack.readMetaAndCreate(
                            TFCGenViewer.identifier("tfc_seed_maker").toString(),
                            Component.literal("TFCSeedMaker Rock Preview Colors"),
                            false,
                            id -> new PathPackResources(id, true, file.findResource("resourcepacks", "tfc_seed_maker")),
                            PackType.CLIENT_RESOURCES,
                            Pack.Position.TOP,
                            PackSource.BUILT_IN
                    );
                    if (pack != null) {
                        packConsumer.accept(pack);
                    }
                });
            }
        }
    }
}
