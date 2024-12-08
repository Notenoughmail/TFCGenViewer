package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record PreviewInfo(
        Component rightInfo,
        ResourceLocation image,
        int previewSizeGrids,
        int x0,
        int y0,
        boolean empty,
        @Nullable Int2ObjectFunction<Component> tooltip
) {

    public PreviewInfo(Component rightInfo, ResourceLocation image, int previewSizeGrids, int x0, int y0, @Nullable Int2ObjectFunction<Component> tooltip) {
        this(rightInfo, image, previewSizeGrids, x0, y0, false, tooltip);
    }

    public static final PreviewInfo EMPTY = new PreviewInfo(Component.translatable("tfcgenviewer.preview_world.preview_info.generating"), TFCGenViewer.identifier("textures/gui/throbber.png"), 0, 0, 0, true, null);
    public static final PreviewInfo ERROR = new PreviewInfo(Component.translatable("tfcgenviewer.preview_world.preview_info.error"), TFCGenViewer.identifier("textures/gui/gen_error.png"), 0, 0, 0, null);
}
