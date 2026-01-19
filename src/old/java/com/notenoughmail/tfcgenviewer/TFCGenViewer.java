package com.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.config.ServerConfig;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import com.notenoughmail.tfcgenviewer.util.TFCGVCommands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.Map;
import java.util.stream.Stream;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<PlacedFeature> VISUALIZABLE_FEATURES = TagKey.create(Registries.PLACED_FEATURE, identifier("visualizable_features"));

    public TFCGenViewer() {

        TFCGVChannel.init();
        ServerConfig.register();

        MinecraftForge.EVENT_BUS.addListener(TFCGVCommands::register);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            Config.register();
            EventHandler.init();
        }
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(ID, path);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> ofEntryStream(Stream<Map.Entry<K, V>> stream) {
        return Map.ofEntries(stream.toArray(Map.Entry[]::new));
    }
}
