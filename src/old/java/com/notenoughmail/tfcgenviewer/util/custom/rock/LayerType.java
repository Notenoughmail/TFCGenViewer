package com.notenoughmail.tfcgenviewer.util.custom.rock;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public enum LayerType {
    NONE(null),
    BOTTOM("bottom"),
    OCEAN("ocean_floor"),
    VOLCANIC("volcanic"),
    LAND("land"),
    UPLIFT("uplift");

    public final Component title;

    LayerType(@Nullable String key) {
        title = key == null ? CommonComponents.EMPTY : Component.translatable("tfcgenviewer.rock_editor.rock_layer_type." + key);
    }
}
