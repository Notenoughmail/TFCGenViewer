package com.notenoughmail.tfcgenviewer.util.custom;

import com.notenoughmail.tfcgenviewer.TFCGenViewer;
import com.notenoughmail.tfcgenviewer.util.PreviewInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PreviewPane extends AbstractWidget {

    public static final ResourceLocation PROGRESS_BAR = TFCGenViewer.identifier("textures/gui/progress_bar.png");

    private PreviewInfo previewInfo;
    private final int size;
    private Mode tooltipMode;
    private final Font font;
    private final boolean allowCoordinates;
    private int tick = 0;
    private int genProgress = -1;

    public PreviewPane(int pX, int pY, int size, Font font, boolean allowCoordinates) {
        super(pX, pY, size, size, Component.empty());
        this.size = size;
        tooltipMode = Mode.NONE;
        this.font = font;
        this.allowCoordinates = allowCoordinates;
        previewInfo = PreviewInfo.EMPTY;
    }

    public void setInfo(PreviewInfo info) {
        previewInfo = info;
    }

    public void setProgress(int progress) {
        genProgress = progress;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (previewInfo.empty()) {
            graphics.blit(previewInfo.image(), getX(), getY(), 0, ((tick >> 1) % 7) * size, size, size, size, size * 8);
        } else {
            graphics.blit(previewInfo.image(), getX(), getY(), 0, 0, size, size, size, size);
        }
        if (!(previewInfo.empty() || previewInfo.error()) && isMouseOver(pMouseX, pMouseY)) {
            switch (tooltipMode) {
                case COORDS -> {
                    final int
                            previewBlocks = previewInfo.previewSizeGrids() * 128,
                            x0 = previewInfo.x0(),
                            x1 = x0 + previewBlocks,
                            y0 = previewInfo.y0(),
                            y1 = y0 + previewBlocks,
                            x = (int) Mth.map(pMouseX, getX(), getX() + size, x0, x1),
                            y = (int) Mth.map(pMouseY, getY(), getY() + size, y0, y1);
                    graphics.renderTooltip(font, Component.translatable("tfcgenviewer.preview_world.preview_pos", x, y), pMouseX, pMouseY);
                }
                case COLOR_DESC -> {
                    final Int2ObjectFunction<Component> tooltips = previewInfo.tooltip();
                    assert tooltips != null : "A preview with mode of PREVIEW should have a tooltip map!";
                    final int
                            previewGrids = previewInfo.previewSizeGrids(),
                            xPixel = (int) Mth.map(pMouseX, getX(), getX() + size, 0, previewGrids),
                            yPixel = (int) Mth.map(pMouseY, getY(), getY() + size, 0, previewGrids),
                            color = ((DynamicTexture) Minecraft.getInstance().getTextureManager().getTexture(previewInfo.image())).getPixels().getPixelRGBA(xPixel, yPixel);
                    graphics.renderTooltip(font, tooltips.get(color), pMouseX, pMouseY);
                }
                default -> {}
            }
        }
        if (genProgress != -1F) {
            final int scale = Minecraft.getInstance().options.guiScale().get(); // [1, 4]
            final int leftPos = getX() + (getWidth() >> 1) - 51;
            final int yPos = getY() + getHeight() - (scale * 8);
            graphics.blit(
                    PROGRESS_BAR,
                    leftPos,
                    yPos,
                    0,
                    0,
                    102,
                    5,
                    128,
                    128
            );
            graphics.blit(
                    PROGRESS_BAR,
                    leftPos,
                    yPos,
                    0,
                    5,
                    genProgress,
                    5,
                    128,
                    128
            );
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        if (previewInfo.empty() || previewInfo.error()) {
            return false;
        }
        final boolean click = super.clicked(pMouseX, pMouseY);
        if (click) tooltipMode = tooltipMode.next(allowCoordinates);
        return click;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}

    public void tick() {
        tick++;
    }

    private enum Mode {
        NONE,
        COORDS,
        COLOR_DESC;

        private Mode next(boolean allowCoords) {
            return switch (this) {
                case NONE -> allowCoords ?
                        COORDS :
                        COLOR_DESC;
                case COORDS -> COLOR_DESC;
                case COLOR_DESC -> NONE;
            };
        }
    }
}
