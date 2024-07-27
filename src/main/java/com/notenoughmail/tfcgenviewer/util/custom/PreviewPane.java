package com.notenoughmail.tfcgenviewer.util.custom;

import com.notenoughmail.tfcgenviewer.util.ImageBuilder;
import com.notenoughmail.tfcgenviewer.util.PreviewInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class PreviewPane extends AbstractWidget {

    private PreviewInfo previewInfo;
    private final int size;
    private boolean showCoordinates;
    private final Font font;

    public PreviewPane(int pX, int pY, int size, Font font) {
        super(pX, pY, size, size, Component.empty());
        this.size = size;
        showCoordinates = false;
        this.font = font;
    }

    public void setInfo(PreviewInfo info) {
        previewInfo = info;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.blit(ImageBuilder.getPreview(previewInfo.scale()), getX(), getY(), 0, 0, size, size, size, size);
        if (showCoordinates && isMouseOver(pMouseX, pMouseY)) {
            final int
                    previewBlocks = previewInfo.previewSizeBlocks(),
                    x0 = previewInfo.x0(),
                    x1 = x0 + previewBlocks,
                    y0 = previewInfo.y0(),
                    y1 = y0 + previewBlocks;
            final int x = (int) Mth.map(pMouseX, getX(), getX() + size, x0, x1);
            final int y = (int) Mth.map(pMouseY, getY(), getY() + size, y0, y1);
            graphics.renderTooltip(font, Component.translatable("tfcgenviewer.preview_world.preview_pos", x, y), pMouseX, pMouseY);
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        final boolean click = super.clicked(pMouseX, pMouseY);
        if (click) showCoordinates = !showCoordinates;
        return click;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
}
