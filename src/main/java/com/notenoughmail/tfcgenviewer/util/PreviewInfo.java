package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.network.chat.Component;

public record PreviewInfo(Component rightInfo, int scale, int previewSizeBlocks, int x0, int y0) {

    public static PreviewInfo EMPTY = new PreviewInfo(Component.empty(), 0, 4096, 0, 0);
}
