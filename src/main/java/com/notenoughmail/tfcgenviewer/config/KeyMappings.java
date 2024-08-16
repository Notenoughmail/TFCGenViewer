package com.notenoughmail.tfcgenviewer.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyMappings {

    public static final KeyMapping OPEN_VIEWER = new KeyMapping("tfcgenviewer.key.open_viewer", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "TFCGenViewer");
}
