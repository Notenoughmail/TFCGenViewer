package com.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import com.notenoughmail.tfcgenviewer.config.Config;
import com.notenoughmail.tfcgenviewer.config.ServerConfig;
import com.notenoughmail.tfcgenviewer.network.TFCGVChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Component PREVIEW_WORLD = Component.translatable("button." + ID + ".preview_world");

    public TFCGenViewer() {

        TFCGVChannel.init();
        ServerConfig.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            Config.register();
            EventHandler.init();
        }
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(ID, path);
    }
}
