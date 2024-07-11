package com.notenoughmail.tfcgenviewer;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TFCGenViewer.ID)
public class TFCGenViewer {

    public static final String ID = "tfcgenviewer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Component PREVIEW_WORLD = Component.translatable("button." + ID + ".preview_world");

    public TFCGenViewer() {
        Config.register();
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(ID, path);
    }
}
