package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record PreviewInfo(Component rightInfo, ResourceLocation image, int previewSizeBlocks, int x0, int y0, boolean empty) {

    public PreviewInfo(Component rightInfo, ResourceLocation image, int previewSizeBlocks, int x0, int y0) {
        this(rightInfo, image, previewSizeBlocks, x0, y0, false);
    }

    public static PreviewInfo EMPTY = new PreviewInfo(Component.translatable("tfcgenviewer.preview_world.preview_info.generating"), ImageBuilder.THROBBER, 0, 0, 0, true);
}
