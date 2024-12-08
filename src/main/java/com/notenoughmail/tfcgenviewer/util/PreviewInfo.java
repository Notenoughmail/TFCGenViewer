package com.notenoughmail.tfcgenviewer.util;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record PreviewInfo(
        Component rightInfo,
        ResourceLocation image,
        int previewSizeGrids,
        int x0,
        int y0,
        Mode mode,
        @Nullable Int2ObjectFunction<Component> tooltip
) {

    public PreviewInfo(Component rightInfo, ResourceLocation image, int previewSizeGrids, int x0, int y0, Mode mode) {
        this(rightInfo, image, previewSizeGrids, x0, y0, mode, null);
    }

    public boolean empty() {
        return mode == Mode.EMPTY;
    }

    public boolean error() {
        return mode == Mode.ERROR;
    }

    public static final PreviewInfo EMPTY = new PreviewInfo(Component.translatable("tfcgenviewer.preview_world.preview_info.generating"), TFCGenViewer.identifier("textures/gui/throbber.png"), 0, 0, 0, Mode.EMPTY);
    public static final PreviewInfo ERROR = new PreviewInfo(Component.translatable("tfcgenviewer.preview_world.preview_info.error"), TFCGenViewer.identifier("textures/gui/gen_error.png"), 0, 0, 0, Mode.ERROR);

    public enum Mode {
        PREVIEW,
        EMPTY,
        ERROR
    }
}
