package com.notenoughmail.tfcgenviewer.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

// For all intents and purposes, a holder for a scrollable view of a wrapped text component
// TODO: Implement
public class InfoPane extends AbstractStringWidget {

    private final ResourceLocation pictureTex;
    private final int pictureSize, textWidth;
    private List<FormattedCharSequence> lines;
    private int lengthOfLines;

    public InfoPane(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font pFont, ResourceLocation pictureTex, int pictureSize) {
        super(pX, pY, pWidth, pHeight, pMessage, pFont);
        textWidth = width - 4;
        lines = getFont().split(getMessage(), textWidth);
        lengthOfLines = lines.size() * getFont().lineHeight;
        this.pictureTex = pictureTex;
        this.pictureSize = pictureSize;
    }

    @Override
    public void setMessage(Component pMessage) {
        super.setMessage(pMessage);
        lines = getFont().split(getMessage(), textWidth);
        lengthOfLines = lines.size() * getFont().lineHeight;
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        super.onClick(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
