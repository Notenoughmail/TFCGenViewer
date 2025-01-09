package com.notenoughmail.tfcgenviewer.util.custom;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ExpiringTextWidget extends StringWidget {

    private int tick;
    private final int lifetime;

    public ExpiringTextWidget(Screen parent, Font font, Component pMessage, int lifetime) {
        super(0, 0, parent.width, parent.height, pMessage, font);
        tick = 0;
        this.lifetime = lifetime;
        alignCenter();
    }

    public boolean tick() {
        tick++;
        return tick >= lifetime;
    }
}
