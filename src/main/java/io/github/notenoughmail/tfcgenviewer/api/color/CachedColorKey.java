package io.github.notenoughmail.tfcgenviewer.api.color;

import net.minecraft.network.chat.Component;

public interface CachedColorKey {

    Component colorKey();

    void clearCache();
}
