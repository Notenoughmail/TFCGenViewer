package io.github.notenoughmail.tfcgenviewer.client.widget;

import io.github.notenoughmail.tfcgenviewer.TFCGenViewer;
import io.github.notenoughmail.tfcgenviewer.api.scale.IScale;
import io.github.notenoughmail.tfcgenviewer.api.scale.ImageSize;
import io.github.notenoughmail.tfcgenviewer.impl.ColorTooltips;
import io.github.notenoughmail.tfcgenviewer.impl.preview.Image;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class PreviewPane extends AbstractWidget {

    public static final Component NARRATION_TITLE = Component.translatable("tfcgenviewer.widget.preview_pane.narration.title");

    public static final ResourceLocation PROGRESS_BACKGROUND = TFCGenViewer.id("progress_bar_background");
    public static final ResourceLocation PROGRESS_FILL = TFCGenViewer.id("progress_bar_fill");
    public static final ResourceLocation PROCESSING = TFCGenViewer.id("throbber");
    public static final ResourceLocation ERROR = TFCGenViewer.id("gen_error");
    public static final ResourceLocation DISPLAY = TFCGenViewer.id("dynamic/preview");

    private static DynamicTexture getTexture(Image image) {
        final DynamicTexture texture = new DynamicTexture(image.getNative());
        Minecraft.getInstance().getTextureManager().register(DISPLAY, texture); // Automatically closes the old image
        return texture;
    }

    private Mode tooltipMode;
    private State state;
    private final Supplier<Font> font;
    private final boolean allowCoordinates;
    private float progress;
    private DisplayState display;

    public PreviewPane(int x, int y, Supplier<Font> font, boolean allowCoordinates) {
        super(x, y, 10, 10, Component.empty());
        tooltipMode = Mode.NONE;
        state = State.PROCESSING;
        this.font = font;
        this.allowCoordinates = allowCoordinates;
        resetProgress();
    }

    public void nowProcessing() {
        alterState(false);
    }

    public void alterState(boolean error) {
        state = error ? State.ERROR : State.PROCESSING;
        resetProgress();
        if (display != null) {
            display.close();
            display = null;
        }
    }

    public void updateImage(Image image, ColorTooltips colorTooltips, IScale<?> scale, int x0, int z0) {
        display = new DisplayState(image, colorTooltips, scale, x0, z0, getTexture(image));
        state = State.DISPLAY;
        resetProgress();
    }

    public void updateProgress(int xPixels, ImageSize size) {
        progress = (float) xPixels / size.sizeInPixels();
    }

    private void resetProgress() {
        progress = -1F;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        switch (state) {
            case PROCESSING -> graphics.blitSprite(
                    PROCESSING,
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight()
            );
            case ERROR -> graphics.blitSprite(
                    ERROR,
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight()
            );
            case DISPLAY -> {
                if (isMouseOver(mouseX, mouseY)) {
                    switch (tooltipMode) {
                        case COORDS -> {
                            final int
                                    x0 = display.x0(),
                                    x1 = x0 + display.sizeInBlocks(),
                                    y0 = display.z0(),
                                    y1 = y0 + display.sizeInBlocks(),
                                    x = (int) Mth.map(mouseX, getX(), getX() + getWidth(), x0, x1),
                                    y = (int) Mth.map(mouseY, getY(), getY() + getHeight(), y0, y1);
                            graphics.renderTooltip(font.get(), Component.translatable("tfcgenviewer.widget.preview_pane.hover_pos", x, y), mouseX, mouseY);
                        }
                        case COLOR_DESC -> {
                            final int
                                    previewPixels = display.image().size(),
                                    xPixel = (int) Mth.map(mouseX, getX(), getX() + getWidth(), 0, previewPixels),
                                    yPixel = (int) Mth.map(mouseY, getY(), getY() + getHeight(), 0, previewPixels),
                                    color = display.texture().getPixels().getPixelRGBA(xPixel, yPixel);
                            graphics.renderTooltip(font.get(), display.colors().get(color), mouseX, mouseY);
                        }
                    }
                }
                final int imageSizePixels = display.image().size();
                graphics.blit(
                        DISPLAY,
                        getX(),
                        getY(),
                        getWidth(),
                        getHeight(),
                        0,
                        0,
                        imageSizePixels,
                        imageSizePixels,
                        imageSizePixels,
                        imageSizePixels
                );
            }
        }
        if (progress != -1F) {
            final int scale = 5 - Minecraft.getInstance().options.guiScale().get();
            final int width = Math.min(102 * scale, getWidth() - 10);
            final int leftPos = getX() + ((getWidth() - width) >> 1);
            final int yPos = getY() + getHeight() - 8;
            final int height = Math.min(5 * scale, 20);
            graphics.blitSprite(
                    PROGRESS_BACKGROUND,
                    leftPos,
                    yPos,
                    width,
                    height
            );
            graphics.blitSprite(
                    PROGRESS_FILL,
                    width,
                    height,
                    0,
                    0,
                    leftPos,
                    yPos,
                    (int) (progress * width),
                    height
            );
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        final boolean click = super.clicked(pMouseX, pMouseY);
        if (click && state == State.DISPLAY) tooltipMode = tooltipMode.next(allowCoordinates);
        return click;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        out.add(NarratedElementType.TITLE, NARRATION_TITLE);
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

    private enum State {
        ERROR,
        PROCESSING,
        DISPLAY
    }

    private record DisplayState(Image image, ColorTooltips colors, IScale<?> scale, int x0, int z0, DynamicTexture texture) {

        int sizeInBlocks() {
            return image.size() * scale.blocksPerPixel();
        }

        public void close() {
            image.close();
        }
    }
}
