package com.notenoughmail.tfcgenviewer;

import com.notenoughmail.tfcgenviewer.config.KeyMappings;
import com.notenoughmail.tfcgenviewer.config.color.*;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.network.packets.ViewerRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
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

import java.util.Map;

public class /*Client*/EventHandler {

    public static void init() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandler::reloadAssets);
        modBus.addListener(EventHandler::addPackFinders);
        modBus.addListener(EventHandler::registerKeyMappings);

        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventHandler::onKeyInput);
    }

    private static void reloadAssets(ModelEvent.RegisterAdditional e) {
        RockColors.clear();
        BiomeColors.clear();
        RockTypeColors.KEY.clearCache();
        InlandHeightColors.KEY.clearCache();
        RiversColors.KEY.clearCache();
        BiomeAltitudeColors.KEY.clearCache();
        Colors.TEMP_KEY.clearCache();
        Colors.RAIN_KEY.clearCache();

        final ResourceManager rm = Minecraft.getInstance().getResourceManager();

        final Map<ResourceLocation, Resource> rocks = rm.listResources("tfcgenviewer/rocks", rl -> rl.getPath().endsWith(".json"));
        rocks.forEach(RockColors::assignColor);

        final Map<ResourceLocation, Resource> biomes = rm.listResources("tfcgenviewer/biomes", rl -> rl.getPath().endsWith(".json"));
        biomes.forEach(BiomeColors::assignColor);

        final Map<ResourceLocation, Resource> rockTypes = rm.listResources("tfcgenviewer/rock_types", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        rockTypes.forEach(RockTypeColors::assignGradient);

        final Map<ResourceLocation, Resource> inlandHeights = rm.listResources("tfcgenviewer/inland_height", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        inlandHeights.forEach(InlandHeightColors::assign);

        final Map<ResourceLocation, Resource> rivers = rm.listResources("tfcgenviewer/rivers_and_mountains", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        rivers.forEach(RiversColors::assign);

        final Map<ResourceLocation, Resource> biomeAltitude = rm.listResources("tfcgenviewer/biome_altitude", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        biomeAltitude.forEach(BiomeAltitudeColors::assign);

        final Map<ResourceLocation, Resource> misc = rm.listResources("tfcgenviewer/colors", rl -> rl.getNamespace().equals(TFCGenViewer.ID) && rl.getPath().endsWith(".json"));
        misc.forEach(Colors::assign);
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

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_VIEWER);
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (KeyMappings.OPEN_VIEWER.isDown()) {
            TFCGVChannel.send(PacketDistributor.SERVER.noArg(), ViewerRequestPacket.INSTANCE);
        }
    }
}
