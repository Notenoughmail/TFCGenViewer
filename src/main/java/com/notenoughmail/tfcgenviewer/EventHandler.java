package com.notenoughmail.tfcgenviewer;

import com.notenoughmail.tfcgenviewer.config.RockColors;
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
import net.minecraftforge.forgespi.language.IModInfo;
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
        RockColors.clear();
        final ResourceManager rm = Minecraft.getInstance().getResourceManager();
        final Map<ResourceLocation, Resource> resources = rm.listResources("tfcgenviewer/rocks", rl -> rl.getPath().endsWith(".json"));
        resources.forEach(RockColors::assignColor);
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