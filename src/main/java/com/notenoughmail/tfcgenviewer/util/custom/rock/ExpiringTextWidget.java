package com.notenoughmail.tfcgenviewer.util.custom.rock;

import com.mojang.blaze3d.systems.RenderSystem;
import com.notenoughmail.tfcgenviewer.util.WidgetUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;

public class ExpiringTextWidget extends MultiLineTextWidget {

    private int tick;
    private final int lifetime;

    public ExpiringTextWidget(Font font, Component pMessage, int lifetime, int maxWidth) {
        super(pMessage, font);
        tick = 0;
        this.lifetime = lifetime;
        setMaxWidth(maxWidth);
    }

    public void centeredOn(int x, int y) {
        setX(x);
        setY(y);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200F); // renderFakeItem uses 150 for 3d items, this puts it above them in the display & editor

        final MultiLineLabel text = cache.getValue(getFreshCacheKey());
        final int top = getY() - (text.getLineCount() * 9) / 2 - 3;

        // Draw the background, I think the setColors have some sort of function, I dunno just following how buttons do it
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitNineSlicedSized(WidgetUtils.GUI_ELEMENTS, getX() - (3 + text.getWidth() / 2), top, text.getWidth() + 6, text.getLineCount() * 9 + 6, 3, 3, 3, 3, 40, 15, 20, 40, 64, 64);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        text.renderCentered(graphics, getX(), top + 3, 9, getColor());

        graphics.pose().popPose();
    }

    public boolean tick() {
        tick++;
        return tick >= lifetime;
    }
}
