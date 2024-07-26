package com.notenoughmail.tfcgenviewer.util.custom;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

// For all intents and purposes, a holder for a scrollable view of a wrapped text component
public class InfoPane extends AbstractScrollWidget {

    private final ResourceLocation pictureTex;
    private final int pictureSize, textWidth;
    private List<FormattedCharSequence> lines;
    private int maxLengthOfContent;
    private final Font font;

    public InfoPane(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font pFont, ResourceLocation pictureTex, int pictureSize) {
        super(pX, pY, pWidth, pHeight, pMessage);
        font = pFont;
        textWidth = width - 8;
        this.pictureTex = pictureTex;
        this.pictureSize = pictureSize;
        setMessage(pMessage);
    }

    @Override
    public void setMessage(Component message) {
        lines = font.split(message, textWidth);
        maxLengthOfContent = lines.size() * font.lineHeight + pictureSize + 13;
        setScrollAmount(0);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (!scrollbarVisible()) {
            pGuiGraphics.blit(pictureTex, getX() + 4, getY() + height - pictureSize - 2, 0, 0, pictureSize, pictureSize, pictureSize, pictureSize);
        }
    }

    @Override
    protected int getInnerHeight() {
        return maxLengthOfContent;
    }

    @Override
    protected double scrollRate() {
        return 9;
    }

    @Override
    protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int elementY = getY() + 4;
        for (FormattedCharSequence chars : lines) {
            pGuiGraphics.drawString(font, chars, getX() + 4, elementY, 0xFFFFFF, false);
            elementY += 9;
        }
        if (scrollbarVisible()) {
            elementY += 9;
            pGuiGraphics.blit(pictureTex, getX() + 4, elementY, 0, 0, pictureSize, pictureSize, pictureSize, pictureSize);
        }
    }

    @Override
    protected void renderBorder(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight) {
        pGuiGraphics.fill(pX + 1, pY + 1, pX + pWidth - 1, pY + pHeight - 1, -16777216);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
