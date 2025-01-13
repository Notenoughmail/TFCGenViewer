package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200F); // renderFakeItem uses 150 for 3d items, this puts it above them in the display & editor
        // Recreate super call so that math isn't done twice
        final Component component = getMessage();
        final Font font = getFont();
        final int
                textWidth = font.width(component),
                i = getX() + Math.round(alignX * (getWidth() - textWidth)),
                j = getY() + (getHeight() - 9) / 2;

        // Draw the background, I think the setColors have some sort of function, I dunno just following how buttons do it
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitNineSlicedSized(RockSettingsDisplay.GUI_ELEMENTS, i - 3, j - 3, textWidth + 6, 15, 4, 15, 40, 15, 20, 40, 64, 64);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.drawString(font, component, i, j, getColor());
        graphics.pose().popPose();
    }

    public boolean tick() {
        tick++;
        return tick >= lifetime;
    }
}
