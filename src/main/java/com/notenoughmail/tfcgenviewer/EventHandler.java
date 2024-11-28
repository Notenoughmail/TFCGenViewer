package com.notenoughmail.tfcgenviewer;

import com.notenoughmail.tfcgenviewer.config.KeyMappings;
import com.notenoughmail.tfcgenviewer.config.color.BiomeColors;
import com.notenoughmail.tfcgenviewer.config.color.Colors;
import com.notenoughmail.tfcgenviewer.config.color.RockColors;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerRequestPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.resource.PathPackResources;

public class EventHandler {

    public static void init() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandler::addPackFinders);
        modBus.addListener(EventHandler::registerKeyMappings);
        modBus.addListener(EventHandler::registerClientResourceReloadListeners);

        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventHandler::onKeyInput);
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
                event.addRepositorySource(packConsumer -> {
                    final Pack pack = Pack.readMetaAndCreate(
                            TFCGenViewer.identifier("random").toString(),
                            Component.literal("Random Preview Colors"),
                            false,
                            id -> new PathPackResources(id, true, file.findResource("resourcepacks", "random")),
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

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_VIEWER);
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (KeyMappings.OPEN_VIEWER.isDown()) {
            TFCGVChannel.send(PacketDistributor.SERVER.noArg(), ViewerRequestPacket.INSTANCE);
        }
    }

    private static void registerClientResourceReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(BiomeColors.Biomes);
        event.registerReloadListener(RockColors.Rocks);
        event.registerReloadListener(Colors.Color);
        event.registerReloadListener(Colors.Gradient);
    }
}
